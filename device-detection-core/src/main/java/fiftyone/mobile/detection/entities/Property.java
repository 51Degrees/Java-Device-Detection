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

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.util.*;
import java.util.Map;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.IReadonlyList;
import fiftyone.mobile.detection.cache.Cache;
import fiftyone.mobile.detection.entities.stream.FixedCacheList;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Encapsulates all the information about a property including how its
 * {@link Value values} should be used and what they mean. 
 * <p> 
 * Some properties are not mandatory and may not always contain values. 
 * For example: information concerning features of a television may not be 
 * applicable to a mobile phone. The {@link #isMandatory} data member should be 
 * checked before assuming a value will be returned. 
 * <p>
 * Properties can return none, one or many values. The {@link #isList} data 
 * member should be referred to to determine the number of values to expect. 
 * Properties where IsList is false will return a maximum of one value.
 * <p> 
 * {@link #getDescription()} can be used by UI developers to provide more information
 * about the intended use of the property and its values.
 * <p>
 * The {@link #getCategory()} method can be used to group together related 
 * properties in configuration UIs. 
 * <p> 
 * {@link #getValues()} returns {@link Values} instances that
 * provide various utility methods.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic. Use the relevant {@link Dataset} method to access these 
 * objects.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public class Property extends BaseEntity implements Comparable<Property> {

    /**
     * True if the property might have more than one value.
     */
    public final boolean isList;
    /**
     * True if the property must contain values.
     */
    public final boolean isMandatory;
    /**
     * True if the values the property returns are relevant to configuration
     * user interfaces and are suitable to be selected from a list of table of
     * options.
     */
    public final boolean showValues;
    /**
     * True if the property is relevant to be shown in a configuration user
     * interface where the property may appear in a list of options.
     */
    public final boolean show;
    /**
     * True if the property is marked as obsolete and will be removed from a
     * future version of the data set.
     */
    public final boolean isObsolete;
    /**
     * The order in which the property should appear in relation to others with
     * a position value set when used to create a display string for the profile
     * it is contained within.
     */
    public final byte displayOrder;
    /**
     * The index of the first value related to the property.
     */
    final int firstValueIndex;
    /**
     * @return an array of maps associated with the property.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String[] getMaps() throws IOException {
        String[] localMaps = maps;
        if (localMaps == null) {
            synchronized (this) {
                localMaps = maps;
                if (localMaps == null) {
                    String[] temp = new String[mapCount];
                    for(int i = 0; i < mapCount; i++) {
                        temp[i] = dataSet.maps.get(i + firstMapIndex).getName();
                    }
                    maps = localMaps = temp;
                }
            }
        }
        return localMaps;
    }
    @SuppressWarnings("VolatileArrayField")
    private volatile String[] maps;
    private final int mapCount;
    private final int firstMapIndex;

    /**
     * The name of the property to use when adding to Javascript as a property 
     * name. Unacceptable characters such as '/' are removed.
     * 
     * @return the name of the property when used in Javascript.
     * @throws IOException if there was a problem accessing data file.
     */
     @SuppressWarnings("DoubleCheckedLocking")
    public String getJavaScriptName() throws IOException {
        String localJavascriptName = javascriptName;
        if (localJavascriptName == null) {
            synchronized(this) {
                localJavascriptName = javascriptName;
                if (localJavascriptName == null) {
                    StringBuilder temp = new StringBuilder();
                    for(char character : getName().toCharArray()) {
                        if (Character.isLetterOrDigit(character)) {
                            temp.append(character);
                        }
                    }
                    javascriptName = localJavascriptName = temp.toString();
                }
            }
        }
        return localJavascriptName;
    }
    private volatile String javascriptName;
    
    /**
     * The name of the property.
     * 
     * @return name of the property.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getName() throws IOException {
        String localName = name;
        if (localName == null) {
            synchronized (this) {
                localName = name;
                if (localName == null) {
                    name = localName = 
                            getDataSet().strings.get(nameOffset).toString();
                }
            }
        }
        return localName;
    }
    private volatile String name;
    private final int nameOffset;
    
    /**
     * The strongly type data type the property returns.
     */
    public final PropertyValueType valueType;

    /**
     * The value the property returns if a strongly type value is not available.
     * 
     * @return {@link Value} the property returns if a strongly type value is 
     * not available.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Value getDefaultValue() throws IOException {
        Value localDefaultValue = defaultValue;
        if (localDefaultValue == null &&
            defaultValueIndex >= 0) {
            synchronized (this) {
                localDefaultValue = defaultValue;
                if (localDefaultValue == null) {
                    defaultValue = localDefaultValue = 
                            getDataSet().getValues().get(defaultValueIndex);
                }
            }
        }
        return localDefaultValue;
    }
    private volatile Value defaultValue;
    private final int defaultValueIndex;

    /**
     * The component the property relates to.
     * 
     * @return {A@link Component} the property relates to.
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
                            getDataSet().components.get(componentIndex);
                }
            }
        }
        return localComponent;
    }
    private volatile Component component;
    private final int componentIndex;

    /**
     * An array of values the property has available.
     *
     * @return array of {@link Values values} the property has available.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Values getValues() throws IOException {
        Values localValues = values;
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
    private volatile Values values;

    /**
     * Ensure that the values of this property have been initialized to point
     * to the profiles they are in.
     * @throws IOException
     */
    private void ensureValueProfilesSet() throws IOException {
        boolean localValueProfilesSet = valueProfilesSet;
        if (localValueProfilesSet == false) {
            synchronized (this) {
                localValueProfilesSet = valueProfilesSet;
                if (localValueProfilesSet == false) {

                    // If the Values list is cached increase the size
                    // of the cache to improve performance for this
                    // feature by storing all related values in the cache.
                    // Having all the possible values cached will improve
                    // performance for subsequent requests. if the data
                    // set isn't cached then there will only be one instance
                    // of each profile and value in memory so the step isn't
                    // needed as the direct reference will be used.
                    if (dataSet.values instanceof FixedCacheList
                            && values != null)
                    {
                        ((FixedCacheList)dataSet.values).increaseCacheSize(values.count());
                    }

                    // A map of value indexes to the profiles that contain those values.
                    Map<Integer, List<Integer>> valueIndexProfileIndexMap = new HashMap<Integer, List<Integer>>();
                    for (Value value : getValues().getAll()) {
                        valueIndexProfileIndexMap.put(value.index, new ArrayList<Integer>());
                    }

                    // Add all the profile indexes to the map.
                    for (Profile profile : getComponent().getProfiles()) {
                        for (Value value : profile.getValues(this).getAll()) {
                            valueIndexProfileIndexMap.get(value.index).add(profile.getIndex());
                        }
                    }

                    // Set the profile indexes in the property.
                    for (int valueIndex : valueIndexProfileIndexMap.keySet()) {
                        Value value = dataSet.values.get(valueIndex);
                        value.setProfileIndexes(valueIndexProfileIndexMap.get(valueIndex));
                    }
                    valueProfilesSet = true;
                }
            }
        }
    }

    /**
     * Check to see if the property has initialized the linkage to profiles that refer to it.
     */
    public boolean isValueProfilesSet() {
        return valueProfilesSet;
    }

    // guard to show whether the profile pointers have been set up on the values for this property
    private volatile boolean valueProfilesSet = false;

    /**
     * Find the {@link Profile Profiles} that contain the stated value for this property.
     * @param valueName the value to check for
     * @param filterProfiles limit search to the profiles passed (null for all profiles)
     * @return a list of profiles that contain the value or an empty list if there are none
     * @throws IOException
     */
    public List<Profile> findProfiles(String valueName, List<Profile> filterProfiles) throws IOException {
        List<Profile> result = new ArrayList<Profile>();

        // makes sure values have had their profiles initialized
        ensureValueProfilesSet();

        if (valueName != null) {
            Value value = this.values.get(valueName);
            if (value != null) {
                if (filterProfiles == null) {
                    for (Integer profileIndex : value.getProfileIndexes()) {
                        result.add(dataSet.profiles.get(profileIndex));
                    }
                } else {
                    for (Profile profile : filterProfiles) {
                        if (value.getProfileIndexes().contains(profile.getIndex())) {
                            result.add(profile);
                        }
                    }
                }
            }
        }
        // Return an unmodifiable list.
        return Collections.unmodifiableList(result);
    }

    /**
     * A description of the property suitable to be displayed to end users via a
     * user interface.
     *
     * @return description of the property suitable to be displayed to end users 
     * via a user interface.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getDescription() throws IOException {
        String localDescription = description;
        if (localDescription == null && descriptionOffset >= 0) {
            synchronized (this) {
                localDescription = description;
                if (localDescription == null) {
                    description = localDescription = 
                            getDataSet().strings.get(descriptionOffset).toString();
                }
            }
        }
        return localDescription;
    }
    private volatile String description;
    private final int descriptionOffset;

    /**
     * The category the property relates to within the data set. A category is 
     * not the same as the component.
     *
     * @return name of category the property relates to within the data set.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getCategory() throws IOException {
        String localCategory = category;
        if (localCategory == null && categoryOffset >= 0) {
            synchronized (this) {
                localCategory = category;
                if (localCategory == null) {
                    category = localCategory = 
                            getDataSet().strings.get(categoryOffset).toString();
                }
            }
        }
        return localCategory;
    }
    private volatile String category;
    private final int categoryOffset;

    /**
     * A URL to more information about the property.
     *
     * @return URL to more information about the property.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public URL getUrl() throws IOException {
        URL localUrl = url;
        if (localUrl == null && urlOffset >= 0) {
            synchronized (this) {
                localUrl = url;
                if (localUrl == null) {
                    try {
                        url = localUrl = 
                                new URL(getDataSet().strings.get(urlOffset).
                                toString());
                    } catch (MalformedURLException e) {
                        url = localUrl = null;
                    }
                }
            }
        }
        return localUrl;
    }
    private volatile URL url;
    private final int urlOffset;
    private final int lastValueIndex;

    /**
     * @return The index of the last value related to the property.
     */
    public int getLastIndexValue() {
        return lastValueIndex;
    }
    
    /**
     * Constructs a new instance of Property
     *
     * @param dataSet {@link Dataset} to construct from.
     * @param index property index.
     * @param reader BinaryReader to be used.
     * @throws IOException if there was a problem accessing data file.
     */
    public Property(Dataset dataSet, int index, BinaryReader reader) 
                                                            throws IOException {
        super(dataSet, index);
        this.componentIndex = reader.readByte();
        this.displayOrder = reader.readByte();
        this.isMandatory = reader.readBoolean();
        this.isList = reader.readBoolean();
        this.showValues = reader.readBoolean();
        this.isObsolete = reader.readBoolean();
        this.show = reader.readBoolean();
        this.valueType = PropertyValueType.create(reader.readByte());
        this.defaultValueIndex = reader.readInt32();
        this.nameOffset = reader.readInt32();
        this.descriptionOffset = reader.readInt32();
        this.categoryOffset = reader.readInt32();
        this.urlOffset = reader.readInt32();
        this.firstValueIndex = reader.readInt32();
        this.lastValueIndex = reader.readInt32();
        this.mapCount = reader.readInt32();
        this.firstMapIndex = reader.readInt32();

        this.valueProfilesSet = false;
    }

    /**
     * Initialises the often used lists and references if storing of object
     * references is enabled.
     * <p>
     * This method should not be called as it is part of the internal logic.
     *
     * @throws IOException if there was a problem accessing data file.
     */
    public void init() throws IOException {
        getValues();
        component = getDataSet().getComponents().get(componentIndex);
        getDescription();
        getName();
        getCategory();
        getUrl();
    }

    /**
     * Returns the values which reference the property by starting at the first
     * value index and moving forward until a new property is found.
     *
     * @return A values list initialised with the property values
     * @throws IOException
     */
    private Values doGetValues() throws IOException {
        Value[] tempValues = new Value[lastValueIndex - firstValueIndex + 1];
        for (int i = firstValueIndex, v = 0; i <= lastValueIndex; i++, v++) {
            tempValues[v] = dataSet.getValues().get(i);
        }
        return new Values(this, tempValues);
    }

    /**
     * Compares this property to another using the index field if they're in the
     * same list, otherwise the name field.
     *
     * @param other {@link Property} to be compared against.
     * @return Indication of relative value.
     */
    @Override
    public int compareTo(Property other) {
        if (getDataSet() == other.getDataSet()) {
            return getIndex() - other.getIndex();
        }
        try {
            return getName().compareTo(other.getName());
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * A string representation of the property.
     *
     * @return the property name as a string.
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
     * Enumeration of strongly typed property values which could be returned.
     */
    public enum PropertyValueType {

        STRING, INT, DOUBLE, BOOL, JAVASCRIPT;

        public static PropertyValueType create(byte b) {
            switch (b) {
                case 0:
                    return STRING;
                case 1:
                    return INT;
                case 2:
                    return DOUBLE;
                case 3:
                    return BOOL;
                case 4:
                    return JAVASCRIPT;
            }
            throw new IllegalArgumentException("Unknown property type: " + b);
        }
    }
}