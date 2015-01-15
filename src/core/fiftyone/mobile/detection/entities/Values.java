package fiftyone.mobile.detection.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import fiftyone.properties.DetectionConstants;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 * Encapsulates a list of one or more values. Provides methods to return
 * boolean, double and string representations of the values list. <p> The class
 * contains helper methods to make consuming the data set easier. <p> For more
 * information see http://51degrees.mobi/Support/Documentation/Java
 */
@SuppressWarnings("serial")
public class Values extends ArrayList<Value> {

    /**
     * The property the list of values relates to.
     */
    private final Property property;

    /**
     * Constructs a new instance of the values list.
     *
     * @param property Property the values list relates to
     * @param values IEnumerable of values to use to initialise the list
     */
    Values(Property property, Collection<Value> values) {
        super(values);
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
                    "toBool can only be used on non List properties");
        }
        return get(0).toBool();
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
                    "toDouble can only be used on non List properties");
        }
        return get(0).toDouble();
    }

    /**
     * Returns the values as a string array.
     *
     * @return a string array of values
     * @throws IOException indicates an I/O exception occurred
     */
    public String[] toStringArray() throws IOException {
        String[] array = new String[size()];
        for (int i = 0; i < size(); i++) {
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

        for (int i = 0; i < size(); i++) {
            result.append(get(i));

            if (i != size() - 1) {
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
        for (Value value : this) {
            if (value.getIsDefault()) {
                return true;
            }
        }
        return false;
    }
}
