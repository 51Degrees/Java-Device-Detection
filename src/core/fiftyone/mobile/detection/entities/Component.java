package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
 * Every device can be split into the major components of hardware, operating
 * system and browser. The properties and values associated with these
 * components are accessed via this class. <p> As there are a small number of
 * components they are always held in memory. <p> For more information see
 * http://51degrees.mobi/Support/Documentation/Java
 */
/**
 * Every device can be split into the major components of hardware, operating
 * system and browser. These the properties and values associated with these
 * components are represented via this class.
 */
public class Component extends BaseEntity implements Comparable<Component> {

    /**
     * The unique Id of the component. Does not change between different data
     * sets.
     */
    private final int componentId;

    /**
     * The unique name of the component.
     * @return unique name of the component
     * @throws java.io.IOException indicates an I/O exception occurred 
     */
    public String getName() throws IOException {
        if (name == null) {
            synchronized (this) {
                if (name == null) {
                    name = getDataSet().strings.get(nameOffset).toString();
                }
            }
        }
        return name;
    }
    private String name;
    private final int nameOffset;

    /**
     * Array of properties the component relates to.
     *
     * @return array of properties the component relates to.
     * @throws IOException indicates an I/O exception occurred
     */
    public Property[] getProperties() throws IOException {
        if (properties == null) {
            synchronized (this) {
                if (properties == null) {
                    properties = GetProperties();
                }
            }
        }
        return properties;
    }
    private Property[] properties;

    /**
     * An array of the profiles.
     * @return an array of the profiles.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Profile[] getProfiles() throws IOException {
        if (profiles == null) {
            synchronized (this) {
                if (profiles == null) {
                    profiles = GetProfiles();
                }
            }
        }
        return profiles;
    }
    private Profile[] profiles;

    /**
     * The default profile that should be returned for the component.
     * @return default profile that should be returned for the component
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Profile getDefaultProfile() throws IOException {
        if (defaultProfile == null) {
            synchronized (this) {
                if (defaultProfile == null) {
                    defaultProfile = getDataSet().getProfiles().get(
                            defaultProfileOffset);
                }
            }
        }
        return defaultProfile;
    }
    private Profile defaultProfile;
    private final int defaultProfileOffset;

    /**
     * Constructs a new instance of Component
     *
     * @param dataSet The data set whose components list the component is
     * contained within
     * @param index Index of the component within the list
     * @param reader the BinaryReader object to be used
     */
    public Component(Dataset dataSet, int index, BinaryReader reader) {
        super(dataSet, index);
        componentId = reader.readByte();
        nameOffset = reader.readInt32();
        defaultProfileOffset = reader.readInt32();
    }

    /**
     * Initialises the references to profiles.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public void init() throws IOException {
        defaultProfile = getDataSet().getProfiles().get(defaultProfileOffset);
        profiles = GetProfiles();
    }

    /**
     * Returns an array of the properties associated with the component.
     *
     * @return
     */
    private Property[] GetProperties() throws IOException {
        List<Property> properties = new ArrayList<Property>();
        for (Property property : getDataSet().getProperties()) {
            if (property.getComponent().getComponentId() == componentId) {
                properties.add(property);
            }
        }
        return properties.toArray(new Property[properties.size()]);
    }

    /**
     * Returns an array of all the profiles that relate to this component.
     *
     * @return
     */
    private Profile[] GetProfiles() throws IOException {
        List<Profile> profiles = new ArrayList<Profile>();
        for (Profile profile : getDataSet().getProfiles()) {
            for (Value value : profile.getValues()) {
                if (value.getComponent().getComponentId() == componentId) {
                    profiles.add(profile);
                    continue;
                }
            }
        }
        return profiles.toArray(new Profile[profiles.size()]);
    }

    /**
     * Compares this component to another using the numeric ComponentId field.
     *
     * @param other The component to be compared against
     * @return Indication of relative value based on ComponentId field
     */
    public int compareTo(Component other) {
        return getComponentId() - other.getComponentId();
    }

    public int getComponentId() {
        return componentId;
    }

    @Override
    public String toString() {
        try {
            return this.getName();
        } catch (IOException e) {
            return null;
        }
    }
}