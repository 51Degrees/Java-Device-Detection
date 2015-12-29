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
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Every device can be split into the major components of hardware, operating
 * system, crawler and browser. Each component has a number of properties and 
 * profiles assigned to it.
 * <p>
 * Components are always held in memory as there is only a small number of them.
 * <p>
 * Full list of components in the data set can be retrieved like:
 * {@code dataSet.getComponents();}. Data set also provides a way to retrieve a 
 * specific component:
 * <ul>
 *  <li>Hardware component: {@code dataSet.getHardware();}
 *  <li>Software component: {@code dataSet.getSoftware();}
 *  <li>Browser component: {@code dataSet.getBrowsers();}
 *  <li>Crawler component: {@code dataSet.getCrawlers();}
 * </ul>
 * <p>
 * Component can be used to retrieve {@link #getProfiles() profiles} and 
 * {@link #getProperties() properties}.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic. Use the relevant {@link Dataset} method to access these 
 * objects.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public abstract class Component extends BaseEntity 
                                implements Comparable<Component> {
    /**
     * Offset for the default profile that should be returned for the component.
     */
    private final int defaultProfileOffset;
    
    /**
     * Offset for the unique name of the component.
     */
    private final int nameOffset;

    /**
     * Constructs a new instance of Component
     *
     * @param dataSet The data set whose components list the component is
     * contained within.
     * @param index Index of the component within the list.
     * @param reader the BinaryReader object to be used.
     */
    public Component(Dataset dataSet, int index, BinaryReader reader) {
        super(dataSet, index);
        componentId = reader.readByte();
        nameOffset = reader.readInt32();
        defaultProfileOffset = reader.readInt32();
    }
    
    /**
     * Compares this component to another using the numeric ComponentId field.
     *
     * @param other The component to be compared against.
     * @return Indication of relative value based on ComponentId field.
     */
    @Override
    public int compareTo(Component other) {
        return getComponentId() - other.getComponentId();
    }
    
    /**
     * The default profile that should be returned for the component.
     * 
     * @return default profile that should be returned for the component.
     * @throws java.io.IOException If there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Profile getDefaultProfile() throws IOException {
        Profile localDefaultProfile = defaultProfile;
        if (localDefaultProfile == null) {
            synchronized (this) {
                localDefaultProfile = defaultProfile;
                if (localDefaultProfile == null) {
                    defaultProfile = localDefaultProfile = 
                            getDataSet().getProfiles().get(defaultProfileOffset);
                }
            }
        }
        return localDefaultProfile;
    }
    private volatile Profile defaultProfile;
    
    /**
     * An array of profiles associated with the component.
     * 
     * @return an array of the {@link Profile profiles}.
     * @throws java.io.IOException if there was a problem accessing data file.
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
     * Array of properties the component relates to.
     *
     * @return array of {@link Property properties} the component relates to.
     * @throws IOException if there was a problem accessing data file.
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
     * Initialises the references to profiles.
     * This method should not be called as it is part of the internal logic.
     * 
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public void init() throws IOException {
        if (name == null)
            name = getName();
        if (defaultProfile == null)
            defaultProfile = getDataSet().getProfiles().get(defaultProfileOffset);
        if (profiles == null)
            profiles = doGetProfiles();
    }
    
    /**
     * The unique name of the component as a string.
     * 
     * @return unique name of the component as a string.
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

    /**
     * Returns an array of all the profiles that relate to this component.
     * @return An array of profiles associated with the component.
     */
    private Profile[] doGetProfiles() throws IOException {
        List<Profile> temp = new ArrayList<Profile>();
        for (Profile profile : getDataSet().getProfiles()) {
            if (profile.getComponent().getComponentId() == componentId) {
                temp.add(profile);
            }
        }
        return temp.toArray(new Profile[temp.size()]);
    }
    
    /**
     * Returns an array of the properties associated with the component.
     * @return An array of the properties associated with the component.
     */
    private Property[] doGetProperties() throws IOException {
        List<Property> tempProperties = new ArrayList<Property>();
        for (Property property : getDataSet().getProperties()) {
            if (property.getComponent().getComponentId() == componentId) {
                tempProperties.add(property);
            }
        }
        return tempProperties.toArray(new Property[tempProperties.size()]);
    }

    /**
     * The unique Id of the component. Does not change between different data
     * sets. Consists of four {@link Profile} IDs.
     * 
     * @return The unique Id of the component.
     */
    public int getComponentId() {
        return componentId;
    }
    private final int componentId;

    /**
     * Returns the components name.
     * 
     * @return Returns the components name.
     */
    @Override
    public String toString() {
        try {
            return this.getName();
        } catch (IOException e) {
            return "Null";
        }
    }
    
    /**
     * List of HTTP headers that should be checked in order to perform a 
     * detection where more headers than User-Agent are available. 
     * This data is used by methods that can HTTP Header collections.
     * 
     * @return List of HTTP headers that should be checked in order to perform  
     * detection.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public abstract String[] getHttpheaders() throws IOException;
    
    /**
     * Compares this instance to another using the component Id.
     * 
     * @param other The component to be compared against.
     * @return True if {@link #componentId} of this component is equal to the 
     *         ComponentId of the other component.
     */
    public boolean equals(Component other) {
        return getComponentId() == other.getComponentId();
    }
}