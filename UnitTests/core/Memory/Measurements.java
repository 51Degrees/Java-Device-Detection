package Memory;

import common.MatchProcessor;
import common.Results;
import fiftyone.mobile.detection.Match;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

/**
 * Used to approximate the amount of memory used during the test.
 */
public class Measurements extends MatchProcessor {
    
    private final long startMemory;

    public AtomicLong totalMemory = new AtomicLong();

    public AtomicInteger memorySamples = new AtomicInteger();

    public Measurements() {
        startMemory = allocatedMemory();
    }

    private int allocatedMemory() {
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        return (int)(runtime.totalMemory() - runtime.freeMemory());
    }
    
    public int getAverageMemoryUsed() {
        double averageMemoryUsed = ((totalMemory.longValue() / 
                memorySamples.intValue()) - startMemory) / 
                (double)(1024 * 1024); 
        return (int)averageMemoryUsed;
    }

    public void reset()
    {
        totalMemory.set(0);
        memorySamples.set(0);
    }

    @Override
    public void Process(Match match, Results result) throws IOException {
        if (result.count.intValue() % 1000 == 0) {
            memorySamples.incrementAndGet();
            totalMemory.addAndGet(allocatedMemory());
        }
    }
}
