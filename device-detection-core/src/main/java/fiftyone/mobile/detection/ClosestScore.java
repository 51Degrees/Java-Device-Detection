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

import fiftyone.mobile.detection.entities.BaseEntity;
import java.io.IOException;
import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Signature;

/**
 * This method should not be called as it is part of the internal logic.
 */
class ClosestScore extends BaseScore {

    /**
     * Calculate the initial score based on the difference in length of the
     * right most node and the target User-Agent.
     */
    @Override
    protected int getInitialScore(Signature signature, int lastNodeCharacter) 
                                                            throws IOException {
        return Math.abs(lastNodeCharacter + 1 - signature.getLength());
    }

    /**
     * Returns the difference score between the node and the target User-Agent
     * working from right to left.
     *
     * @param state current working state of the matching process
     * @param node
     * @return difference score
     * @throws IOException
     */
    @Override
    protected int getScore(MatchState state, Node node) throws IOException {
        int score = 0;
        int nodeIndex = node.getCharacters().length - 1, 
            targetIndex = node.position + node.getLength();

        // Adjust the score and indexes if the node is too long.
        if (targetIndex >= state.getTargetUserAgentArray().length) {
            score = targetIndex - state.getTargetUserAgentArray().length;
            nodeIndex -= score;
            targetIndex = state.getTargetUserAgentArray().length - 1;
        }

        while (nodeIndex >= 0 && score < state.getLowestScore()) {
            int difference = Math.abs(
                    state.getTargetUserAgentArray()[targetIndex] -
                    node.getCharacters()[nodeIndex]);
            if (difference != 0) {
                int numericDifference = 0;

                // Move right when the characters are numeric to ensure
                // the full number is considered in the difference comparison.
                int newNodeIndex = nodeIndex + 1;
                int newTargetIndex = targetIndex + 1;
                while (newNodeIndex < node.getLength() &&
                       newTargetIndex < state.getTargetUserAgentArray().length && 
                       BaseEntity.getIsNumeric(
                               state.getTargetUserAgentArray()[newTargetIndex]) &&
                       BaseEntity.getIsNumeric(
                               node.getCharacters()[newNodeIndex])) {
                    newNodeIndex++;
                    newTargetIndex++;
                }
                nodeIndex = newNodeIndex - 1;
                targetIndex = newTargetIndex - 1;

                // Find when the characters stop being numbers.
                int characters = 0;
                while (nodeIndex >= 0 &&
                       BaseEntity.getIsNumeric(
                               state.getTargetUserAgentArray()[targetIndex]) &&
                       BaseEntity.getIsNumeric(
                               node.getCharacters()[nodeIndex])) {
                    nodeIndex--;
                    targetIndex--;
                    characters++;
                }

                // If there is more than one character that isn't a number then
                // compare the numeric values.
                if (characters > 1) {
                    numericDifference = Math.abs(
                            BaseEntity.getNumber(
                                    state.getTargetUserAgentArray(), 
                                    targetIndex + 1, 
                                    characters) -
                            BaseEntity.getNumber(
                                    node.getCharacters(), 
                                    nodeIndex + 1, 
                                    characters));
                }

                if (numericDifference != 0) {
                    score += numericDifference;
                } else {
                    score += (difference * 10);
                }
            }
            nodeIndex--;
            targetIndex--;
        }

        return score;
    }
}
