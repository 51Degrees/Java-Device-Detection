package fiftyone.mobile.detection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.mobile.detection.entities.Value;
import fiftyone.mobile.detection.entities.Values;
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
 * Contains all the information associated with the device detection and matched
 * result. <p> The match property can be used to request results from the match
 * using the accessor provided with a Property or the string name of the
 * property. <p> The Signature the target device match against can be returned
 * along with the associated profiles. <p> Statistics associated with the match
 * can also be returned. For example; the Elapsed property returns the time
 * taken to perform the match. The Confidence property provides a value to
 * indicate the differences between the match result and the target user agent.
 * <p> For more information see http://51degrees.mobi/Support/Documentation/Java
 */
/**
 * Generate when a device detection is requested to include the signature
 * matched, the confidence of the match and the method used to obtain the match.
 */
public class Match {

    /**
     * Used to persist the match results to the cache. Used with the SetState
     * method of the match class to retrieve the state.
     */
    class MatchState {

        final MatchMethods method;
        final int nodesEvaluated;
        final Profile[] profiles;
        final int rootNodesEvaluated;
        final Signature signature;
        final int signaturesCompared;
        final int signaturesRead;
        final int stringsRead;
        final int lowestScore;
        final String targetUserAgent;
        final byte[] targetUserAgentArray;
        final ArrayList<Node> nodes;
        final int closestSignaturesCount;

        /**
         * Creates the state based on the match provided.
         *
         * @param match
         */
        MatchState(Match match) throws IOException {
            method = match.getMethod();
            nodesEvaluated = match.getNodesEvaluated();
            profiles = match.getProfiles();
            rootNodesEvaluated = match.getRootNodesEvaluated();
            signature = match.getSignature();
            signaturesCompared = match.getSignaturesCompared();
            signaturesRead = match.getSignaturesRead();
            stringsRead = match.getStringsRead();
            closestSignaturesCount = match.getClosestSignaturesCount();
            lowestScore = match.lowestScore;
            targetUserAgent = match.getTargetUserAgent();
            targetUserAgentArray = match.getTargetUserAgentArray();
            nodes = new ArrayList<Node>(match.nodes);
        }
    }

    /**
     * Used to iterate over the closest signatures.
     */
    public interface RankedSignatureIterator {

        /**
         * Resets the iterator to be used again.
         */
        void reset();

        /**
         * @return returns true if there are more elements in the next property.
         */
        boolean hasNext();

        /**
         * @return the next integer in the iteration.
         */
        int next();
    }

    /**
     * A custom linked list used to identify the most frequently occurring
     * signature indexes.
     */
    private class PossibleSignatures {

        PossibleSignature first;
        PossibleSignature last;
        int size = 0;

        void addBefore(PossibleSignature existing, PossibleSignature newItem) {
            newItem.next = existing;
            newItem.previous = existing.previous;
            if (existing.previous != null) {
                existing.previous.next = newItem;
            }
            existing.previous = newItem;
            if (existing == first) {
                first = newItem;
            }
            size++;
        }

        void addAfter(PossibleSignature existing, PossibleSignature newItem) {
            newItem.next = existing.next;
            newItem.previous = existing;
            if (existing.next != null) {
                existing.next.previous = newItem;
            }
            existing.next = newItem;
            if (existing == last) {
                last = newItem;
            }
            size++;
        }

        /**
         * Adds the item to the end of the linked list.
         */
        void add(PossibleSignature newItem) {
            if (last != null) {
                addAfter(last, newItem);
            } else {
                first = newItem;
                last = newItem;
                size++;
            }
        }

        /**
         * Removes any reference to this element from the linked list.
         */
        void remove(PossibleSignature existing) {
            if (first == existing) {
                first = existing.next;
            }
            if (last == existing) {
                last = existing.previous;
            }
            if (existing.previous != null) {
                existing.previous.next = existing.next;
            }
            if (existing.next != null) {
                existing.next.previous = existing.previous;
            }
            size--;
        }
    }

    /**
     * Used to represent a signature index and the number of times it occurs in
     * the matched nodes.
     */
    private class PossibleSignature {

        /**
         * The ranked signature index.
         */
        public final int rankedSignatureIndex;
        /**
         * The number of times the signature index occurs.
         */
        public int frequency;
        /**
         * The next signature index in the linked list.
         */
        public PossibleSignature next;
        /**
         * The previous signature index in the linked list.
         */
        public PossibleSignature previous;

