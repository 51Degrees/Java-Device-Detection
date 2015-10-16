/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

import java.io.IOException;

import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Signature;

abstract class BaseScore {

    /**
     * Gets the score for the specific node of the signature.
     * @param match Information about the detection.
     * @param node 
     * @return
     */
    protected abstract int getScore(Match match, Node node) throws IOException;

    /**
     * Sets any initial score before each node is evaluated.
     * @param match Information about the detection.
     * @param signature Signature string.
     * @param lastNodeCharacter
     * @return
     */
    protected abstract int getInitialScore(Match match, Signature signature, 
                                    int lastNodeCharacter) throws IOException;

    /**
     * Checks all the signatures using the scoring method provided.
     * @param match Information about the detection.
     * @param closestSignatures Signature strings to evaluate.
     * @throws IOException 
     */
    void evaluateSignatures(Match match,
            RankedSignatureIterator closestSignatures) throws IOException {
        int count = 0, signatureIndex, rankedSignatureIndex;
        closestSignatures.reset();
        match.setLowestScore(Integer.MAX_VALUE);
        int lastNodeCharacter = match.getNodes().get(match.getNodes().size() - 1).getRoot().position;
        while (closestSignatures.hasNext()
                && count < match.getDataSet().maxSignatures) {
            rankedSignatureIndex = closestSignatures.next();
            signatureIndex = match.getDataSet().rankedSignatureIndexes.get(
                    rankedSignatureIndex).getValue();
            evaluateSignature(
                    match,
                    match.getDataSet().signatures.get(signatureIndex),
                    lastNodeCharacter);
            count++;
        }
    }

    /**
     * Compares all the characters up to the max length between the signature 
     * and the target user agent updating the match information if this 
     * signature is better than any evaluated previously.
     * @param match Information about the detection.
     * @param signature Signature string.
     * @param lastNodeCharacter The signature to be evaluated.
     * @throws IOException 
     */
    private void evaluateSignature(Match match, Signature signature, int lastNodeCharacter) throws IOException {
        match.signaturesCompared++;

        // Get the score between the target and the signature stopping if it's
        // going to be larger than the lowest score already found.
        int score = getScore(match, signature, lastNodeCharacter);

        // If the score is lower than the current lowest then use this signature.
        if (score < match.getLowestScore()) {
            match.setLowestScore(score);
            match.setSignature(signature);
        }
    }

    /**
     * Steps through the nodes of the signature comparing those that aren't 
     * contained in the matched nodes to determine a score between the signature
     * and the target user agent. If that score becomes greater or equal to the
     * lowest score determined so far then stop.
     * @param match Information about the detection.
     * @param signature Signature string.
     * @param lastNodeCharacter The position of the last character in the 
     * matched nodes.
     * @return
     * @throws IOException 
     */
    private int getScore(Match match, Signature signature, int lastNodeCharacter) throws IOException {
        // Calculate the initial score based on the difference in length of 
        // the right most node and the target user agent.
        int runningScore = getInitialScore(match, signature, lastNodeCharacter);

        // We only need to check the nodes that are different. As the nodes
        // are in the same order we can simply look for those that are different.
        int matchNodeIndex = 0;
        int signatureNodeIndex = 0;

        while (signatureNodeIndex < signature.getNodeOffsets().length
                && runningScore < match.getLowestScore()) {
            int matchNodeOffset = matchNodeIndex >= match.getNodes().size() ? Integer.MAX_VALUE : match.getNodes().get(matchNodeIndex).getIndex();
            int signatureNodeOffset = signature.getNodeOffsets()[signatureNodeIndex];
            if (matchNodeOffset > signatureNodeOffset) {
                // The matched node is either not available, or is higher than
                // the current signature node. The signature node is not contained
                // in the match so we must score it.
                int score = getScore(match, match.getDataSet().nodes.get(signature.getNodeOffsets()[signatureNodeIndex]));

                // If the score is less than zero then a score could not be 
                // determined and the signature can't be compared to the target
                // user agent. Exit with a high score.
                if (score < 0) {
                    return Integer.MAX_VALUE;
                }
                runningScore += score;
                signatureNodeIndex++;
            } else if (matchNodeOffset == signatureNodeOffset) {
                // They both are the same so move to the next node in each.
                matchNodeIndex++;
                signatureNodeIndex++;
            } else if (matchNodeOffset < signatureNodeOffset) {
                // The match node is lower so move to the next one and see if
                // it's higher or equal to the current signature node.
                matchNodeIndex++;
            }
        }

        return runningScore;
    }
}
