package fiftyone.mobile.detection;

import java.io.IOException;

import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Signature;

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
class ClosestScore extends BaseScore {

    /**
     * Calculate the initial score based on the difference in length of the
     * right most node and the target user agent.
     */
    @Override
    protected int getInitialScore(Match match, Signature signature, int lastNodeCharacter) throws IOException {
        return Math.abs(lastNodeCharacter + 1 - signature.getLength());
    }

    /**
     * Returns the difference score between the node and the target user agent
     * working from right to left.
     *
     * @param match
     * @param node
     * @return
     * @throws IOException
     */
    @Override
    protected int getScore(Match match, Node node) throws IOException {
        int score = 0;
        int nodeIndex = node.getCharacters().length - 1, targetIndex = node.position + node.getLength();

        // Adjust the score and indexes if the node is too long.
        if (targetIndex >= match.getTargetUserAgentArray().length) {
            score = targetIndex - match.getTargetUserAgentArray().length;
            nodeIndex -= score;
            targetIndex = match.getTargetUserAgentArray().length - 1;
        }

        while (nodeIndex >= 0 && score < match.getLowestScore()) {
            int difference = Math.abs(
                    match.getTargetUserAgentArray()[targetIndex]
                    - node.getCharacters()[nodeIndex]);
            if (difference != 0) {
                int numericDifference = 0;

                // Move right when the characters are numeric to ensure
                // the full number is considered in the difference comparison.
                int newNodeIndex = nodeIndex + 1;
                int newTargetIndex = targetIndex + 1;
                while (newNodeIndex < node.getLength()
                        && newTargetIndex < match.getTargetUserAgentArray().length
                        && getIsNumeric(match.getTargetUserAgentArray()[newTargetIndex])
                        && getIsNumeric(node.getCharacters()[newNodeIndex])) {
                    newNodeIndex++;
                    newTargetIndex++;
                }
                nodeIndex = newNodeIndex - 1;
                targetIndex = newTargetIndex - 1;

                // Find when the characters stop being numbers.
                int characters = 0;
                while (nodeIndex >= 0
                        && getIsNumeric(match.getTargetUserAgentArray()[targetIndex])
                        && getIsNumeric(node.getCharacters()[nodeIndex])) {
                    nodeIndex--;
                    targetIndex--;
                    characters++;
                }

                // If there is more than one character that isn't a number then
                // compare the numeric values.
                if (characters > 1) {
                    numericDifference = Math.abs(
                            getNumber(match.getTargetUserAgentArray(), targetIndex + 1, characters)
                            - getNumber(node.getCharacters(), nodeIndex + 1, characters));
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

    private static boolean getIsNumeric(byte value) {
        return (value >= (byte) '0' && value <= (byte) '9');
    }

    /**
     * Returns an integer representation of the characters between start and
     * end. Assumes that all the characters are numeric characters.
     *
     * @param array Array of characters with numeric characters present between
     * start and end
     * @param start The first character to use to convert to a number
     * @param end The last character to use to convert to a number
     * @return
     */
    private static int getNumber(byte[] array, int start, int length) {
        int value = 0;
        for (int i = start + length - 1, p = 0; i >= start; i--, p++) {
            value += (int) (Math.pow(10, p)) * ((byte) array[i] - (byte) '0');
        }
        return value;
    }
}
