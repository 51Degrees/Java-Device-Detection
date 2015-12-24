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

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.SortedList;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Profile is a collection of {@link Value values} for a single 
 * {@link Component} gathered under a unique Id.
 * <p>
 * All profiles belong to one of the four components: hardware, software, 
 * crawler and browser and have a number of values associated with each profile.
 * The contents of existing profiles should not change over time. Each value 
 * contains a getter method for the corresponding {@link Property}.
 * <p>
 * Profiles make up the unique Id of a device. Each device Id consists of
 * <p>
 * Each profile relates to one or more {@link Signature}.
 * <p>
 * New profiles are added with each data file update. Some profiles may be 
 * removed if we do not see any use of the profile for a long period of time. 
 * Premium and Enterprise data contain a lot more profiles and hence provide 
 * better detection results, especially for less common devices.
 * <a href="https://51degrees.com/compare-data-options">
 * Compare data options</a>.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic. Use the relevant {@link Dataset} method to access these 
 * objects.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public abstract class Profile extends BaseEntity implements Comparable<Profile> {
    
    /**
     * Index of the component the profile belongs to.
     */
    private final int componentIndex;
    
    /**
     * An array of the signature indexes associated with the profile.
     */
    @SuppressWarnings("VolatileArrayField")
    protected volatile int[] signatureIndexes;
    
    /**
     * Unique Id of the profile. Does not change between different data sets.
     */
    public final int profileId;
    
    /**
     * A list of the indexes of the values associated with the profile.
     */
    @SuppressWarnings("VolatileArrayField")
    protected volatile int[] valueIndexes;
    
    /**
     * Returned when the property has no values in the provide.
     */
    private final Value[] emptyValues = new Value[0];
    
    /**
     * Constructs a new instance of the Profile
     *
     * @param dataSet The data set the profile will be contained within.
     * @param offset The offset of the profile in the source data structure.
     * @param reader Reader connected to the input stream.
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
     * 
     * @return The {@link Component} the profile belongs to.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Component getComponent() throws IOException {
        Component localComponent = component;
        if (localComponent == null) {
            synchronized (this) {
                localComponent = component;
                if (localComponent == null) {
                    component = localComponent = 
                            getDataSet().getComponents().get(componentIndex);
                }
            }
        }
        return localComponent;
    }
    private volatile Component component;
    
    /**
     * Returns an array of properties the profile relates to.
     * 
     * @return array of {@link Property properties} associated with this profile.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
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
    @SuppressWarnings("VolatileArrayField")
    private volatile Property[] properties;
    
    /**
     * Returns an array of properties the profile relates to. Used by the 
     * getProperties method.
     * 
     * @return Returns an array of properties the profile relates to.
     */
    private Property[] doGetProperties() throws IOException {
        Set<Property> tree = new TreeSet<Property>(
                new Comparator<Property>() {
                    @Override
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
     * @param propertyName string name of the {@link Property} required.
     * @return Array of the {@link Values} associated with the property, or null 
     * if the property does not exist.
     * @throws IOException if there was a problem accessing data file.
     */
    public Values getValues(String propertyName) throws IOException {
        return getValues(dataSet.properties.get(propertyName));
    }
    
    /**
     * Gets the values associated with the property.
     *
     * @param property the {@link Property} whose values are required.
     * @return Array of the {@link Values} associated with the property, or null 
     * if the property does not exist.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Values getValues(Property property) throws IOException {
        Values localValues;
        localValues = getPropertyIndexToValues().get(property.getIndex());
        if (localValues == null) {
            synchronized (this) {
                localValues = getPropertyIndexToValues().get(property.getIndex());
                if (localValues == null) {
                    localValues = getPropertyValues(property);
                    getPropertyIndexToValues().add(property.getIndex(), localValues);
                }
            }
        }
        return localValues;
    }
    
    /**
     * Gets the values associated with the property for this profile.
     * 
     * @param property Property to be returned.
     * @return Array of values associated with the property and profile.
     */
    private Values getPropertyValues(Property property) throws IOException {
        // Work out the start and end index in the values associated
        // with the profile that relate to this property.
        Value[] result;
        int start = 
                Arrays.binarySearch(getValueIndexes(), property.firstValueIndex);
        
        // If the start is negative then the first value doesn't exist.
        // Take the complement and use this as the first index which will 
        // relate to the first value associated with the profile for the
        // property.
        if (start < 0) {
            start = ~start;
        }
        
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
        return new Values(property, result);
    }
    
    /**
     * Gets the signatures related to the profile.
     * 
     * @return array of {@link Signature signatures} associated with this profile.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
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
    @SuppressWarnings("VolatileArrayField")
    private volatile Signature[] signatures;
    
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
     * 
     * @return array of {@link Value values} associated with this profile.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
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
    @SuppressWarnings("VolatileArrayField")
    private volatile Value[] values;
    
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
     * Called after the entire data set has been loaded to ensure 
     * any further initialisation steps that require other items in
     * the data set can be completed.
     * <p>
     * This method should not be called as it is part of the internal logic.
     *
     * @throws java.io.IOException if there was a problem accessing data file.
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
        for(Property property : properties) {
            getPropertyIndexToValues().add(
                    property.getIndex(),
                    getPropertyValues(property));
        }
    }
   
    /**
     * A hash map relating the index of a property to the values returned 
     * by the profile. Used to speed up subsequent data processing.
     * 
     * @return a hash map with property indexes mapped to corresponding values.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    private SortedList<Integer, Values> getPropertyIndexToValues() {
        SortedList<Integer, Values> localPropertyIndexToValues = 
                propertyIndexToValues;
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
    private volatile SortedList<Integer, Values> propertyIndexToValues;
    
    /**
     * A string representation of the profiles display values.
     *
     * @return the profile as a string.
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
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
                                int tempIndex = Collections.binarySearch(
                                            list, value, propertyComparator);
                                if (tempIndex < 0) {
                                    list.add(~tempIndex, value);
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
                            stringValue = 
                                    localStringValue = String.valueOf(profileId);
                        }
                    } catch (IOException e) {
                        stringValue = 
                                localStringValue = String.valueOf(profileId);
                    }
                }
            }
        }
        return localStringValue;
    }
    private volatile String stringValue;
    
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
     * 
     * @return array of the indexes of the values associated with the profile.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public abstract int[] getValueIndexes() throws IOException;
    
    /**
     * An array of the signature indexes associated with the profile.
     * 
     * @return An array of the signature indexes associated with the profile.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public abstract int[] getSignatureIndexes() throws IOException;
}
