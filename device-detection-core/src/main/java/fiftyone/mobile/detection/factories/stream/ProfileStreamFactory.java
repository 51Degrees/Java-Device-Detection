/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.factories.stream;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.stream.Pool;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 *
 * @author mike
 */
public class ProfileStreamFactory extends fiftyone.mobile.detection.factories.ProfileFactory {
    
    private final Pool pool;
    
    public ProfileStreamFactory(Pool pool) {
        this.pool = pool;
    }

    @Override
    protected Profile construct(Dataset dataSet, int index, BinaryReader reader) {
        return new fiftyone.mobile.detection.entities.stream.Profile((fiftyone.mobile.detection.entities.stream.Dataset)dataSet, index, reader);
    }
}
