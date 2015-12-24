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
import fiftyone.mobile.detection.search.SearchResult;
import java.util.Comparator;
import java.util.List;

/**
 * A single static class which controls the device detection process.
 * <p>
 * The process uses several steps to determine the properties associated with 
 * the provided User-Agent. 
 * <p>
 * Step 1 - each of the character positions of the target User-Agent are 
 * checked from right to left to determine if a complete node or sub string is 
 * present at that position. For example; the sub string Chrome/11 might 
 * indicate the User-Agent relates to Chrome version 11 from Google. Once every 
 * character position is checked a list of matching nodes will be available.
 * <p>
 * Step 2 - The list of signatures is then searched to determine if the 
 * matching nodes relate exactly to an existing signature. Any popular device 
 * will be found at this point. The approach is exceptionally fast at 
 * identifying popular devices. This is termed the Exact match method.
 * <p>
 * Step 3 - If a match has not been found exactly then the target User-Agent is 
 * evaluated to find nodes that have the smallest numeric difference. For 
 * example if Chrome/40 were in the target User-Agent at the same position as 
 * Chrome/32 in the signature then Chrome/32 with a numeric difference score of 
 * 8 would be used. If a signature can then be matched exactly against the new 
 * set of nodes this will be returned. This is termed the Numeric match method.
 * <p>
 * Step 4 - If the target User-Agent is less popular, or newer than the 
 * creation time of the data set, a small sub set of possible signatures are 
 * identified from the matched nodes. The sub set is limited to the most 
 * popular 200 signatures.
 * <p>
 * Step 5 - The sub strings of the signatures from Step 4 are then evaluated to 
 * determine if they exist in the target User-Agent. For example if Chrome/32 
 * in the target appears one character to the left of Chrome/32 in the 
 * signature then a difference of 1 would be returned between the signature 
 * and the target. This is termed the Nearest match method.
 * <p>
 * Step 6 - The signatures from Step 4 are evaluated against the target 
 * User-Agent to determine the difference in relevant characters between them. 
 * The signature with the lowest difference in ASCII character values with the 
 * target is returned. This is termed the Closest match method.
 * <p>
 * Random User-Agents will not identify any matching nodes. In these situations 
 * a default signature is returned.
 * <p>
 * The characteristics of the detection data set will determine the accuracy of 
 * the result match. Older data sets that are unaware of the latest devices, 
 * or User-Agent formats in use will be less accurate.
 * <p>
 * This class is part of the internal logic and should not be referenced 
 * directly.
 */
class Controller {

