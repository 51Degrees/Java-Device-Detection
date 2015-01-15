package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.SortedList;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
 * Represents a collection of properties and values relating to a profile which
 * in turn relates to a component. Each Signature relates to one profile for
 * each component.
 */
public class Profile extends BaseEntity implements Comparable<Profile> {

    /**
     * Comparator used to order the properties by descending display order.
     */
    private static final Comparator<Value> propertyComparator = new Comparator<Value>() {
        @Override
        public int compare(Value v1, Value v2) {
            try {
                if (v1.getProperty().displayOrder < v2.getProperty().displayOrder) {
                    return 1;
                }
                if (v1.getProperty().displayOrder > v2.getProperty().displayOrder) {
                    return -1;
                }
                return 0;
            } catch (IOException e) {
                return 0;
            }
        }
    };
    /**
     * Unique Id of the profile. Does not change between different data sets.
     */
    public final int profileId;
    /**
     * A list of the indexes of the values associated with the profile.
     */
    private final int[] valueIndexes;

    /**
     * Gets the values associated with the property name.
     *
     * @param propertyName Name of the property whose values are required
     * @return Array of the values associated with the property, or null if the
     * property does not exist
     * @throws IOException indicates an I/O exception occurred
     */
    public Values getValues(String propertyName) throws IOException {
        return getValues(getDataSet().get(propertyName));
    }

    /**
     * Gets the values associated with the property.
     *
     * @param property The property whose values are required
     * @return Array of the values associated with the property, or null if the
     * property does not exist
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Values getValues(Property property) throws IOException {
        // Does the storage structure already exist?
        if (nameToValues == null) {
            synchronized (this) {
                if (nameToValues == null) {
                    nameToValues = new SortedList<String, Values>();
                }
            }
        }

        // Do the values already exist for the property?
        synchronized (nameToValues) {

            Values result = nameToValues.get(property.getName());
            if (result != null) {
                return result;
            }

            // Create the list of values.
            List<Value> vals = new ArrayList<Value>();
            for (Value value : getValues()) {
                if (value.getProperty() == property) {
                    vals.add(value);
                }
            }
            result = new Values(property, vals);

            if (result.size() == 0) {
                result = null;
            }

            // Store for future reference.
            nameToValues.add(property.getName(), result);

            return result;
        }
    }
    private SortedList<String, Values> nameToValues;

    /**
     * @return Array of signatures associated with the profile.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Signature[] Signatures() throws IOException {
        if (signatures == null) {
            synchronized (this) {
                if (signatures == null) {
                    signatures = doGetSignatures();
                }
            }
        }
        return signatures;
    }
    private Signature[] signatures;
    private final int[] signatureIndexes;

    /**
     * @return The component the profile belongs to
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Component getComponent() throws IOException {
        if (component == null) {
            synchronized (this) {
                if (component == null) {
                    component = getDataSet().getComponents()
                            .get(componentIndex);
                }
            }
        }
        return component;
    }
    private Component component;
    private final int componentIndex;

    /**
     * @return An array of values associated with the profile.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Value[] getValues() throws IOException {
        if (values == null) {
            synchronized (this) {
                if (values == null) {
                    values = doGetValues();
                }
            }
        }
        return values;
    }
    private Value[] values;

    /**
     * @return An array of properties associated with the profile.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Property[] getProperties() throws IOException {
        if (properties == null) {
            synchronized (this) {
                if (properties == null) {
                    properties = doGetProperties();
                }
            }
        }
        return properties;
    }
    private Property[] properties = null;

    /**
     * Constructs a new instance of the Profile
     *
     * @param dataSet The data set the profile will be contained with in
     * @param offset The offset of the profile in the source data structure
     * @param reader Reader connected to the input stream
     */
    public Profile(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset);
        this.componentIndex = reader.readByte();
        this.profileId = reader.readInt32();
        int valueIndexesCount = reader.readInt32();
        int signatureIndexesCount = reader.readInt32();
        this.valueIndexes = BaseEntity.readIntegerArray(reader, valueIndexesCount);
        this.signatureIndexes = BaseEntity.readIntegerArray(reader, signatureIndexesCount);
    }

    /**
     * If storage of object references is enabled initialises the arrays of
     * related properties and values.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public void init() throws IOException {
        properties = doGetProperties();
        values = doGetValues();
        signatures = doGetSignatures();
        component = getDataSet().getComponents().get(componentIndex);
    }

    /**
     * @return Returns an array of signatures the profile relates to.
     */
    private Signature[] doGetSignatures() throws IOException {
        Signature[] array = new Signature[signatureIndexes.length];
        for (int i = 0; i < signatureIndexes.length; i++) {
            array[i] = getDataSet().getSignatures().get(signatureIndexes[i]);
        }
        return array;
    }

    /**
     * @return Returns an array of properties the profile relates to.
     */
    private Property[] doGetProperties() throws IOException {
        Set<Property> tree = new TreeSet<Property>(
                new Comparator<Property>() {
                    public int compare(Property o1, Property o2) {
                        try {
                            return o1.getName().compareTo(o2.getName());
                        } catch (IOException ex) {
                            return 0;
                        }
                    }
                });

        for (Value value : getValues()) {
            tree.add(value.getProperty());
        }

        return tree.toArray(new Property[tree.size()]);
    }

    /**
     * @return Returns an array of values the profile relates to.
     */
    private Value[] doGetValues() throws IOException {
        Value[] array = new Value[getValueIndexes().length];
        for (int i = 0; i < array.length; i++) {
            array[i] = getDataSet().getValues().get(getValueIndexes()[i]);
        }
        return array;
    }

    /**
     * Compares this profile to another using the numeric ProfileId field.
     *
     * @param other The component to be compared against
     * @return Indication of relative value based on ProfileId field
     */
    @Override
    public int compareTo(Profile other) {
        return profileId - other.profileId;
    }

    /**
     * A string representation of the profiles display values.
     *
     * @return the profile as a string
     */
    @Override
    public String toString() {
        if (stringValue == null) {
            synchronized (this) {
                if (stringValue == null) {
                    List<Value> list = new ArrayList<Value>();
                    try {
                        for (int i = 0; i < getValues().length; i++) {
                            Value value = getValues()[i];
                            if (value.getProperty().displayOrder > 0
                                    && value.getName().contains("Unknown") == false) {
                                int index = Collections.binarySearch(list, value, propertyComparator);
                                if (index < 0) {
                                    list.add(~index, value);
                                }
                            }
                        }
                        if (list.size() > 0) {
                            // Values with a display order were found. Sort 
                            // them and then concatenate before returning.
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < list.size(); i++) {
                                sb.append(list.get(i).toString());
                                if (i < list.size() - 1) {
                                    sb.append("/");
                                }
                            }
                            stringValue = sb.toString();
                        } else {
                            stringValue = "Blank";
                        }
                    } catch (IOException e) {
                        stringValue = "Blank";
                    }
                }
            }
        }
        return stringValue;
    }
    private String stringValue = null;

    public int[] getValueIndexes() {
        return valueIndexes;
    }

    public int[] getSignatureIndexes() {
        return signatureIndexes;
    }
    private static final int MIN_LENGTH = 1 + 4 + 4 + 4;

    public int getLength() {
        return MIN_LENGTH + (valueIndexes.length * 4)
                + (signatureIndexes.length * 4);
    }
}
