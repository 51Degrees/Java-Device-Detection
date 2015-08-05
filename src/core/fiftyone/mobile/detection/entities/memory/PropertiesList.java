package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
/**
 * A list of properties in memory as a fixed list. Contains an accessor which 
 * can be used to retrieve entries by property name.
 */
public class PropertiesList extends MemoryFixedList<Property> {

    /**
     * Contains the key value pair of Property name and Property object.
     */
    private final Map<String, Property> propertyNameDictionary;
    
    /**
     * Constructs a new instance of PropertiesList.
     * @param dataSet The Dataset being created.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @param entityFactory Used to create new instances of the entity
     */
    public PropertiesList(Dataset dataSet, BinaryReader reader, 
            BaseEntityFactory<Property> entityFactory) {
        super(dataSet, reader, entityFactory);
        this.propertyNameDictionary = new HashMap<String, Property>();
    }
    
    /**
     * Returns the properties in the list as a dictionary where the key is the 
     * name of the property. Used to rapidly return this property from the name.
     * @return HashMap of Property name -> Property object entries.
     */
    private Map<String, Property> getPropertyNameDictionary() {
        if (propertyNameDictionary.isEmpty()) {
            synchronized(this) {
                if (propertyNameDictionary.isEmpty()) {
                    for (Property p : array) {
                        //Equivalent of the this.ToDictionary(k => k.Name) in c#
                        try {
                            propertyNameDictionary.put(p.getName(), p);
                        } catch (IOException ex) {
                            Logger.getLogger(PropertiesList.class.getName())
                                                .log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
        return propertyNameDictionary;
    }
    
    /**
     * Returns the property matching the name provided, or null if no such 
     * property is available.
     * @param propertyName Property name required.
     * @return The property matching the name, otherwise null.
     */
    public Property get(String propertyName) {
        Property property = getPropertyNameDictionary().get(propertyName);
        return property;
    }
}
