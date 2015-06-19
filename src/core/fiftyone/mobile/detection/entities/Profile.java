package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.SortedList;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
public abstract class Profile extends BaseEntity implements Comparable<Profile> {
    /**
     * The component the profile belongs to.
     */
    private Component component;
    /**
     * Index of the component the profile belongs to.
     */
    private final int componentIndex;
    /**
     * List of Values associated with the property name.
     */
    private SortedList<String, Values> nameToValues;
    /**
     * Array of Properties associated with the profile.
     */
    private Property[] properties;
    /**
     * List of Property indexes wit the corresponding list of values.
     */
    private SortedList<Integer, Values> propertyIndexToValues;
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
    private Signature[] signatures;
    /**
     * An array of the signature indexes associated with the profile.
     * @abstract
     */
    protected int[] signatureIndexes;
    /**
     * A string representation of the profile.
     */
    private String stringValue;
    /**
     * An array of values associated with the profile.
     */
    private Value[] values;
    /**
     * Unique Id of the profile. Does not change between different data sets.
     */
    public final int profileId;
    /**
     * A list of the indexes of the values associated with the profile.
     * @abstract
     */
    protected int[] valueIndexes;
    
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
        if (component == null) {
            synchronized (this) {
                if (component == null) {
                    component = getDataSet().getComponents().get(componentIndex);
                }
            }
        }
        return component;
    }
    
    /**
     * Returns the month name as an integer. The else-if implementation instead 
     * of switch is to ensure backwards compatibility with 1.6.
     * @param month Name of the month, i.e. January.
     * @return The integer representation of the month.
     */
    private static int getMonthAsInt(String month) {
        if (month.toLowerCase().equals("january"))
            return 1;
        else if(month.toLowerCase().equals("february"))
            return 2;
        else if(month.toLowerCase().equals("march"))
            return 3;
        else if(month.toLowerCase().equals("april"))
            return 4;
        else if(month.toLowerCase().equals("may"))
            return 5;
        else if(month.toLowerCase().equals("june"))
            return 6;
        else if(month.toLowerCase().equals("july"))
            return 7;
        else if(month.toLowerCase().equals("august"))
            return 8;
        else if(month.toLowerCase().equals("september"))
            return 9;
        else if(month.toLowerCase().equals("october"))
            return 10;
        else if(month.toLowerCase().equals("november"))
            return 11;
        else if(month.toLowerCase().equals("december"))
            return 12;
        else
            throw new Error("Month name does not appear to be valid. Expecting "
                    + "a full month name i.e. january.");
    }
    
    /**
     * Returns an array of properties the profile relates to.
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
    
    /**
     * Gets the signatures related to the profile.
     * @return Array of signatures associated with the profile.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Signature[] getSignatures() throws IOException {
        if (signatures == null) {
            synchronized (this) {
                if (signatures == null) {
                    signatures = doGetSignatures();
                }
            }
        }
        return signatures;
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
        if (values == null) {
            synchronized (this) {
                if (values == null) {
                    values = doGetValues();
                }
            }
        }
        return values;
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
     * @return 
     */
    private int getValuesIndex(int valueIndex) {
        return getValuesIndex(valueIndex, 0);
    }
    
    /**
     * 
     * @param valueIndex
     * @param lower
     * @return 
     */
    private int getValuesIndex(int valueIndex, int lower) {
        int upper = valueIndexes.length - 1;
        int middle = 0;
        
        while(lower <= upper) {
            middle = lower + (upper - lower) / 2;
            if (valueIndexes[middle] == 0) {
                return middle;
            } else if(valueIndexes[middle] == 0) {
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
     * the data set can be completed.Called after the entire data set has been loaded to ensure 
     * any further initialisation steps that require other items in
     * the data set can be completed.
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
     * 
     * @return 
     */
    private SortedList<Integer, Values> getPropertyIndexToValues() {
        if (propertyIndexToValues == null) {
            synchronized(this) {
                if (propertyIndexToValues == null) {
                    propertyIndexToValues = new SortedList<Integer, Values>();
                }
            }
        }
        return propertyIndexToValues;
    }
    
    /**
     * The release date of the profile if it's a hardware profile.
     * @return The release date of the profile if it's a hardware profile.
     * @throws IOException 
     */
    private Date getReleaseDate() throws IOException {
        String releaseMonth = null;
        String releaseYear = null;
        if (!releaseDateChecked) {
            synchronized(this) {
                if (!releaseDateChecked) {
                    for(Value v : values) {
                        if (v.getProperty().getName().equals("ReleaseMonth")) {
                            releaseMonth = v.getName();
                        }
                    }
                    for(Value v : values) {
                        if (v.getProperty().getName().equals("ReleaseYear")) {
                            releaseYear = v.getName();
                        }
                    }
                    if (releaseMonth != null && releaseYear != null) {
                        int monthValue = getMonthAsInt(releaseMonth);
                        int yearValue = Integer.valueOf(releaseYear);
                        Calendar calendar = Calendar.getInstance();
                        calendar.clear();
                        calendar.set(Calendar.MONTH, monthValue);
                        calendar.set(Calendar.YEAR, yearValue);
                        releaseDate = calendar.getTime();
                    }
                    releaseDateChecked = true;
                }
            }
        }
        return releaseDate;
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
     * A array of the indexes of the values associated with the profile.
     * @return A array of the indexes of the values associated with the profile.
     */
    public abstract int[] getValueIndexes();
    
    /**
     * An array of the signature indexes associated with the profile.
     * @return An array of the signature indexes associated with the profile.
     */
    public abstract int[] getSignatureIndexes();
}
