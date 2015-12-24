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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.WrappedIOException;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.mobile.detection.search.SearchArrays;
import java.util.Collections;

/**
 * A value associated with a property and component within the dataset. 
 * <p>
 * Every {@link Property} can return one of many values, or multiple values 
 * if it's a list property. For example: SupportedBearers returns a list of 
 * the bearers that the device can support. 
 * <p> 
 * Class contains metadata related to this value including the display name, 
 * description and URL to find out additional information. Metadata can be used 
 * by UI developers to provide users with more information about the meaning and 
 * intended use of this value. Access metadata like:
 * {@code value.getDescription();} and {@code value.getUrl();}.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic. Use the relevant {@link Dataset} method to access these 
 * objects.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public class Value extends BaseEntity {
    
    /**
     * The value as an integer. Integer object instead of integer primitive 
     * because of the nullable requirement.
     */
    private volatile Integer asInt;
    
    /**
     * The length in bytes of the value record in the data file.
     */
    public static final int RECORD_LENGTH = (4 * 3) + 2;

    /**
     * Used to find profiles with values that include this one.
     */
    private static final SearchValues valuesIndexSearch = new SearchValues();
    
    /**
     * @return The name of the value as string.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getName() throws IOException {
        String localName = name;
        if (localName == null) {
            synchronized (this) {
                localName = name;
                if (localName == null) {
                    name = localName = 
                            getDataSet().strings.get(nameIndex).toString();
                }
            }
        }
        return localName;
    }
    private volatile String name;
    private final int nameIndex;

    /**
     * @return array containing the {@link Signature signatures} that the value 
     *         is associated with.
     * @throws IOException if there was a problem accessing data file.
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
     * @return array containing the {@link Profile profiles} this value is 
     *         associated with.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Profile[] getProfiles() throws IOException {
        Profile[] localProfiles = profiles;
        if (localProfiles == null) {
            synchronized (this) {
                localProfiles = profiles;
                if (localProfiles == null) {
                    profiles = localProfiles = doGetProfiles();
                }
            }
        }
        return localProfiles;
    }
    @SuppressWarnings("VolatileArrayField")
    private volatile Profile[] profiles;

    /**
     * @return The {@link Property} the value relates to.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Property getProperty() throws IOException {
        Property localProperty = property;
        if (localProperty == null) {
            synchronized (this) {
                localProperty = property;
                if (localProperty == null) {
                    property = localProperty = getDataSet().getProperties().get(propertyIndex);
                }
            }
        }
        return localProperty;
    }
    private volatile Property property;
    final int propertyIndex;

    /**
     * @return The {@link Component} the value relates to.
     * @throws IOException if there was a problem accessing data file.
     */
    public Component getComponent() throws IOException {
        return getProperty().getComponent();
    }

    /**
     * @return a description of the value suitable to be displayed to end users
     *         via a user interface.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getDescription() throws IOException {
        String localDescription = description;
        if (descriptionIndex >= 0 && localDescription == null) {
            synchronized (this) {
                localDescription = description;
                if (localDescription == null) {
                    description = localDescription = getDataSet().strings
                            .get(descriptionIndex).toString();
                }
            }
        }
        return localDescription;
    }
    private volatile String description;
    private final int descriptionIndex;

    /**
     * @return A URL to more information about the value if present.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public URL getUrl() throws IOException {
        URL localUrl = url;
        if (urlIndex >= 0 && localUrl == null) {
            synchronized (this) {
                localUrl = url;
                if (localUrl == null) {
                    url = localUrl = new URL(getDataSet().strings.get(urlIndex)
                            .toString());
                }
            }
        }
        return localUrl;
    }
    private volatile URL url;
    private final int urlIndex;

    /**
     * Constructs a new instance of Value.
     *
     * @param dataSet the {@link Dataset} the value is contained within.
     * @param index the index in the data structure to the value.
     * @param reader Reader connected to the source data structure and
     *        positioned to start reading.
     */
    public Value(Dataset dataSet, int index, BinaryReader reader) {
        super(dataSet, index);

        this.propertyIndex = reader.readInt16();
        this.nameIndex = reader.readInt32();
        this.descriptionIndex = reader.readInt32();
        this.urlIndex = reader.readInt32();
    }

    /**
     * Called after the entire data set has been loaded to ensure any further
     * initialisation steps that require other items in the data set can be
     * completed. The Profiles and Signatures are not initialised as they are
     * very rarely used and take a long time to initialise.
     * <p>
     * This method should not be called as it is part of the internal logic.
     *
     * @throws IOException if there was a problem accessing data file.
     */
    public void init() throws IOException {
        name = getDataSet().strings.get(nameIndex).toString();
        property = getDataSet().getProperties().get(propertyIndex);
        if (descriptionIndex >= 0) {
            description = getDataSet().strings.get(descriptionIndex)
                    .toString();
        }
        if (urlIndex >= 0) {
            try {
                url = new URL(getDataSet().strings.get(urlIndex)
                        .toString());
            } catch (MalformedURLException e) {
                url = null;
            }
        }
    }

    /**
     * Compares two values by name.
     * 
     * @param value name of value to compare the current value against.
     * @return integer representing difference.
     */
    public int compareTo(String value) {
        try {
            return getName().compareTo(value);
        } catch (IOException ex) {
            throw new WrappedIOException(ex.getMessage());
        }
    }
    
    /**
     * Compares this value to another using the index field if they're in the
     * same list other wise the name value.
     *
     * @param other The value to be compared against.
     * @return Indication of relative value based on index field.
     */
    public int compareTo(Value other) {
        if (getDataSet() == other.getDataSet()) {
            return getIndex() - other.getIndex();
        }
        try {
            return getName().compareTo(other.getName());
        } catch (IOException ex) {
            throw new WrappedIOException(ex.getMessage());
        }
    }

    /**
     * @return the value name as a string.
     */
    @Override
    public String toString() {
        try {
            return getName();
        } catch (IOException e) {
            return super.toString();
        }
    }

    /**
     * @return Returns the value as a number.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public double toDouble() throws IOException {
        Double localAsNumber = asNumber;
        if (localAsNumber == null) {
            synchronized (this) {
                localAsNumber = asNumber;
                if (localAsNumber == null) {
                    try {
                        asNumber = localAsNumber = 
                                Double.parseDouble(getName());
                    } catch (NumberFormatException e) {
                        if (this != getProperty().getDefaultValue()) {
                            asNumber = localAsNumber = 
                                    getProperty().getDefaultValue().toDouble();
                        } else {
                            asNumber = localAsNumber = (double) 0;
                        }
                    }
                }
            }
        }
        return (double) localAsNumber;
    }
    private volatile Double asNumber;

    /**
     * @return Returns the value as a boolean.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public boolean toBool() throws IOException {
        Boolean localAsBool = asBool;
        if (localAsBool == null) {
            synchronized (this) {
                localAsBool = asBool;
                if (localAsBool == null) {
                    asBool = localAsBool = Boolean.parseBoolean(getName());
                }
            }
        }
        return (boolean) localAsBool;
    }
    private volatile Boolean asBool;

    /**
     * Returns true if the value is the null value for the property. If the
     * property has no null value false is always returned.
     *
     * @return true if the value is the null value for the property. If the
     *         property has no null value false is always returned.
     * @throws IOException if there was a problem accessing data file.
     */
    public boolean getIsDefault() throws IOException {
        Value defaultValue = property.getDefaultValue();
        if (defaultValue != null) {
            return this.getName().equals(defaultValue.getName());
        }
        return false;
    }
    
    /**
     * Returns the value as an integer.
     * 
     * @return If the value can not convert to an integer and the value is not 
     *         equal to the null value then the null value for the property 
     *         will be used. If no conversion is possible 0 is returned.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public int toInt() throws IOException {
        Integer localAsInt = asInt;
        if (localAsInt == null) {
            synchronized (this) {
                localAsInt = asInt;
                if (localAsInt == null) {
                    Double d = toDouble();
                    asInt = localAsInt = d.intValue();
                }
            }
        }
        return localAsInt;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Private methods">
    /**
     * Gets all the profiles associated with the value.
     *
     * @return the profiles from the component that relate to this value
     * @throws IOException if there was a problem accessing data file.
     */
    private Profile[] doGetProfiles() throws IOException {
        List<Profile> list = new ArrayList<Profile>();

        for (Profile profile : getComponent().getProfiles()) {
            if (valuesIndexSearch.binarySearch(
                    profile.getValues(), 
                    getIndex()) >= 0) {
                list.add(profile);
            }
        }

        return list.toArray(new Profile[list.size()]);
    }
    
    /**
     * Gets all the signatures associated with the value.
     *
     * @return Returns the signatures associated with the value
     * @throws IOException if there was a problem accessing data file.
     */
    private Signature[] doGetSignatures() throws IOException {
        // Get a distinct list of signature indexes.
        List<Integer> list = new ArrayList<Integer>();
        for (Profile profile : getProfiles()) {
            for (Integer signatureIndex : profile.getSignatureIndexes()) {
                int localIndex = Collections.binarySearch(list, signatureIndex);
                if (localIndex < 0) {
                    list.add(~localIndex, signatureIndex);
                }
            }
        }
        // Turn that list into an array of signatures.
        Signature[] result = new Signature[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getDataSet().getSignatures().get(list.get(i));
        }
        return result;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Private static class for binary search access.">
    /**
     * Provides access to binary search and overrides the compareTo method.
     */
    private static class SearchValues extends SearchArrays<Value, Integer> {
        @Override
        public int compareTo(Value item, Integer key) {
            return item.compareTo(key);
        }
    }
    // </editor-fold>
}
