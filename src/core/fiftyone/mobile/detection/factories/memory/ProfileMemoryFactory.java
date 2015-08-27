/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.factories.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.factories.ProfileFactory;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 *
 * @author mike
 */
public class ProfileMemoryFactory extends ProfileFactory {

    @Override
    protected Profile construct(Dataset dataSet, int index, BinaryReader reader) {
        return new fiftyone.mobile.detection.entities.memory.Profile(dataSet, index, reader);
    }
    
}
