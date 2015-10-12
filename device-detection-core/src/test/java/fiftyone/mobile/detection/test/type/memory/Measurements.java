/*
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
 */

package fiftyone.mobile.detection.test.type.memory;

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.test.common.MatchProcessor;
import fiftyone.mobile.detection.test.common.Results;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Used to approximate the amount of memory used during the test.
 */
public class Measurements extends MatchProcessor {

    public AtomicLong totalMemory = new AtomicLong();

    public AtomicInteger memorySamples = new AtomicInteger();

    public Measurements() {
    }

    private long allocatedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public int getAverageMemoryUsed() {
        long averageMemoryUsed = totalMemory.longValue() / memorySamples.longValue();
        long aveMenoryUsedMb = averageMemoryUsed / (1024l * 1024l);
        return (int) aveMenoryUsedMb;
    }

    public void reset() {
        System.gc();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

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
