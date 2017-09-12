/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 *
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY:
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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

package fiftyone.mobile.detection.helper;

import fiftyone.mobile.detection.Match;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * helper for matches
 */
public class MatchHelper {
    public static void matchEquals(Match match1, Match match2) throws IOException {
        assertEquals(match1.getClosestSignaturesCount(), match2.getClosestSignaturesCount());
        assertEquals(match1.getMethod(), match2.getMethod());
        assertEquals(match1.getDeviceId(), match2.getDeviceId());
        assertEquals(match1.getDifference(), match2.getDifference());
    }


}
