/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 *
 * @author mike
 */
public class PropertiesList extends MemoryFixedList<Property> {

    public PropertiesList(Dataset dataSet, BinaryReader reader, 
            BaseEntityFactory<Property> entityFactory) {
        super(dataSet, reader, entityFactory);
    }
}
