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
package fiftyone.mobile.detection;

import fiftyone.mobile.detection.entities.Component;
import java.io.IOException;

import fiftyone.properties.MatchMethods;
import fiftyone.mobile.detection.entities.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A single static class which controls the device detection process. 
 * 
 * The process uses 3 steps to determine the properties associated with the 
 * provided user agent. 
 * 
 * Step 1 - each of the character positions of the target user agent are 
 * checked from right to left to determine if a complete node or substring 
 * is present at that position. For example; the sub string Chrome/11 might 
 * indicate the user agent relates to Chrome version 11 from Google. Once
 * every character position is checked a list of matching nodes will be
 * available. 
 * 
 * Step 2 - The list of signatures is then searched to determine
 * if the matching nodes relate exactly to an existing signature. Any popular
 * device will be found at this point. The approach is exceptionally fast at
 * identifying popular devices. 
 * 
 * Step 3 - If the target user agent is less popular, or newer than the 
 * creation time of the data set, a small sub set of possible signatures are 
 * identified from the matching nodes. These signatures are evaluated against 
 * the target user agent to determine the different in relevant characters 
 * between them. The signature which has the lowest difference and is most 
 * popular is then returned. 
 * 
 * Random user agents will not identify any matching nodes. In these situations 
 * a default signature is returned.
 * 
 * The characteristics of the detection data set will determine the accuracy of 
 * the result match. Older data sets that are unaware of the latest devices, 
 * or user agent formats in use will be less accurate.
 * 
 * For more information see http://51degrees.com/Support/Documentation/Java
 */
class Controller {

    /**
     * Comparator used to order the nodes by length with the shortest first.
     */
    private static final Comparator<Node> nodeComparator = new Comparator<Node>() {
        @Override
        public int compare(Node o1, Node o2) {
            try {
                int l0 = o1.getRankedSignatureIndexes().length;
                int l1 = o2.getRankedSignatureIndexes().length;
                if (l0 < l1) {
                    return -1;
                }
                if (l0 > l1) {
                    return 1;
                }
                if (l0 == l1) {
                    /* If both have the same rank, sort by position. */
                    if (o1.position > o2.position) {
                        return 1;
                    }
                    if (o1.position < o2.position) {
                        return -1;
                    }
                }
            } catch (IOException ex) {
                throw new WrappedIOException(ex.getMessage());
            }
            return 0;
        }
    };
    
    /**
     * Used to calculate nearest scores between the match and the user agent.
     */
    private static final NearestScore nearest = new NearestScore();
    /**
     * Used to calculate closest scores between the match and the user agent.
     */
    private static final ClosestScore closest = new ClosestScore();

    /**
     * Entry point to the detection process. Provided with a Match instance
     * configured with the information about the request. 
     * 
     * The dataSet may be used by other threads in parallel and is not assumed 
     * to be used by only one detection process at a time. 
     * 
     * The memory implementation of the data set will always perform fastest 
     * but does consume more memory.
     *
     * @param state current working state of the matching process
     * @throws IOException an I/O exception has occurred
     */
    static void match(MatchState state) throws IOException {

        if (state.getDataSet().getDisposed()) {
            throw new IllegalStateException(
                    "Data Set has been disposed and can't be used for match");
        }

        // If the user agent is too short then don't try to match and
        // return defaults.
        if (state.getTargetUserAgentArray().length == 0
                || state.getTargetUserAgentArray().length < state.getDataSet().getMinUserAgentLength()) {
            // Set the default values.
            matchDefault(state);
        } else {
            // Starting at the far right evaluate the nodes in the data
            // set recording matched nodes. Continue until all character
            // positions have been checked.
            evaluate(state);

            /// Can a precise match be found based on the nodes?
            int signatureIndex = getExactSignatureIndex(state);

            if (signatureIndex >= 0) {
                // Yes a precise match was found.
                state.setSignature(state.getDataSet().signatures.get(signatureIndex));
                state.setMethod(MatchMethods.EXACT);
                state.setLowestScore(0);
            } else {
                // No. So find any other nodes that match if numeric differences
                // are considered.
                evaluateNumeric(state);

                // Can a precise match be found based on the nodes?
                signatureIndex = getExactSignatureIndex(state);

                if (signatureIndex >= 0) {
                    // Yes a precise match was found.
                    state.setSignature(state.getDataSet().signatures.get(signatureIndex));
                    state.setMethod(MatchMethods.NUMERIC);
                } else if (state.getNodesList().size() > 0) {

                    // Get the signatures that are closest to the target.
                    RankedSignatureIterator closestSignatures =
                            getClosestSignatures(state);

                    // Try finding a signature with identical nodes just not in exactly the 
                    // same place.
                    nearest.evaluateSignatures(state, closestSignatures);

                    if (state.getSignature() != null) {
                        // All the sub strings matched, just in different character positions.
                        state.setMethod(MatchMethods.NEAREST);
                    } else {
                        // Find the closest signatures and compare them
                        // to the target looking at the smallest character
                        // difference.
                        closest.evaluateSignatures(state, closestSignatures);
                        state.setMethod(MatchMethods.CLOSEST);
                    }
                }
            }

            // If there still isn't a signature then set the default.
            if (state.getProfiles().length == 0 &&
                state.getSignature() == null) {
                matchDefault(state);
            }
        }
    }

