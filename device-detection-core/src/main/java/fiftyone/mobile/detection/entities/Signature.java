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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.SortedList;
import fiftyone.mobile.detection.WrappedIOException;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Signature of a User-Agent - the relevant characters from a User-Agent 
 * structured in a manner to enable rapid comparison with a target User-Agent. 
 * <p> 
 * A signature contains those characters of a User-Agent which are relevant 
 * for the purposes of device detection. For example: most User-Agents will 
 * start with "Mozilla" and therefore these characters are of very little use 
 * when detecting devices. Other characters such as those that represent the 
 * model of the hardware are very relevant. 
 * <p>
 * A signature contains both an array of relevant characters from User-Agents
 * identified when the data was created and the unique complete node identifies
 * of relevant sub strings contained in multiple signatures and User-Agents.
 * Together this information is used at detection time to rapidly identify the
 * signature matching a target User-Agent. 
 * <p> 
 * Signatures relate to device {@link Property properties} via 
 * {@link Profile profiles}. Each signature relates to one profile for each
 * {@link Component} type.
 * <p> 
 * Unlike other entities
 * the signature may have a varying number of nodes and profiles associated with
 * it depending on the data set. All signatures within a data set will have the
 * same number of profiles and nodes associated with them all. As these can
 * change across data sets they can't be included in the source code.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic. Use the relevant {@link Dataset} method to access these 
 * objects.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public abstract class Signature extends BaseEntity 
                                implements Comparable<Signature> {
    
    /**
     * Offsets to profiles associated with the signature.
     */
    private final int[] profileOffsets;
    
    /**
     * Constructs a new instance of Signature
     *
     * @param dataSet the {@link Dataset} the signature is contained within.
     * @param index the index in the data structure to the signature.
     * @param reader Reader connected to the source data structure and
     * positioned to start reading.
     */
    public Signature(Dataset dataSet, int index, BinaryReader reader) {
        super(dataSet, index);
        // This has been changed as readOffsets no longer returns an array.
        // TODO: verify logic.
        List<Integer> t = readOffsets(dataSet, reader, 
                                    dataSet.signatureProfilesCount);
        profileOffsets = new int[t.size()];
        Iterator<Integer> iter = t.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            profileOffsets[i] = iter.next();
        }
    }
    
    /**
     * A hash map relating the index of a property to the values returned 
     * by the signature. Used to speed up subsequent data processing.
     * @return dictionary relating the index of a property to the values returned 
     * by the signature.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    private SortedList<Integer, Values> getPropertyIndexToValues() {
        SortedList<Integer, Values> localPropertyIndexToValues;
        localPropertyIndexToValues = propertyIndexToValues;
        if (localPropertyIndexToValues == null) {
            synchronized (this) {
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
     * A hash map relating the name of a property to the values returned by 
     * the signature. Used to speed up subsequent data processing.
     * @return a hash map with values mapped to specific property.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    private SortedList<String, Values> getPropertyNameToValues() {
        SortedList<String, Values> localPropertyNameToValues;
        localPropertyNameToValues = propertyNameToValues;
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
     * List of the profiles the signature relates to.
     * 
     * @return List of the {@link Profile profiles} the signature relates to.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Profile[] getProfiles() throws IOException {
        Profile[] localProfiles = profiles;
        if (localProfiles == null) {
            synchronized (this) {
                localProfiles = profiles;
                if (localProfiles == null) {
                    profiles = localProfiles = getProfiles(profileOffsets);
                }
            }
        }
        return localProfiles;
    }
    
    /**
     * The unique Device Id for the signature. Is is composed of the four 
     * {@link Profile} IDs, one per each {@link Component}.
     * 
     * @return unique Device Id for the signature.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public String getDeviceId() throws IOException {
        String localDeviceId = deviceId;
        if (localDeviceId == null) {
            synchronized (this) {
                localDeviceId = deviceId;
                if (localDeviceId == null) {
                    deviceId = localDeviceId = initGetDeviceId();
                }
            }
        }
        return localDeviceId;
    }
    private volatile String deviceId;
    
    /**
     * Gets the values associated with the property.
     * 
     * @param property the {@link Property} whose values are required
     * @return {@link Values} associated with the property, or null if the 
     * property does not exist.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Values getValues(Property property) throws IOException {
        Values localValues = getPropertyIndexToValues().get(property.index);
        if (localValues == null) {
            synchronized (this) {
                localValues = getPropertyIndexToValues().get(property.index);
                if (localValues == null) {
                    localValues = getPropertyValues(property);
                    getPropertyIndexToValues().add(property.index, localValues);
                }
            }
        }
        return localValues;
    }
    
    /**
     * Gets the values associated with the property name.
     * 
     * @param propertyName name of the {@link Property} whose values are required.
     * @return {@link Values} associated with the property, or null if the 
     * property does not exist.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Values getValues(String propertyName) throws IOException {
        Values localValues = getPropertyNameToValues().get(propertyName);
        if (localValues == null) {
            synchronized (this) {
                localValues = getPropertyNameToValues().get(propertyName);
                if (localValues == null) {
                    Property property = dataSet.get(propertyName);
                    if (property != null) {
                        localValues = this.getValues(property);
                    }
                    getPropertyNameToValues().add(propertyName, localValues);
                }
            }
        }
        return localValues;
    }
    
    /**
     * Gets the values associated with the property for this signature.
     * @param property Property to be returned.
     * @return  Array of values associated with the property and signature, or 
     * an empty array if property not found.
     * @throws IOException 
     */
    private Values getPropertyValues(Property property) throws IOException {
        Profile profileForProperty = null;
        for (Profile localProfile : profiles) {
            if (property.getComponent().getComponentId() == 
                    localProfile.getComponent().getComponentId()) {
                profileForProperty = localProfile;
                break;
            }
        }
        return profileForProperty != null ? 
                profileForProperty.getValues(property) :
                null;
    }
    
    /**
     * @return an array of {@link Value value} objects associated with this 
     * signature.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Iterator<Value> getValues() throws IOException {
        // TODO: validate.
        /*
        Value[] localValues = values;
        if (localValues == null) {
            synchronized (this) {
                localValues = values;
                if (localValues == null) {
                    values = localValues = initGetValues();
                }
            }
        }
        return localValues;
        */
        return new ValueIterator(this);
    }

    /**
     * The length in bytes of the signature.
     *
     * @return length in bytes of the signature.
     * @throws IOException if there was a problem accessing data file.
     */
    public int getLength() throws IOException {
        int localLength = length;
        if (localLength == 0) {
            synchronized (this) {
                localLength = length;
                if (localLength == 0) {
                    length = localLength = getSignatureLength();
                }
            }
        }
        return localLength;
    }
    private volatile int length;

    /**
     * Returns an array of nodes associated with the signature.
     * @return an array of nodes associated with the signature.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    protected Node[] doGetNodes() throws IOException {
        Node[] nodesLocal = new Node[getNodeOffsets().size()];
        for (int i = 0; i < getNodeOffsets().size(); i++) {
            nodesLocal[i] = dataSet.nodes.get(getNodeOffsets().get(i));
        }
        return nodesLocal;
    }
    
    /**
     * An array of nodes associated with the signature.
     * 
     * @return  An array of nodes associated with the signature.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Node[] getNodes() throws IOException {
        Node[] localNodes = nodes;
        if (localNodes == null) {
            synchronized(this) {
                localNodes = nodes;
                if (localNodes == null) {
                    nodes = localNodes = doGetNodes();
                }
            }
        }
        return localNodes;
    }
    @SuppressWarnings("VolatileArrayField")
    private volatile Node[] nodes;

    /**
     * Uses the offsets list which must be locked to read in the arrays of nodes
     * or profiles that relate to the signature.
     *
     * @param dataSet The data set the node is contained within
     * @param reader Reader connected to the source data structure and
     * positioned to start reading
     * @param length The number of offsets to read in
     * @return An array of the offsets as integers read from the reader
     */
    protected static List<Integer> readOffsets(Dataset dataSet, 
                                       BinaryReader reader, 
                                       int length) {
        reader.list.clear();
        for (int i = 0; i < length; i++) {
            int profileIndex = reader.readInt32();
            if (profileIndex >= 0) {
                reader.list.add(profileIndex);
            }
        }
        // TODO: validate logic.
        /*
        int[] array = new int[reader.list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.list.get(i);
        }
        return array;
        */
        return reader.list;
    }

    /**
     * Called after the entire data set has been loaded to ensure any further
     * initialisation steps that require other items in the data set can be
     * completed.
     *
     * @throws IOException if there was a problem accessing data file.
     */
    public void init() throws IOException {
        if (nodes == null)
            nodes = getNodes();
        if (profiles == null)
            profiles = getProfiles();
        if (deviceId == null)
            deviceId = getDeviceId();
        if (length == 0) {
            length = getSignatureLength();
        }
    }

    private String initGetDeviceId() throws IOException {
        // Turn them into a string separated by hyphens.
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < getProfiles().length; i++) {
            builder.append(getProfiles()[i].profileId);
            if (i < getProfiles().length - 1) {
                builder.append(DetectionConstants.PROFILE_SEPARATOR);
            }
        }

        return builder.toString();
    }

    /**
     * Gets a string list of the properties and names.
     *
     * @return dictionary of properties and values for the signature.
     * @throws IOException if there was a problem accessing data file.
     */
    public SortedList<String, List<String>> getPropertyValuesAsStrings() 
                                                        throws IOException {
        // Initialise the HashMap with the known number of values and 1 as the 
        // threshold to avoid the need to rehash it.
        SortedList<String, List<String>> list = 
                new SortedList<String, List<String>>();
        while (getValues().hasNext()) {
            Value v = getValues().next();
            if (!list.containsKey(v.getProperty().getName())) {
                list.add(v.getProperty().getName(), new ArrayList<String>());
            }
            list.get(v.getProperty().getName()).add(v.getName());
        }
        
        /*
        for (Value value : getValues()) {
            if (!list.containsKey(value.getProperty().getName())) {
                list.add(value.getProperty().getName(), new ArrayList<String>());
            }
            list.get(value.getProperty().getName()).add(value.getName());
        }
        */
        return list;
    }

    /**
     * Returns an array of values associated with the signature.
     * @return an array of values associated with the signature.
     * @throws IOException
     */
    private Value[] initGetValues() throws IOException {
        List<Value> result = new ArrayList<Value>();

        for (Profile profile : getProfiles()) {
            result.addAll(Arrays.asList(profile.getValues()));
        }

        return result.toArray(new Value[result.size()]);
    }

    /**
     * Returns an array of profiles associated with the signature.
     *
     * @param profileIndexes
     * @return an array of profiles associated with the signature.
     * @throws IOException
     */
    private Profile[] getProfiles(int[] profileIndexes) throws IOException {
        List<Profile> prof = new ArrayList<Profile>();

        for (int profileIndex : profileIndexes) {
            prof.add(getDataSet().getProfiles().get(profileIndex));
        }
        return prof.toArray(new Profile[prof.size()]);
    }
    @SuppressWarnings("VolatileArrayField")
    private volatile Profile[] profiles;

    /**
     * Compares this signature to a list of node offsets.
     *
     * @param nodes list of nodes to compare to.
     * @return Indication of relative value based on the node offsets.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public int compareTo(List<Node> nodes) throws IOException {
        int tempLength = Math.min(getNodeOffsets().size(), nodes.size());

        for (int i = 0; i < tempLength; i++) {
            int difference = getNodeOffsets().get(i) - nodes.get(i).getIndex();
            if (difference != 0) {
                return difference;
            }
        }

        if (getNodeOffsets().size() < nodes.size()) {
            return -1;
        }
        if (getNodeOffsets().size() > nodes.size()) {
            return 1;
        }

        return 0;
    }

    /**
     * Compares this signature to another based on the node offsets. The node
     * offsets in both signatures must be in ascending order.
     *
     * @param other The signature to be compared against.
     * @return Indication of relative value based based on node offsets.
     */
    @Override
    public int compareTo(Signature other) {
        try {
            int tempLength = Math.min(  getNodeOffsets().size(), 
                                        other.getNodeOffsets().size()); 
            for (int i = 0; i < tempLength; i++) {
                int difference = getNodeOffsets().get(i) - 
                        other.getNodeOffsets().get(i);
                if (difference != 0) {
                    return difference;
                }
            }
            
            if (getNodeOffsets().size() < other.getNodeOffsets().size()) {
                return -1;
            }
            if (getNodeOffsets().size() > other.getNodeOffsets().size()) {
                return 1;
            }
        } catch (IOException ex) {
            throw new WrappedIOException(ex.getMessage());
        }
        return 0;
    }

    /**
     * String representation of the signature where irrelevant characters are
     * removed.
     * <p>
     * This method should not be called as it is part of the internal logic.
     * 
     * @return The signature as a string.
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
    public String toString() {
        String localStringValue = stringValue;
        if (localStringValue == null) {
            synchronized (this) {
                localStringValue = stringValue;
                if (localStringValue == null) {
                    try {
                        byte[] buffer = new byte[getLength()];
                        for (Node n : getNodes()) {
                            n.addCharacters(buffer);
                        }
                        for (int i = 0; i < buffer.length; i++) {
                            if (buffer[i] == 0) {
                                buffer[i] = ' ';
                            }
                        }
                        stringValue = localStringValue = new String(buffer, "US-ASCII");
                    } catch (IOException ex) {
                        throw new WrappedIOException(ex.getMessage());
                    }
                }
            }
        }
        return localStringValue;
    }
    private volatile String stringValue;
    
    /**
     * Array of node offsets associated with the signature.
     * 
     * @return Array of node offsets associated with the signature.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public abstract List<Integer> getNodeOffsets() throws IOException;
    
    /**
     * The number of characters in the signature.
     * @return The number of characters in the signature.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    protected abstract int getSignatureLength() throws IOException;
    
    /**
     * Gets the rank, where a lower number means the signature is more popular, 
     * of the signature compared to other signatures.
     * 
     * @return Rank of the signature.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public abstract int getRank() throws IOException;
    
    /**
     * 
     * @param <T> 
     */
    public class ValueIterator implements Iterator<Value> {
        
        private final Signature signature;
        int currentProfile;
        int currentValue;
        
        public ValueIterator(Signature signature) {
            this.signature = signature;
            this.currentProfile = 0;
            this.currentValue = 0;
        }

        @Override
        public boolean hasNext() {
            try {
                if (currentProfile < signature.getProfiles().length && 
                    currentValue < signature.getProfiles()[currentProfile]
                                                        .getValues().length) {
                    return true;
                }
            } catch (IOException ex) {
                throw new ArrayIndexOutOfBoundsException("");
            }
            return false;
        }

        @Override
        public Value next() {
            try {
                Value v = signature.getProfiles()[currentProfile]
                                                    .getValues()[currentValue];
                currentProfile++;
                if (currentValue >= signature.getProfiles()[currentProfile]
                                                        .getValues().length) {
                    currentValue = 0;
                    currentProfile++;
                }
                return v;
            } catch(Exception ex) {
                return null;
            }
        }
        
    }
}
