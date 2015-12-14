/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.ISimpleList;
import fiftyone.mobile.detection.Utilities;
import fiftyone.mobile.detection.entities.headers.Header;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mike
 */
public class MemoryIntegerList implements ISimpleList {
    
    private final Header header;
    protected final int[] array;
    
    public MemoryIntegerList(BinaryReader reader) {
        this.header = new Header(reader);
        this.array = new int[this.header.getCount()];
    }
    
    public void read(BinaryReader reader) {
        for (int i = 0; i < header.getCount(); i++) {
            array[i] = reader.readInt32();
        }
    }

    @Override
    public int get(int index) {
        if (index > array.length || index < 0) {
            throw new ArrayIndexOutOfBoundsException("The requested element is "
                    + "out of bounds for this array.");
        }
        return array[index];
    }
    
    @Override
    public List<Integer> getRange(int index, int count) {
        return new ArrayRange<Integer>(index, count, this.array);
    }

    

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
