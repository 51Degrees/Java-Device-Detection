/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
/**
 * Used to determine if all the signature node sub strings are in the target
 * just at different character positions.
 * <p>
 * This class should not be called as it is part of the internal logic.
 */
class NearestScore extends BaseScore {

    @Override
    protected int getInitialScore(Signature signature, int lastNodeCharacter) 
            throws IOException {
        return 0;
    }

    /**
     * If the sub string is contained in the target but in a different position
     * return the difference between the two sub string positions.
     * @param current working state of the matching process
     * @param node
     * @returns -1 if a score can't be determined, or the difference in
     * positions
     */
    @Override
    protected int getScore(MatchState state, Node node) throws IOException {
        int index = state.getIndexOf(node);
        if (index >= 0) {
            return Math.abs(node.position + 1 - index);
        }

        // Return -1 to indicate that a score could not be calculated.
        return -1;
    }
}