        PossibleSignature(int rankedSignatureIndex, int frequency) {
            this.rankedSignatureIndex = rankedSignatureIndex;
            this.frequency = frequency;
        }
    }
    /**
     * Comparator used to order the nodes by length with the shortest first.
     */
    private static final Comparator<Node> nodeComparator = new Comparator<Node>() {
        @Override
        public int compare(Node o1, Node o2) {
            int l0 = o1.getRankedSignatureIndexes().length;
            int l1 = o2.getRankedSignatureIndexes().length;
            if (l0 < l1) {
                return -1;
            }
            if (l0 > l1) {
                return 1;
            }
            return 0;
        }
    };

    /**
     * Constructs a new detection match ready to be used.
     *
     * @param dataSet data set to be used for this match
     */
    public Match(Dataset dataSet) {
        this.dataSet = dataSet;
    }

    /**
     * Constructs a new detection match ready to be used to identify the
     * profiles associated with the target user agent.
     *
     * @param dataSet data set to be used for this match
     * @param targetUserAgent user agent to identify
     * @throws UnsupportedEncodingException indicates an Unsupported Encoding 
     * exception occurred
     */
    public Match(Dataset dataSet, String targetUserAgent)
            throws UnsupportedEncodingException {
        this.dataSet = dataSet;
        init(targetUserAgent);
    }
    private final List<Signature> signatures = new ArrayList<Signature>();
    /**
     * List of nodes found for the match.
     */
    private final List<Node> nodes = new ArrayList<Node>();
    /**
     * The data set used for the detection.
     */
    private final Dataset dataSet;
    /**
     * The next character position to be checked.
     */
    public int nextCharacterPositionIndex;

    /**
     * The user agent string as an ASCII byte array.
     * @return byte array representing user agent string
     */
    public byte[] getTargetUserAgentArray() {
        return targetUserAgentArray;
    }
    private byte[] targetUserAgentArray;

    /**
     * The target user agent string used for the detection.
     * @return target user agent string used for detection
     */
    public String getTargetUserAgent() {
        return targetUserAgent;
    }
    private String targetUserAgent;

    /**
     * The signature with the closest match to the user agent provided.
     * @return signature with closest match to the user agent provided
     */
    public Signature getSignature() {
        return signature;
    }

    void setSignature(Signature signature) {
        this.signature = signature;
    }
    private Signature signature;

    /**
     * Resets the match for the user agent returning all the fields to the
     * values they would have when the match was first constructed. Used to
     * avoid having to reallocate memory for data structures when a lot of
     * detections are being performed.
     *
     * @param targetUserAgent
     * @throws UnsupportedEncodingException
     */
    void reset(String targetUserAgent) throws UnsupportedEncodingException {
        nodesEvaluated = 0;
        rootNodesEvaluated = 0;
        signaturesCompared = 0;
        signaturesRead = 0;
        stringsRead = 0;

        signatures.clear();
        nodes.clear();
        profiles = null;
        setSignature(null);

        init(targetUserAgent);
    }

    /**
     * Sets the match properties based on the state information provided.
     *
     * @param state of a previous match from the cache.
     */
    void setState(MatchState state) {
        method = state.method;
        nodesEvaluated = state.nodesEvaluated;
        profiles = state.profiles;
        rootNodesEvaluated = state.rootNodesEvaluated;
        signature = state.signature;
        signaturesCompared = state.signaturesCompared;
        signaturesRead = state.signaturesRead;
        stringsRead = state.stringsRead;
        closestSignaturesCount = state.closestSignaturesCount;
        lowestScore = state.lowestScore;
        targetUserAgent = state.targetUserAgent;
        targetUserAgentArray = state.targetUserAgentArray;
        nodes.clear();
        nodes.addAll(state.nodes);
    }

    /**
     * Initialises the match object ready for detection.
     *
     * @param targetUserAgent
     * @throws UnsupportedEncodingException
     */
    private void init(String targetUserAgent) throws UnsupportedEncodingException {
        if (targetUserAgent != null && targetUserAgent.length() > 0) {
            this.targetUserAgentArray = targetUserAgent.getBytes("US-ASCII");
        } else {
            this.targetUserAgentArray = new byte[0];
        }
        
        // Null check to ensure no down stream problems.
        this.targetUserAgent = targetUserAgent == null ? "" : targetUserAgent;

        resetNextCharacterPositionIndex();
    }

    public int getDifference() {
        return getLowestScore() == null ? 0 : getLowestScore();
    }

    /**
     * The method used to obtain the match.
     * @return method used to obtain match
     */
    public MatchMethods getMethod() {
        return method;
    }
    public MatchMethods method;