    /**
     * Evaluate the target user agent again, but this time look for a numeric 
     * difference.
     * @param state current working state of the matching process
     * @throws IOException 
     */
    private static void evaluateNumeric(MatchState state) throws IOException {
        state.resetNextCharacterPositionIndex();
        int existingNodeIndex = state.getNodesList().size() - 1;
        while (state.nextCharacterPositionIndex > 0) {
            if (existingNodeIndex < 0
                    || state.getNodesList().get(existingNodeIndex).getRoot().position
                    < state.nextCharacterPositionIndex) {
                state.incrRootNodesEvaluated();
                Node node = state.getDataSet().rootNodes.get(state.nextCharacterPositionIndex).
                        getCompleteNumericNode(state);
                if (node != null
                        && node.getIsOverlap(state) == false) {
                    // Insert the node and update the existing index so that
                    // it's the node to the left of this one.
                    existingNodeIndex = state.insertNode(node) - 1;

                    // Move to the position of the node found as 
                    // we can't use the next node incase there's another
                    // not part of the same signatures closer.
                    state.nextCharacterPositionIndex = node.position;
                } else {
                    state.nextCharacterPositionIndex--;
                }
            } else {
                // The next position to evaluate should be to the left
                // of the existing node already in the list.
                state.nextCharacterPositionIndex = state.getNodesList().get(existingNodeIndex).position;

                // Swap the existing node for the next one in the list.
                existingNodeIndex--;
            }
        }
    }

    /**
     * The detection failed and a default match needs to be returned.
     * @param state current working state of the matching process
     * @throws IOException
     */
    static void matchDefault(MatchState state) throws IOException {
        state.setMethod(MatchMethods.NONE);
        state.getExplicitProfiles().clear();
        for (Component component : state.getDataSet().components) {
            state.getExplicitProfiles().add(component.getDefaultProfile());
        }
    }

    /**
     * Evaluates the match at the current character position until there are no
     * more characters left to evaluate.
     * @param state Information about the detection
     * @throws IOException
     */
    private static void evaluate(MatchState state) throws IOException {

        while (state.nextCharacterPositionIndex >= 0) {

            // Increase the count of root nodes checked.
            state.incrRootNodesEvaluated();

            // See if a leaf node will match from this list.
            Node node = state.getDataSet().rootNodes.get(state.nextCharacterPositionIndex).getCompleteNode(state);

            if (node != null) {
                state.getNodesList().add(0, node);

                // Check from the next root node that can be positioned to 
                // the left of this one.
                state.nextCharacterPositionIndex = node.nextCharacterPosition;
            } else {
                // No nodes matched at the character position, move to the next 
                // root node to the left.
                state.nextCharacterPositionIndex--;
            }
        }
    }

    /**
     * @return if the nodes of the match correspond exactly to a signature then
     * return the index of the signature. Otherwise -1.
     * @param state current working state of the matching process
     * @throws IOException
     */
    private static int getExactSignatureIndex(MatchState state) throws IOException {
        int lower = 0;
        int upper = state.getDataSet().getSignatures().size() - 1;

        while (lower <= upper) {
            state.incrSignaturesRead();
            int middle = lower + (upper - lower) / 2;
            int comparisonResult = state.getDataSet().getSignatures().get(middle).compareTo(
                    state.getNodesList());
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
     * @param state current working state of the matching process
     * @return An enumeration of closest signatures.
     * @throws IOException
     */
    private static RankedSignatureIterator getClosestSignatures(
            final MatchState state) throws IOException {

        if (state.getNodesList().size() == 1) {
            state.setClosestSignaturesCount(
                    state.getNodesList().get(0).getRankedSignatureIndexes().length);
            return new RankedSignatureIterator() {
                final int[] rankedSignatureIndexes =
                        state.getNodesList().get(0).getRankedSignatureIndexes();
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
            orderedNodes.addAll(state.getNodesList());
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
                        state,
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

            state.setClosestSignaturesCount(linkedList.size);

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

    private static int getClosestSignaturesForNode(
            MatchState state,
            int[] signatureIndexList,
            PossibleSignatures linkedList,
            int maxCount, int iteration) {
        // If there is point adding any new signature indexes set the
        // threshold reached indicator. New signatures won't be added
        // and ones with counts lower than maxcount will be removed.
        boolean thresholdReached = state.getNodesList().size() - iteration < maxCount;
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

    private static PossibleSignatures buildInitialList(int[] list) {
        PossibleSignatures linkedList = new PossibleSignatures();
        for (int index : list) {
            linkedList.add(new PossibleSignature(index, 1));
        }
        return linkedList;
    }
}

/**
 * A custom linked list used to identify the most frequently occurring
 * signature indexes.
 */
class PossibleSignatures {

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
class PossibleSignature {

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
