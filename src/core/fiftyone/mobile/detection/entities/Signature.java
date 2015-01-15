package fiftyone.mobile.detection.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.ReadonlyList;
import fiftyone.mobile.detection.SortedList;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;

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
 * Signature of a user agent. <p> A signature contains those characters of a
 * user agent which are relevant for the purposes of device detection. For
 * example; most user agents will start with "Mozilla" and therefore these
 * characters are of very little use when detecting devices. Other characters
 * such as those that represent the model of the hardware are very relevant. <p>
 * A signature contains both an array of relevant characters from user agents
 * identified when the data was created and the unique complete node identifies
 * of relevant sub strings contained in multiple signatures and user agents.
 * Together this information is used at detection time to rapidly identify the
 * signature matching a target user agent. <p> Signatures relate to device
 * properties via profiles. Each signature relates to one profile for each
 * component type. <p> For more information about signature see
 * http://51degrees.mobi/Support/Documentation/Java <p> Unlike other entities
 * the signature may have a varying number of nodes and profiles associated with
 * it depending on the data set. All signatures within a data set will have the
 * same number of profiles and nodes associated with them all. As these can
 * change across data sets they can't be included in the source code. As such a
 * secondary header is used in the data set to indicate the number of profiles
 * and nodes in use. The Memory.SignatureList and Stream.SignatureList lists are
 * therefore used to manage lists of signatures rather than the generic lists.
 */
/**
 * The relevant characters from a user agent structured in a manner to enable
 * rapid comparison with a target user agent.
 */
public class Signature extends BaseEntity implements Comparable<Signature> {

    /**
     * List of the node indexes the signature relates to ordered by index of the
     * node.
     */
    public final int[] nodeOffsets;

    /**
     * List of the profiles the signature relates to.
     *
     * @return List of the profiles the signature relates to
     * @throws IOException indicates an I/O exception occurred
     */
    public Profile[] getProfiles() throws IOException {
        if (profiles == null) {
            synchronized (this) {
                if (profiles == null) {
                    profiles = getProfiles(profileOffsets);
                }
            }
        }
        return profiles;
    }
    private int[] profileOffsets;
    private Profile[] profiles = null;

    /**
     * The unique Device Id for the signature.
     *
     * @return unique Device Id for the signature
     * @throws IOException indicates an I/O exception occurred
     */
    public String getDeviceId() throws IOException {
        if (deviceId == null) {
            synchronized (this) {
                if (deviceId == null) {
                    deviceId = initGetDeviceId();
                }
            }
        }
        return deviceId;
    }
    private String deviceId;

    public Value[] getValues() throws IOException {
        if (values == null) {
            synchronized (this) {
                if (values == null) {
                    values = initGetValues();
                }
            }
        }
        return values;
    }
    private Value[] values;

    /**
     * Gets the rank, where a lower number means the signature is more popular, of
     * the signature compared to other signatures.
     * As the property uses the ranked signature indexes list to obtain the rank
     * it will be comparatively slow compared to other methods the firs time
     * the property is accessed.
     * @return the rank of the signature compared to others
     * @throws IOException indicates an I/O exception occurred
     */
    public int getRank() throws IOException {
        if (rank == -1) {
            synchronized (this) {
                if (rank == -1) {
                    rank = getSignatureRank();
                }
            }
        }
        return rank;
    }
    int rank = -1;
    
    /**
     * The length in bytes of the signature.
     *
     * @return length in bytes of the signature
     * @throws IOException indicates an I/O exception occurred
     */
    public int getLength() throws IOException {
        if (_length == 0) {
            synchronized (this) {
                if (_length == 0) {
                    _length = getSignatureLength();
                }
            }
        }
        return _length;
    }

    /**
     * Gets the signature rank by iterating through the list of signature ranks.
     * @return Rank compared to other signatures starting at 0.
     */
    private int getSignatureRank() throws IOException {
        ReadonlyList<RankedSignatureIndex> rsi = 
                getDataSet().rankedSignatureIndexes;
        for(int rank = 0; rank < rsi.size(); rank++) {
            if (rsi.get(rank).getSignatureIndex() == this.getIndex()) {
                return rank;
            }
        }
        return Integer.MAX_VALUE;
    }
    
    /**
     * @return the number of characters in the signature.
     * @throws IOException
     */
    private int getSignatureLength() throws IOException {
        Node lastNode = getDataSet().nodes.get(nodeOffsets[nodeOffsets.length - 1]);
        return lastNode.position + lastNode.getLength() + 1;
    }
    private int _length;