    /**
     * The number of signatures read during the detection.
     * @return integer representing number of signatures read during detection
     */
    public int getSignaturesRead() {
        return signaturesRead;
    }
    int signaturesRead;

    /**
     * The number of signatures that were compared against the target user agent
     * if the Closest match method was used.
     * @return integer representing number of signatures compared against user 
     * agent if closest match method was used
     */
    public int getSignaturesCompared() {
        return signaturesCompared;
    }
    int signaturesCompared;

    /**
     * The number of root nodes checked against the target user agent.
     * @return integer representing number of root node checked
     */
    public int getRootNodesEvaluated() {
        return rootNodesEvaluated;
    }
    int rootNodesEvaluated;

    /**
     * The number of nodes checked.
     * @return integer representing the number of nodes checked
     */
    public int getNodesEvaluated() {
        return nodesEvaluated;
    }
    int nodesEvaluated;

    /**
     * The number of strings that were read from the data structure for the
     * match.
     * @return integer representing number of strings read for the match
     */
    public int getStringsRead() {
        return stringsRead;
    }
    int stringsRead;

    /**
     * The number of closest signatures returned for evaluation.
     * @return integer representing number of closest signatures returned for 
     * evaluation
     */
    public int getClosestSignaturesCount() {
        return closestSignaturesCount;
    }
    int closestSignaturesCount;

    /**
     * The unique id of the Device.
     * @return string representing unique id of device
     * @throws IOException signals an I/O exception occurred
     */
    public String getDeviceId() throws IOException {
        return signature != null ? signature.getDeviceId() : null;
    }

    /**
     * Array of profiles associated with the device that was found.
     * @return array of profiles associated with the device that was found
     * @throws IOException indicates an I/O exception occurred
     */
    public Profile[] getProfiles() throws IOException {
        if (profiles == null && signature != null) {
            synchronized (this) {
                if (profiles == null) {
                    profiles = signature.getProfiles();
                }
            }
        }
        return profiles;
    }
    Profile[] profiles;

    /**
     * The user agent of the matching device with irrelevant characters removed.
     * @return the user agent of the matching device with irrelevant characters 
     * removed
     */
    public String getUserAgent() {
        return signature != null ? signature.toString() : null;
    }
    /**
     * The current lowest score for the target user agent. Initialised to the
     * largest possible result.
     */
    private Integer lowestScore;

    /**
     * Reset the next character position index based on the length of the target
     * user agent and the root nodes.
     */
    void resetNextCharacterPositionIndex() {
        // Start checking on the far right of the user agent.
        nextCharacterPositionIndex = Math.min(
                targetUserAgentArray.length - 1,
                getDataSet().rootNodes.size() - 1);
    }

    /**
     * @return if the nodes of the match correspond exactly to a signature then
     * return the index of the signature. Otherwise -1.
     * @throws IOException
     */
    int getExactSignatureIndex() throws IOException {
        int lower = 0;
        int upper = getDataSet().getSignatures().size() - 1;

        while (lower <= upper) {
            signaturesRead++;
            int middle = lower + (upper - lower) / 2;
            int comparisonResult = getDataSet().getSignatures().get(middle).compareTo(
                    getNodes());
            if (comparisonResult == 0) {
                return middle;
            } else if (comparisonResult > 0) {
                upper = middle - 1;
            } else {
                lower = middle + 1;
            }
        }

        return -1;
    }

