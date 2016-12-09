/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2015 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patent 
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent Application No. 13192291.6; and
 * United States Patent Application Nos. 14/085,223 and 14/085,301.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 * 
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A list of properties in memory as a fixed list. Contains an accessor which 
 * can be used to retrieve entries by property name.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public class PropertiesList extends MemoryFixedList<Property> {

    /**
     * Constructs a new instance of PropertiesList.
     * 
     * @param dataSet The Dataset being created.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @param entityFactory Used to create new instances of the entity.
     */
    public PropertiesList(Dataset dataSet, BinaryReader reader, 
            BaseEntityFactory<Property> entityFactory) {
        super(dataSet, reader, entityFactory);
    }
    
    /**
     * Returns the properties in the list as a dictionary where the key is the 
     * name of the property. Used to rapidly return this property from the name.
     * 
     * @return HashMap of Property name -> Property object entries.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    private Map<String, Property> getPropertyNameDictionary() 
            throws IOException {
        Map<String, Property> localPropertyNameDictionary = propertyNameDictionary;
        if (localPropertyNameDictionary == null) {
            synchronized(this) {
                localPropertyNameDictionary = propertyNameDictionary;
                if (localPropertyNameDictionary == null) {
                    localPropertyNameDictionary = new HashMap<String, Property>();
                    for (Property p : array) {
                        localPropertyNameDictionary.put(p.getName(), p);
                    }
                    propertyNameDictionary = localPropertyNameDictionary;
                }
            }
        }
        return localPropertyNameDictionary;
    }
    private Map<String, Property> propertyNameDictionary;
    
    /**
     * Returns the property matching the name provided, or null if no such 
     * property is available.
     * 
     * @param propertyName Property name required.
     * @return The property matching the name, otherwise null.
     * @throws java.io.IOException if there was a problem reading from the data 
     * file.
     */
    public Property get(String propertyName) throws IOException {
        Property property = getPropertyNameDictionary().get(propertyName);
        return property;
    }
}
