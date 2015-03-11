package fiftyone.mobile.detection;

import java.io.IOException;

import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;

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
 * A single static class which controls the device detection process. <p> The
 * process uses 3 steps to determine the properties associated with the provided
 * user agent. <p> Step 1 - each of the character positions of the target user
 * agent are checked from right to left to determine if a complete node or sub
 * string is present at that position. For example; the sub string Chrome/11
 * might indicate the user agent relates to Chrome version 11 from Google. Once
 * every character position is checked a list of matching nodes will be
 * available. <p> Step 2 - The list of signatures is then searched to determine
 * if the matching nodes relate exactly to an existing signature. Any popular
 * device will be found at this point. The approach is exceptionally fast at
 * identifying popular devices. <p> Step 3 - If the target user agent is less
 * popular, or newer than the creation time of the data set, a small sub set of
 * possible signatures are identified from the matching nodes. These signatures
 * are evaluated against the target user agent to determine the different in
 * relevant characters between them. The signature which has the lowest
 * difference and is most popular is then returned. <p> Random user agents will
 * not identify any matching nodes. In these situations a default signature is
 * returned. <p> The characteristics of the detection data set will determine
 * the accuracy of the result match. Older data sets that are unaware of the
 * latest devices, or user agent formats in use will be less accurate. <p> For
 * more information see http://51degrees.mobi/Support/Documentation/Java
 */
public class Controller {

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
     * configured with the information about the request. <p> The dataSet may be
     * used by other threads in parallel and is not assumed to be used by only
     * one detection process at a time. <p> The memory implementation of the
     * data set will always perform fastest but does consume more memory.
     *
     * @param match The match object to be updated.
     * @throws IOException an I/O exception has occurred
     */
    public void match(Match match) throws IOException {

        if (match.getDataSet().getDisposed()) {
            throw new IllegalStateException(
                    "Data Set has been disposed and can't be used for match");
        }

        // If the user agent is too short then don't try to match and
        // return defaults.
        if (match.getTargetUserAgentArray().length == 0
                || match.getTargetUserAgentArray().length < match.getDataSet().getMinUserAgentLength()) {
            // Set the default values.
            matchDefault(match);
        } else {
            // Starting at the far right evaluate the nodes in the data
            // set recording matched nodes. Continue until all character
            // positions have been checked.
            evaluate(match);

            /// Can a precise match be found based on the nodes?
            int signatureIndex = match.getExactSignatureIndex();

            if (signatureIndex >= 0) {
                // Yes a precise match was found.
                match.setSignature(match.getDataSet().signatures.get(signatureIndex));
                match.method = MatchMethods.EXACT;
                match.setLowestScore(0);
            } else {
                // No. So find any other nodes that match if numeric differences
                // are considered.
                evaluateNumeric(match);

                // Can a precise match be found based on the nodes?
                signatureIndex = match.getExactSignatureIndex();

                if (signatureIndex >= 0) {
                    // Yes a precise match was found.
                    match.setSignature(match.getDataSet().signatures.get(signatureIndex));
                    match.method = MatchMethods.NUMERIC;
                } else if (match.getNodes().size() > 0) {

                    // Get the signatures that are closest to the target.
                    Match.RankedSignatureIterator closestSignatures =
                            match.getClosestSignatures();

                    // Try finding a signature with identical nodes just not in exactly the 
                    // same place.
                    nearest.evaluateSignatures(match, closestSignatures);

                    if (match.getSignature() != null) {
                        // All the sub strings matched, just in different character positions.
                        match.method = MatchMethods.NEAREST;
                    } else {
                        // Find the closest signatures and compare them
                        // to the target looking at the smallest character
                        // difference.
                        closest.evaluateSignatures(match, closestSignatures);
                        match.method = MatchMethods.CLOSEST;
                    }
                }
            }

            // If there still isn't a signature then set the default.
            if (match.getProfiles() == null
                    && match.getSignature() == null) {
                matchDefault(match);
            }

        }
    }

    private void evaluateNumeric(Match match) throws IOException {
        match.resetNextCharacterPositionIndex();
        int existingNodeIndex = match.getNodes().size() - 1;
        while (match.nextCharacterPositionIndex > 0) {
            if (existingNodeIndex < 0
                    || match.getNodes().get(existingNodeIndex).getRoot().position
                    < match.nextCharacterPositionIndex) {
                match.rootNodesEvaluated++;
                Node node = match.getDataSet().rootNodes.get(match.nextCharacterPositionIndex).
                        getCompleteNumericNode(match);
                if (node != null
                        && node.getIsOverlap(match) == false) {
                    // Insert the node and update the existing index so that
                    // it's the node to the left of this one.
                    existingNodeIndex = match.insertNode(node) - 1;

                    // Move to the position of the node found as 
                    // we can't use the next node incase there's another
                    // not part of the same signatures closer.
                    match.nextCharacterPositionIndex = node.position;
                } else {
                    match.nextCharacterPositionIndex--;
                }
            } else {
                // The next position to evaluate should be to the left
                // of the existing node already in the list.
                match.nextCharacterPositionIndex = match.getNodes().get(existingNodeIndex).position;

                // Swap the existing node for the next one in the list.
                existingNodeIndex--;
            }
        }
    }

    /**
     * The detection failed and a default match needs to be returned.
     *
     * @param match Information about the detection
     * @throws IOException
     */
    private void matchDefault(Match match) throws IOException {
        match.method = MatchMethods.NONE;
        match.profiles = new Profile[match.getDataSet().components.size()];
        for (int i = 0; i < match.profiles.length; i++) {
            match.profiles[i] = match.getDataSet().components.get(i).getDefaultProfile();
        }
    }

    /**
     * Evaluates the match at the current character position until there are no
     * more characters left to evaluate.
     *
     * @param match Information about the detection
     * @throws IOException
     */
    private void evaluate(Match match) throws IOException {

        while (match.nextCharacterPositionIndex >= 0) {

            // Increase the count of root nodes checked.
            match.rootNodesEvaluated++;

            // See if a leaf node will match from this list.
            Node node = match.getDataSet().rootNodes.get(match.nextCharacterPositionIndex).
                    getCompleteNode(match);

            if (node != null) {
                match.getNodes().add(0, node);

                // Check from the next root node that can be positioned to 
                // the left of this one.
                match.nextCharacterPositionIndex = node.nextCharacterPosition;
            } else {
                // No nodes matched at the character position, move to the next 
                // root node to the left.
                match.nextCharacterPositionIndex--;
            }
        }
    }
}