    /**
     * Comparator used to order the nodes by length with the shortest first.
     */
    private static final Comparator<Node> nodeComparator = 
                                                    new Comparator<Node>() {
        @Override
        public int compare(Node o1, Node o2) {
            try {
                int l0 = o1.getRankedSignatureIndexes().size();
                int l1 = o2.getRankedSignatureIndexes().size();
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
     * Used to calculate nearest scores between the match and the User-Agent.
     */
    private static final NearestScore nearest = new NearestScore();
    /**
     * Used to calculate closest scores between the match and the User-Agent.
     */
    private static final ClosestScore closest = new ClosestScore();

    /**
     * Entry point to the detection process. Provided with a Match instance
     * configured with the information about the request. 
     * <p>
     * The dataSet may be used by other threads in parallel and is not assumed 
     * to be used by only one detection process at a time. 
     * <p>
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
        // If the User-Agent is too short then don't try to match and
        // return defaults.
        if (state.getTargetUserAgentArray().length == 0
                || state.getTargetUserAgentArray().length < 
                   state.getDataSet().getMinUserAgentLength()) {
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
                state.setSignature(state.getDataSet().signatures.
                        get(signatureIndex));
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
                    state.setSignature(state.getDataSet().signatures.
                            get(signatureIndex));
                    state.setMethod(MatchMethods.NUMERIC);
                } else if (state.getNodesList().size() > 0) {

                    // Get the signatures that are closest to the target.
                    RankedSignatureIterator closestSignatures =
                            getClosestSignatures(state);

                    // Try finding a signature with identical nodes just not in 
                    // exactly the same place.
                    nearest.evaluateSignatures(state, closestSignatures);

                    if (state.getSignature() != null) {
                        // All the sub strings matched, just in different 
                        // character positions.
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
     * Evaluate the target User-Agent again, but this time look for a numeric 
     * difference.
     * 
     * @param state current working state of the matching process
     * @throws IOException if there was a problem accessing data file.
     */
    private static void evaluateNumeric(MatchState state) throws IOException {
        state.resetNextCharacterPositionIndex();
        int existingNodeIndex = state.getNodesList().size() - 1;
        while (state.nextCharacterPositionIndex > 0) {
            if (existingNodeIndex < 0
                    || state.getNodesList().get(existingNodeIndex).getRoot().position
                    < state.nextCharacterPositionIndex) {
                state.incrRootNodesEvaluated();
                Node node = state.getDataSet().rootNodes.
                        get(state.nextCharacterPositionIndex).
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
                state.nextCharacterPositionIndex = state.getNodesList().
                        get(existingNodeIndex).position;

                // Swap the existing node for the next one in the list.
                existingNodeIndex--;
            }
        }
    }

    /**
     * The detection failed and a default match needs to be returned.
     * 
     * @param state current working state of the matching process
     * @throws IOException if there was a problem accessing data file.
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
     * 
     * @param state Information about the detection
     * @throws IOException if there was a problem accessing data file.
     */
    private static void evaluate(MatchState state) throws IOException {

        while (state.nextCharacterPositionIndex >= 0) {

            // Increase the count of root nodes checked.
            state.incrRootNodesEvaluated();

            // See if a leaf node will match from this list.
            Node node = state.getDataSet().rootNodes.
                    get(state.nextCharacterPositionIndex).getCompleteNode(state);

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
     * If the nodes of the match correspond exactly to a signature then return
     * the index of the signature found. Otherwise -1.
     * 
     * @param state of the match process
     * @return index of the signature or -1
     * @throws IOException if there was a problem accessing data file.
     */
    private static int getExactSignatureIndex(MatchState state) 
                                                            throws IOException {
        SearchResult result = state.match.getDataSet().getSignatureSearch().
            binarySearchResults(state.getNodesList());
        state.signaturesRead += result.getIterations();
        return result.getIndex();
    }
    
    /**
     * Returns a distinct list of signatures which most closely match the target
     * User-Agent string. Where a single signature is not present across all the
     * nodes the signatures which match the most nodes from the target user
     * agent string are returned.
     * 
     * @param state current working state of the matching process
     * @return An enumeration of closest signatures.
     * @throws IOException if there was a problem accessing data file.
     */
    private static RankedSignatureIterator getClosestSignatures(
            final MatchState state) throws IOException {
        RankedSignatureIterator result;
        if (state.getNodesList().size() == 1) {
            result = new RankedSignatureIterator() {
                List<Integer> rankedSignatureIndexes = state.getNodesList().
                        get(0).getRankedSignatureIndexes();
                int index = 0;

                @Override
                public boolean hasNext() {
                    return index < rankedSignatureIndexes.size();
                }
                
                @Override
                public int size() {
                    return rankedSignatureIndexes.size();
                }

                @Override
                public int next() {
                    int value = rankedSignatureIndexes.get(index);
                    index++;
                    return value;
                }

                @Override
                public void reset() {
                    index = 0;
                }
            };
        } else {
            final MostFrequentFilter filter = new MostFrequentFilter(state);
            result = new RankedSignatureIterator() {
                final List<Integer> rankedSignatureIndexes = filter;
                int index = 0;

                @Override
                public boolean hasNext() {
                    return index < rankedSignatureIndexes.size();
                }
                
                @Override
                public int size() {
                    return rankedSignatureIndexes.size();
                }

                @Override
                public int next() {
                    int value = rankedSignatureIndexes.get(index);
                    index++;
                    return value;
                }

                @Override
                public void reset() {
                    index = 0;
                }
            };
        }
        state.closestSignaturesCount += result.size();
        return result;
    }
}