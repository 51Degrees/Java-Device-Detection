/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Profile entity with stream specific data access implementation.
 */
public class Profile extends fiftyone.mobile.detection.entities.Profile {
    private final int position;
    private final int valueIndexesCount;
    private final int signatureIndexesCount;
    private final Pool pool;
    
    public Profile(Dataset dataSet, int index, BinaryReader reader) {
        super(dataSet, index, reader);
        this.pool = dataSet.pool;
        valueIndexesCount = reader.readInt32();
        signatureIndexesCount = reader.readInt32();
        position = reader.getPos();
    }
    
    @Override
    public int[] getValueIndexes() {
        if(valueIndexes == null) {
            synchronized(this) {
                if(valueIndexes == null) {
                    BinaryReader reader = null;
                    try {
                        reader = pool.getReader();
                        reader.setPos(position);
                        valueIndexes = BaseEntity.readIntegerArray(reader, valueIndexesCount);
                    } catch (Exception ex) {
                        throw new Error("Cannot to obtain _valueIndexes: "+ex);
                    } finally {
                        if (reader != null) {
                            pool.release(reader);
                        }
                    }
                }
            }
        }
        return valueIndexes;
    }
    
    public int[] getSignatureIndexes() {
        if(signatureIndexes == null) {
            synchronized(this) {
                if (signatureIndexes == null) {
                    BinaryReader reader = null;
                    try {
                        reader = pool.getReader();
                        int offset = valueIndexesCount * (Integer.SIZE / Byte.SIZE);
                        reader.setPos(position + offset);
                        signatureIndexes = BaseEntity.readIntegerArray(reader, signatureIndexesCount);
                    } catch (Exception ex) {
                        throw new Error("Cannot to obtain _signatureIndexes: "+ex);
                    } finally {
                        if (reader != null)
                            pool.release(reader);
                    }
                }
            }
        }
        return signatureIndexes;
    }
}
