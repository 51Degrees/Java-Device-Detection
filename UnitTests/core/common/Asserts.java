package common;

import fiftyone.mobile.detection.Dataset;
import static org.junit.Assert.*;

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
 * This Source Code Form is "Incompatible With Secondary Licenses @ %.0f%%", dataSet.get() * 100), as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */

public class Asserts {
    public static void AssertCacheMissesGoodAll(Dataset dataSet)
    {
        assertTrue(String.format("Signature Cache Misses @ %.0f%%", dataSet.getPercentageSignatureCacheMisses() * 100), dataSet.getPercentageSignatureCacheMisses() < 0.4);
        assertTrue(String.format("Strings Cache Misses @ %.0f%%", dataSet.getPercentageStringsCacheMisses() * 100), dataSet.getPercentageStringsCacheMisses() < 0.6);
        assertTrue(String.format("Ranked Signatures Cache Misses @ %.0f%%", dataSet.getPercentageRankedSignatureCacheMisses() * 100), dataSet.getPercentageRankedSignatureCacheMisses() < 0.5);
        assertTrue(String.format("Node Cache Misses @ %.0f%%", dataSet.getPercentageNodeCacheMisses() * 100), dataSet.getPercentageNodeCacheMisses() < 0.3);
        assertTrue(String.format("Value Cache Misses @ %.0f%%", dataSet.getPercentageValuesCacheMisses() * 100), dataSet.getPercentageValuesCacheMisses() < 0.3);
        assertTrue(String.format("Profile Cache Misses @ %.0f%%", dataSet.getPercentageProfilesCacheMisses() * 100), dataSet.getPercentageProfilesCacheMisses() < 0.3);
    }
        
    public static void AssertCacheMissesGood(Dataset dataSet)
    {
        assertTrue(String.format("Signature Cache Misses @ %.0f%%", dataSet.getPercentageSignatureCacheMisses() * 100), dataSet.getPercentageSignatureCacheMisses() < 0.4);
        assertTrue(String.format("Strings Cache Misses @ %.0f%%", dataSet.getPercentageStringsCacheMisses() * 100), dataSet.getPercentageStringsCacheMisses() < 0.5);
        assertTrue(String.format("Ranked Signatures Cache Misses @ %.0f%%", dataSet.getPercentageRankedSignatureCacheMisses() * 100), dataSet.getPercentageRankedSignatureCacheMisses() < 0.5);
        assertTrue(String.format("Node Cache Misses @ %.0f%%", dataSet.getPercentageNodeCacheMisses() * 100), dataSet.getPercentageNodeCacheMisses() < 0.3);
    }

    public static void AssertCacheMissesBadAll(Dataset dataSet)
    {
        assertTrue(String.format("Signature Cache Misses @ %.0f%%", dataSet.getPercentageSignatureCacheMisses() * 100), dataSet.getPercentageSignatureCacheMisses() < 0.4);
        assertTrue(String.format("Strings Cache Misses @ %.0f%%", dataSet.getPercentageStringsCacheMisses() * 100), dataSet.getPercentageStringsCacheMisses() < 0.5);
        assertTrue(String.format("Ranked Signatures Cache Misses @ %.0f%%", dataSet.getPercentageRankedSignatureCacheMisses() * 100), dataSet.getPercentageRankedSignatureCacheMisses() < 0.5);
        assertTrue(String.format("Node Cache Misses @ %.0f%%", dataSet.getPercentageNodeCacheMisses() * 100), dataSet.getPercentageNodeCacheMisses() < 0.5);
        assertTrue(String.format("Value Cache Misses @ %.0f%%", dataSet.getPercentageValuesCacheMisses() * 100), dataSet.getPercentageValuesCacheMisses() < 0.3);
        assertTrue(String.format("Profile Cache Misses @ %.0f%%", dataSet.getPercentageProfilesCacheMisses() * 100), dataSet.getPercentageProfilesCacheMisses() < 0.3);
    }

    public static void AssertCacheMissesBad(Dataset dataSet)
    {
        assertTrue(String.format("Signature Cache Misses @ %.0f%%", dataSet.getPercentageSignatureCacheMisses() * 100), dataSet.getPercentageSignatureCacheMisses() < 0.4);
        assertTrue(String.format("Strings Cache Misses @ %.0f%%", dataSet.getPercentageStringsCacheMisses() * 100), dataSet.getPercentageStringsCacheMisses() < 0.8);
        assertTrue(String.format("Ranked Signatures Cache Misses @ %.0f%%", dataSet.getPercentageRankedSignatureCacheMisses() * 100), dataSet.getPercentageRankedSignatureCacheMisses() < 0.5);
        assertTrue(String.format("Node Cache Misses @ %.0f%%", dataSet.getPercentageNodeCacheMisses() * 100), dataSet.getPercentageNodeCacheMisses() < 0.5);
    }
}
