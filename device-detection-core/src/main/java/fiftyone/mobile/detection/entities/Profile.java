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
package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.SortedList;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a collection of properties and values relating to a profile which
 * in turn relates to a component. Each Signature relates to one profile for
 * each component.
 */
public abstract class Profile extends BaseEntity implements Comparable<Profile> {
    /**
     * The component the profile belongs to.
     */
    private volatile Component component;
    /**
     * Index of the component the profile belongs to.
     */
    private final int componentIndex;
    /**
     * List of Values associated with the property name.
     */
    private volatile SortedList<String, Values> nameToValues;
    /**
     * Array of Properties associated with the profile.
     */
    private volatile Property[] properties;
    /**
     * List of Property indexes wit the corresponding list of values.
     */
    private volatile SortedList<Integer, Values> propertyIndexToValues;
    /**
     * The release date of the profile if it's a hardware profile.
     */
    private Date releaseDate;
    /**
     * Release date checked.
     */
    private boolean releaseDateChecked;
    /**
     * Array of signatures associated with the profile.
     */
    private volatile Signature[] signatures;
    /**
     * An array of the signature indexes associated with the profile.
     * @abstract
     */
    protected volatile int[] signatureIndexes;
    /**
     * A string representation of the profile.
     */
    private volatile String stringValue;
    /**
     * An array of values associated with the profile.
     */
    private volatile Value[] values;
    /**
     * Unique Id of the profile. Does not change between different data sets.
     */
    public final int profileId;
    /**
     * A list of the indexes of the values associated with the profile.
     * @abstract
     */
    protected volatile int[] valueIndexes;
    /**
     * Returned when the property has no values in the provide.
     */
    private final Value[] emptyValues = new Value[0];
    
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
    }
    
    /**
     * Compares this profile to another using the numeric ProfileId field.
     *
     * @param other The component to be compared against.
     * @return Indication of relative value based on ProfileId field.
     */
    @Override
    public int compareTo(Profile other) {
        return profileId - other.profileId;
    }
    
    /**
     * The component the profile belongs to.
     * @return The component the profile belongs to
     * @throws java.io.IOException
     */
    public Component getComponent() throws IOException {
        Component localComponent = component;
        if (localComponent == null) {
            synchronized (this) {
                localComponent = component;
                if (localComponent == null) {
                    component = localComponent = getDataSet().getComponents().get(componentIndex);
                }
            }
        }
        return localComponent;
    }
    
    /**
     * Returns an array of properties the profile relates to.
     * @return An array of properties associated with the profile.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Property[] getProperties() throws IOException {
        Property[] localProperties = properties;
        if (localProperties == null) {
            synchronized (this) {
                localProperties = properties;
                if (localProperties == null) {
                    properties = localProperties = doGetProperties();
                }
            }
        }
        return localProperties;
    }
    
    /**
     * Returns an array of properties the profile relates to. Used by the 
     * getProperties method.
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
     * Gets the values associated with the property name.
     *
     * @param propertyName Name of the property whose values are required
     * @return Array of the values associated with the property, or null if the
     * property does not exist
     * @throws IOException indicates an I/O exception occurred
     */
    public Values getValues(String propertyName) throws IOException {
        Values localValues = null;
        localValues = getPropertyNameToValues().get(propertyName);
        if (localValues == null) {
            synchronized (this) {
                getPropertyNameToValues().get(propertyName);
                if (localValues == null) {
                    Property property = dataSet.get(propertyName);
                    if (property != null) {
                        localValues = this.getValues(property);
                        getPropertyNameToValues().add(propertyName, localValues);
                    }
                }
            }
        }
        return localValues;
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
        Values localValues = null;
        localValues = getPropertyIndexToValues().get(property.getIndex());
        if (localValues == null) {
            synchronized (this) {
                localValues = getPropertyIndexToValues().get(property.getIndex());
                if (localValues == null) {
                    Value[] v = getPropertyValues(property);
                    localValues = new Values(property, v);
                    getPropertyIndexToValues().add(property.getIndex(), localValues);
                }
            }
        }
        return localValues;
        /*
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
        */
    }
    
    /**
     * Gets the values associated with the property for this profile.
     * @param property Property to be returned.
     * @return Array of values associated with the property and profile.
     */
    private Value[] getPropertyValues(Property property) throws IOException {
        // Work out the start and end index in the values associated
        // with the profile that relate to this property.
        Value[] result;
        //Arrays.sort(getValueIndexes());
        //int start = getValuesIndex(property.firstValueIndex, 0);
        int start = Arrays.binarySearch(getValueIndexes(), property.firstValueIndex);
        
        // If the start is negative then the first value doesn't exist.
        // Take the complement and use this as the first index which will 
        // relate to the first value associated with the profile for the
        // property.
        if (start < 0) {
            start = ~start;
        }
        
        //int end = getValuesIndex(property.getLastIndexValue(), start);
        int end = Arrays.binarySearch(getValueIndexes(), 
                start, 
                (getValueIndexes().length), 
                property.getLastIndexValue());
        
        // If the end is negative then the last value doesn't exist. Take
        // the complement and use this as the last index. However if this 
        // value doesn't relate to the property then it's the first value
        // for the next property and we need to move back one in the list.
        if (end < 0) {
            end = ~end;
            if (end >= getValueIndexes().length ||
                    getValueIndexes()[end] > property.getLastIndexValue()) {
                end--;
            }
        }
        // If start is greater than end then there are no values for this
        // property in this profile. Return an empty array.
        if (start > end) {
            result = emptyValues;
        } else {
            result = new Value[end - start + 1];
            for (int i = start, v = 0; i <= end; i++, v++) {
                Value value = dataSet.values.get(getValueIndexes()[i]);
                result[v] = value;
            }
        }
        // Create the array and populate it with the values for the profile
        // and property.
        return result;
    }
    
    /**
     * Gets the signatures related to the profile.
     * @return Array of signatures associated with the profile.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Signature[] getSignatures() throws IOException {
        Signature[] localSignatures = signatures;
        if (localSignatures == null) {
            synchronized (this) {
                localSignatures = signatures;
                if (localSignatures == null) {
                    signatures = localSignatures = doGetSignatures();
                }
            }
        }
        return localSignatures;
    }
    
    /**
     * Gets the signatures related to the profile.
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
     * Gets the values associated with the profile.
     * @return An array of values associated with the profile.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Value[] getValues() throws IOException {
        Value[] localValues = values;
        if (localValues == null) {
            synchronized (this) {
                localValues = values;
                if (localValues == null) {
                    values = localValues = doGetValues();
                }
            }
        }
        return localValues;
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
     * 
     * @param valueIndex
     * @param lower
     * @return 
     */
    private int getValuesIndex(int valueIndex, int lower) {
        //int upper = valueIndexes.length - 1;
        int upper = getValueIndexes().length - 1;
        int middle = 0;
        
        while(lower <= upper) {
            middle = lower + (upper - lower) / 2;
            if (getValueIndexes()[middle] == 0) {
                return middle;
            } else if(getValueIndexes()[middle] == 0) {
                upper = middle - 1;
            } else {
                lower = middle + 1;
            }
        }
        return ~middle;
    }
    
    /**
     * Called after the entire data set has been loaded to ensure 
     * any further initialisation steps that require other items in
     * the data set can be completed.
     *
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public void init() throws IOException {
        if (properties == null)
            properties = doGetProperties();
        if (values == null)
            values = doGetValues();
        if (signatures == null)
            signatures = doGetSignatures();
        if (component == null)
            component = getDataSet().getComponents().get(componentIndex);
    }
    
    /**
     * A hash map relating the name of a property to the values returned by 
     * the profile. Used to speed up subsequent data processing.
     * @return a hash map with values mapped to specific property.
     */
    private SortedList<String, Values> getPropertyNameToValues() {
        SortedList<String, Values> localPropertyNameToValues = propertyNameToValues;
        if (localPropertyNameToValues == null) {
            synchronized (this) {
                localPropertyNameToValues = propertyNameToValues;
                if (localPropertyNameToValues == null) {
                    propertyNameToValues = localPropertyNameToValues = 
                            new SortedList<String, Values>();
                }
            }
        }
        return localPropertyNameToValues;
    }
    private volatile SortedList<String, Values> propertyNameToValues;
    
    /**
     * A hash map relating the index of a property to the values returned 
     * by the profile. Used to speed up subsequent data processing.
     * @return a hash map with property indexes mapped to corresponding values.
     */
    private SortedList<Integer, Values> getPropertyIndexToValues() {
        SortedList<Integer, Values> localPropertyIndexToValues = propertyIndexToValues;
        if (localPropertyIndexToValues == null) {
            synchronized(this) {
                localPropertyIndexToValues = propertyIndexToValues;
                if (localPropertyIndexToValues == null) {
                    propertyIndexToValues = localPropertyIndexToValues = 
                            new SortedList<Integer, Values>();
                }
            }
        }
        return localPropertyIndexToValues;
    }
    
    /**
     * A string representation of the profiles display values.
     *
     * @return the profile as a string
     */
    @Override
    public String toString() {
        String localStringValue = stringValue;
        if (localStringValue == null) {
            synchronized (this) {
                localStringValue = stringValue;
                if (localStringValue == null) {
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
                            stringValue = localStringValue = sb.toString();
                        } else {
                            stringValue = localStringValue = String.valueOf(profileId);
                        }
                    } catch (IOException e) {
                        stringValue = localStringValue = String.valueOf(profileId);
                    }
                }
            }
        }
        return localStringValue;
    }
    
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
     * A array of the indexes of the values associated with the profile in order
     * of value index in the data set values list.
     * @return A array of the indexes of the values associated with the profile.
     */
    public abstract int[] getValueIndexes();
    
    /**
     * An array of the signature indexes associated with the profile.
     * @return An array of the signature indexes associated with the profile.
     */
    public abstract int[] getSignatureIndexes();
}