    /**
     * Returns a distinct list of signatures which most closely match the target
     * user agent string. Where a single signature is not present across all the
     * nodes the signatures which match the most nodes from the target user
     * agent string are returned.
     *
     * @return An enumeration of closest signatures.
     * @throws IOException
     */
    RankedSignatureIterator getClosestSignatures() throws IOException {

        if (nodes.size() == 1) {
            closestSignaturesCount =
                    nodes.get(0).getRankedSignatureIndexes().length;
            return new RankedSignatureIterator() {
                final int[] rankedSignatureIndexes =
                        nodes.get(0).getRankedSignatureIndexes();
                int index = 0;

                @Override
                public boolean hasNext() {
                    return index < rankedSignatureIndexes.length;
                }

                @Override
                public int next() {
                    int value = rankedSignatureIndexes[index];
                    index++;
                    return value;
                }

                @Override
                public void reset() {
                    index = 0;
                }
            };
        } else {
            int maxCount = 1, iteration = 2;

            // Get an iterator for the nodes in ascending order of signature length.
            List<Node> orderedNodes = new ArrayList<Node>();
            orderedNodes.addAll(nodes);
            Collections.sort(orderedNodes, nodeComparator);
            Iterator<Node> nodeIterator = orderedNodes.iterator();

            // Get the first node and add all the signature indexes.
            Node node = nodeIterator.next();
            final PossibleSignatures linkedList = buildInitialList(
                    node.getRankedSignatureIndexes());

            // Count the number of times each signature index occurs.
            while (nodeIterator.hasNext()) {
                node = nodeIterator.next();
                maxCount = getClosestSignaturesForNode(
                        node.getRankedSignatureIndexes(),
                        linkedList,
                        maxCount,
                        iteration);
                iteration++;
            }

            PossibleSignature current = linkedList.first;
            while (current != null) {
                if (current.frequency < maxCount) {
                    linkedList.remove(current);
                }
                current = current.next;
            }

            closestSignaturesCount = linkedList.size;

            return new RankedSignatureIterator() {
                PossibleSignature first = linkedList.first;
                PossibleSignature current = null;

                @Override
                public boolean hasNext() {
                    return current != null;
                }

                @Override
                public int next() {
                    int value = current.rankedSignatureIndex;
                    current = current.next;
                    return value;
                }

                @Override
                public void reset() {
                    current = first;
                }
            };
        }
    }

    private int getClosestSignaturesForNode(
            int[] signatureIndexList,
            PossibleSignatures linkedList,
            int maxCount, int iteration) {
        // If there is point adding any new signature indexes set the
        // threshold reached indicator. New signatures won't be added
        // and ones with counts lower than maxcount will be removed.
        boolean thresholdReached = nodes.size() - iteration < maxCount;
        PossibleSignature current = linkedList.first;
        int signatureIndex = 0;
        while (signatureIndex < signatureIndexList.length && current != null) {
            if (current.rankedSignatureIndex > signatureIndexList[signatureIndex]) {
                // The base list is higher than the target list. Add the element
                // from the target list and move to the next element in each.
                if (thresholdReached == false) {
                    linkedList.addBefore(
                            current,
                            new PossibleSignature(
                            signatureIndexList[signatureIndex],
                            1));
                }
                signatureIndex++;
            } else if (current.rankedSignatureIndex < signatureIndexList[signatureIndex]) {
                if (thresholdReached) {
                    // Threshold reached so we can removed this item
                    // from the list as it's not relevant.
                    PossibleSignature nextItem = current.next;
                    if (current.frequency < maxCount) {
                        linkedList.remove(current);
                    }
                    current = nextItem;
                } else {
                    current = current.next;
                }
            } else {
                // They're the same so increase the frequency and move to the next
                // element in each.
                current.frequency++;
                if (current.frequency > maxCount) {
                    maxCount = current.frequency;
                }
                signatureIndex++;
                current = current.next;
            }
        }
        if (thresholdReached == false) {
            // Add any signature indexes higher than the base list to the base list.
            while (signatureIndex < signatureIndexList.length) {
                linkedList.add(
                        new PossibleSignature(
                        signatureIndexList[signatureIndex],
                        1));
                signatureIndex++;
            }
        }
        return maxCount;
    }

