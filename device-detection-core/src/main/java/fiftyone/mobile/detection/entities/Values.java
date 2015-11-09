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
package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.search.SearchArrays;
import java.io.IOException;

import fiftyone.properties.DetectionConstants;

/**
 * Encapsulates a list of one or more values. Provides methods to return
 * boolean, double and string representations of the values list. 
 * 
 * The class contains helper methods to make consuming the data set easier.
 */
@SuppressWarnings("serial")
public class Values {

    private static class SearchValuesByName extends SearchArrays<Value, String> {
        @Override
        public int compareTo(Value item, String key) {
            return item.compareTo(key);
        }
    }
    
    /**
     * Used to find values based on name.
     */
    private static final SearchValuesByName valuesNameSearch = 
            new SearchValuesByName();    
    
    /**
     * The property the list of values relates to.
     */
    private final Property property;
    
    /**
     * An array of values to expose.
     */
    private final Value[] values;
    
    /**
     * Constructs a new instance of the values list.
     *
     * @param property Property the values list relates to
     * @param values IEnumerable of values to use to initialise the list
     */
    Values(Property property, Value[] values) {
        this.values = values;
        this.property = property;
    }

    /**
     * The value represented as a boolean. return A boolean representation of
     * the only item in the list. MobileException Thrown if the method is called
     * for a property with multiple values
     *
     * @return return A boolean representation of the only item in the list
     * @throws IOException indicates an I/O exception occurred
     */
    public boolean toBool() throws IOException {
        if (property.isList) {
            throw new UnsupportedOperationException(
                    "Can't convert list to a boolean.");
        }
        if (values.length > 0) {
            return  get(0).toBool();
        }
        return false;
    }

    /**
     * The value represented as a double.
     *
     * @return A double representation of the only item in the list.
     * @throws IOException indicates an I/O exception occurred
     */
    public double toDouble() throws IOException {
        if (property.isList) {
            throw new UnsupportedOperationException(
                    "Can't convert list to double.");
        }
        if (values.length > 0) {
            return get(0).toDouble();
        }
        return 0;
    }

    /**
     * Returns the values as a string array.
     *
     * @return a string array of values
     * @throws IOException indicates an I/O exception occurred
     */
    public String[] toStringArray() throws IOException {
        String[] array = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = this.get(i).getName();
        }
        return array;
    }

    /**
     * The values represented as a string where multiple values are seperated by
     * colons.
     *
     * @return The values as a string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            result.append(get(i));

            if (i != values.length - 1) {
                result.append(DetectionConstants.VALUE_SEPARATOR);
            }
        }

        return result.toString();
    }

    /**
     * Returns true if any of the values are the null values for the property.
     *
     * @return true if any of the values are the null values for the property
     * @throws IOException indicates an I/O exception occurred
     */
    public boolean getIsDefault() throws IOException {
        for (Value value : values) {
            if (value.getIsDefault()) {
                return true;
            }
        }
        return false;
    }
    
    public Value[] getAll() {
        return values;
    }
    
    /**
     * @param valueName name required.
     * @return the value associated with the name provided.
     * @throws java.io.IOException
     */
    public Value get(String valueName) throws IOException {
        int index = valuesNameSearch.binarySearch(values, valueName).getIndex();
        return index >= 0 ? get(index) : null;
    }
    
    public Value get(int index) {
        return values[index];
    }
    
    public boolean contains(Value item) {
        for (Value v : values) {
            if (v.equals(item)) {
                return true;
            }
        }
        return false;
    }
    
    public void copyTo(Value[] array, int arrayIndex) {
        int current = arrayIndex;
        int i = 0;
        while (current < values.length) {
            array[i] = values[current];
            current++;
            i++;
        }
    }
    
    public int count() {
        return values.length;
    }
    
    
}
