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
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Encapsulates all the information about a property including how it's values
 * should be used and what they mean. <p> Some properties are not mandatory and
 * may not always contain values. For example; information concerning features
 * of a television may not be applicable to a mobile phone. The IsMandatory
 * property should be checked before assuming a value will be returned. <p>
 * Properties can return none, one or many values. The IsList property should be
 * referred to to determine the number of values to expect. Properties where
 * IsList is false will only return up to one value. <p> The property also
 * provides other information about the intended use of the property. The
 * Description can be used by UI developers to provide more information about
 * the intended use of the property and it's values. The Category property can
 * be used to group together related properties in configuration UIs. <p> Values
 * are returned in the type Values which includes utility methods to easily
 * extract strongly typed values. <p> For more information see
 * http://51degrees.mobi/Support/Documentation/Java
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
     * @throws java.io.IOException
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
     * name. Unacceptable characters such as / are removed.
     * @return the name of the property when used in javascript.
     * @throws IOException 
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
     * @return name of the property
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getName() throws IOException {
        String localName = name;
        if (localName == null) {
            synchronized (this) {
                localName = name;
                if (localName == null) {
                    name = localName = getDataSet().strings.get(nameOffset).toString();
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
     * @return value the property returns if a strongly type value is not available
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Value getDefaultValue() throws IOException {
        Value localDefaultValue = defaultValue;
        if (localDefaultValue == null &&
            defaultValueIndex >= 0) {
            synchronized (this) {
                localDefaultValue = defaultValue;
                if (localDefaultValue == null) {
                    defaultValue = localDefaultValue = getDataSet().getValues().get(defaultValueIndex);
                }
            }
        }
        return localDefaultValue;
    }
    private volatile Value defaultValue;
    private final int defaultValueIndex;

    /**
     * The component the property relates to.
     * @return component the property relates to
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Component getComponent() throws IOException {
        Component localComponent = component;
        if (localComponent == null) {
            synchronized (this) {
                localComponent = component;
                if (localComponent == null) {
                    component = localComponent = getDataSet().components.get(componentIndex);
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
     * @return array of values the property has available
     * @throws IOException indicates an I/O exception occurred
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
     * A description of the property suitable to be displayed to end users via a
     * user interface.
     *
     * @return description of the property suitable to be displayed to end users 
     * via a user interface.
     * @throws IOException indicates an I/O exception occurred
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getDescription() throws IOException {
        String localDescription = description;
        if (localDescription == null && descriptionOffset >= 0) {
            synchronized (this) {
                localDescription = description;
                if (localDescription == null) {
                    description = localDescription = getDataSet().strings
                            .get(descriptionOffset).toString();
                }
            }
        }
        return localDescription;
    }
    private volatile String description;
    private final int descriptionOffset;

    /**
     * The category the property relates to within the data set.
     *
     * @return category the property relates to within the data set
     * @throws IOException indicates an I/O exception occurred
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getCategory() throws IOException {
        String localCategory = category;
        if (localCategory == null && categoryOffset >= 0) {
            synchronized (this) {
                localCategory = category;
                if (localCategory == null) {
                    category = localCategory = getDataSet().strings.get(categoryOffset)
                            .toString();
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
     * @return URL to more information about the property
     * @throws IOException indicates an I/O exception occurred
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public URL getUrl() throws IOException {
        URL localUrl = url;
        if (localUrl == null && urlOffset >= 0) {
            synchronized (this) {
                localUrl = url;
                if (localUrl == null) {
                    try {
                        url = localUrl = new URL(getDataSet().strings.get(urlOffset)
                                .toString());
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
     * @param dataSet data set to construct from
     * @param index property index
     * @param reader BinaryReader to be used
     * @throws IOException indicates an I/O exception occurred
     */
    public Property(Dataset dataSet, int index, BinaryReader reader) throws IOException {
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
    }

    /**
     * Initialises the often used lists and references if storing of object
     * references is enabled.
     *
     * @throws IOException indicates an I/O exception occurred
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
     * @param other The property to be compared against
     * @return Indication of relative value
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
     * @return the property name as a string
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