    /**
     * Constructs a new instance of Signature
     *
     * @param dataSet The data set the node is contained within
     * @param index The index in the data structure to the node
     * @param reader Reader connected to the source data structure and
     * positioned to start reading
     */
    public Signature(Dataset dataSet, int index, BinaryReader reader) {
        super(dataSet, index);
        profileOffsets = ReadOffsets(dataSet, reader, dataSet.getProfilesCount());
        nodeOffsets = ReadOffsets(dataSet, reader, dataSet.getNodesCount());
    }

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
    private int[] ReadOffsets(Dataset dataSet, BinaryReader reader, int length) {
        reader.list.clear();
        for (int i = 0; i < length; i++) {
            int profileIndex = reader.readInt32();
            if (profileIndex >= 0) {
                reader.list.add(profileIndex);
            }
        }
        int[] array = new int[reader.list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = reader.list.get(i);
        }
        return array;
    }

    /**
     * Called after the entire data set has been loaded to ensure any further
     * initialisation steps that require other items in the data set can be
     * completed.
     *
     * @throws IOException indicates an I/O exception occurred
     */
    public void init() throws IOException {
        profiles = getProfiles(profileOffsets);
        values = initGetValues();
        deviceId = initGetDeviceId();

        // Set the profile offsets to null as they're no longer
        // needed and can be freed for garbage collection.
        profileOffsets = null;
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
     * @return dictionary of properties and values for the signature
     * @throws IOException indicates an I/O exception occurred
     */
    public SortedList<String, List<String>> getPropertyValuesAsStrings() throws IOException {
        SortedList<String, List<String>> list = new SortedList<String, List<String>>();
        for (Value value : getValues()) {
            if (!list.containsKey(value.getProperty().getName())) {
                list.add(value.getProperty().getName(), new ArrayList<String>());
            }
            list.get(value.getProperty().getName()).add(value.getName());
        }
        return list;
    }

    /**
     * Returns an array of values associated with the signature.
     *
     * @return
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
     * @return
     * @throws IOException
     */
    private Profile[] getProfiles(int[] profileIndexes) throws IOException {
        List<Profile> profiles = new ArrayList<Profile>();

        for (int index : profileIndexes) {
            profiles.add(getDataSet().getProfiles().get(index));
        }
        return profiles.toArray(new Profile[profiles.size()]);
    }

    /**
     * Compares this signature to a list of node offsets.
     *
     * @param nodes list of nodes to compare to
     * @return Indication of relative value based on the node offsets
     */
    public int compareTo(List<Node> nodes) {
        int length = Math.min(this.nodeOffsets.length, nodes.size());

        for (int i = 0; i < length; i++) {
            int difference = this.nodeOffsets[i] - nodes.get(i).getIndex();
            if (difference != 0) {
                return difference;
            }
        }

        if (this.nodeOffsets.length < nodes.size()) {
            return -1;
        }
        if (this.nodeOffsets.length > nodes.size()) {
            return 1;
        }

        return 0;
    }

    /**
     * Compares this signature to another based on the node offsets. The node
     * offsets in both signatures must be in ascending order.
     *
     * @param other The signature to be compared against
     * @return Indication of relative value based based on node offsets
     */
    public int compareTo(Signature other) {
        int length = Math.min(nodeOffsets.length, other.nodeOffsets.length);

        for (int i = 0; i < length; i++) {
            int difference = nodeOffsets[i] - other.nodeOffsets[i];
            if (difference != 0) {
                return difference;
            }
        }

        if (nodeOffsets.length < other.nodeOffsets.length) {
            return -1;
        }
        if (nodeOffsets.length > other.nodeOffsets.length) {
            return 1;
        }

        return 0;
    }

    /**
     * String representation of the signature where irrelevant characters are
     * removed.
     *
     * @return The signature as a string
     */
    @Override
    public String toString() {
        if (stringValue == null) {
            synchronized (this) {
                if (stringValue == null) {
                    try {
                        byte[] buffer = new byte[getLength()];
                        for (int i : nodeOffsets) {
                            getDataSet().nodes.get(i).addCharacters(buffer);
                        }
                        for (int i = 0; i < buffer.length; i++) {
                            if (buffer[i] == 0) {
                                buffer[i] = ' ';
                            }
                        }
                        stringValue = new String(buffer, "US-ASCII");
                    } catch (IOException e) {
                        return super.toString();
                    }
                }
            }
        }
        return stringValue;
    }
    private String stringValue;
}