    private PossibleSignatures buildInitialList(int[] list) {
        PossibleSignatures linkedList = new PossibleSignatures();
        for (int index : list) {
            linkedList.add(new PossibleSignature(index, 1));
        }
        return linkedList;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Integer getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(Integer lowestScore) {
        this.lowestScore = lowestScore;
    }

    public void incrStringsRead() {
        stringsRead++;
    }

    public void incrNodesEvaluated() {
        nodesEvaluated++;
    }

    Dataset getDataSet() {
        return dataSet;
    }

    /**
     * Gets the values associated with the property name using the profiles
     * found by the match. If matched profiles don't contain a value then the
     * default profiles for each of the components are also checked.
     *
     * @param property The property whose values are required
     * @return Array of the values associated with the property, or null if the
     * property does not exist
     * @throws IOException indicates an I/O exception occurred
     */
    public Values getValues(Property property) throws IOException {
        Values value = null;

        if (property != null) {
            // Get the property value from the profile returned
            // from the match.
            for (Profile profile : getProfiles()) {
                if (profile.getComponent().getComponentId()
                        == property.getComponent().getComponentId()) {
                    value = profile.getValues(property);
                    break;
                }
            }

            // If the value has not been found use the default profile.
            if (value == null) {
                value = property.getComponent().getDefaultProfile().getValues(property);
            }
        }

        return value;
    }

    /**
     * Gets the values associated with the property name using the profiles
     * found by the match. If matched profiles don't contain a value then the
     * default profiles for each of the components are also checked.
     *
     * @param propertyName The property name whose values are required
     * @return Array of the values associated with the property, or null if the
     * property does not exist
     * @throws IOException indicates an I/O exception occurred
     */
    public Values getValues(String propertyName) throws IOException {
        return getValues(dataSet.get(propertyName));
    }

    public Map<String, String[]> getResults() throws IOException {
        if (results == null) {
            synchronized (this) {
                if (results == null) {
                    Map<String, String[]> newResults =
                            new HashMap<String, String[]>();

                    // Add the properties and values first.
                    for (Profile profile : getProfiles()) {
                        if (profile != null) {
                            for (Property property : profile.getProperties()) {
                                Value[] values = profile.getValues();
                                List<String> strings = new ArrayList<String>();
                                for (int i = 0; i < values.length; i++) {
                                    if (values[i].getProperty() == property) {
                                        strings.add(values[i].getName());
                                    }
                                }
                                newResults.put(
                                    property.getName(),
                                    strings.toArray(new String[strings.size()]));
                            }
                        }
                    }

                    newResults.put(DetectionConstants.DIFFERENCE_PROPERTY,
                            new String[]{Integer.toString(getDifference())});
                    newResults.put(DetectionConstants.NODES,
                            new String[]{toString()});

                    // Add any other derived values.
                    newResults.put(DetectionConstants.DEVICEID,
                            new String[]{getDeviceId()});

                    results = newResults;
                }
            }
        }
        return this.results;
    }
    private Map<String, String[]> results;

    /**
     * Replaces any characters in the target user agent which are outside the
     * range the dataset used when it was built with question marks.
     */
    public void cleanTargetUserAgentArray() {
        for (int i = 0; i < targetUserAgentArray.length; i++) {
            if (targetUserAgentArray[i] < dataSet.lowestCharacter
                    || targetUserAgentArray[i] > dataSet.highestCharacter) {
                targetUserAgentArray[i] = (byte) '?';
            }
        }
    }

    /**
     * Returns the start character position of the node within the target user
     * agent, or -1 if the node does not exist.
     *
     * @param node
     * @return
     * @throws IOException
     */
    int getIndexOf(Node node) throws IOException {
        byte[] characters = node.getCharacters();
        int finalIndex = characters.length - 1;
        for (int index = 0; index < getTargetUserAgentArray().length - characters.length; index++) {
            for (int nodeIndex = 0, targetIndex = index;
                    nodeIndex < characters.length && targetIndex < getTargetUserAgentArray().length;
                    nodeIndex++, targetIndex++) {
                if (characters[nodeIndex] != getTargetUserAgentArray()[targetIndex]) {
                    break;
                } else if (nodeIndex == finalIndex) {
                    return index;
                }
            }
        }
        return -1;
    }

    /**
     * Override the profiles found by the match with the profileId provided.
     *
     * @param profileId The ID of the profile to replace the existing component
     * @throws IOException indicates an I/O exception occurred
     */
    public void updateProfile(int profileId) throws IOException {
        // Find the new profile from the data set.
        Profile newProfile = dataSet.findProfile(profileId);
        if (newProfile != null) {
            // Loop through the profiles found so far and replace the
            // profile for the same component with the new one.
            for (int i = 0; i < getProfiles().length; i++) {
                // Compare by component Id incase the stream data source is
                // used and we have different instances of the same component
                // being used.
                if (profiles[i].getComponent().getComponentId()
                        == newProfile.getComponent().getComponentId()) {
                    profiles[i] = newProfile;
                    break;
                }
            }
        }
    }

    /**
     * A string representation of the nodes found from the target user agent.
     *
     * @return a string representation of the match
     */
    @Override
    public String toString() {
        if (nodes != null && nodes.size() > 0) {
            try {
                byte[] value = new byte[targetUserAgent.length()];
                for (Node node : nodes) {
                    node.addCharacters(value);
                }
                for (int i = 0; i < value.length; i++) {
                    if (value[i] == 0) {
                        value[i] = (byte) '_';
                    }
                }
                return new String(value, "US-ASCII");
            } catch (IOException e) {
                return super.toString();
            }
        }
        return super.toString();
    }

    /**
     * Inserts the node into the list checking to find it's correct position in
     * the list first.
     *
     * @param node The node to be added to the match list
     * @return The index of the node inserted into the list
     */
    public int insertNode(Node node) {
        int index = ~Collections.binarySearch(nodes, node);

        nodes.add(index, node);

        return index;
    }
}