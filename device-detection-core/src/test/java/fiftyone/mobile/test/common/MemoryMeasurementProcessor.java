/*
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
 */

package fiftyone.mobile.test.common;

import fiftyone.mobile.detection.Match;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.ThreadMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Used to approximate the amount of memory used during the test.
 */
public class MemoryMeasurementProcessor implements MatchProcessor {

    private final Logger logger = LoggerFactory.getLogger(MemoryMeasurementProcessor.class);

    public AtomicLong totalMemory = new AtomicLong();

    public AtomicInteger memorySamples = new AtomicInteger();

    private final ThreadMXBean threadMXBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();

    long heapAtStart;

    private final ConcurrentHashMap<Long, Long> lastThreadAllocation = new ConcurrentHashMap<Long, Long>();
    private final ConcurrentHashMap<Long, Long> totalCumulativeThreadAllocation = new ConcurrentHashMap<Long, Long>();
    private final Runtime runtime = Runtime.getRuntime();

    public MemoryMeasurementProcessor() {
    }

    private long heapSize() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private long allocatedMemory() {

        logger.trace("Before GC Runtime: {} Thread Allocated Now {}",
                numberFormat.format(heapSize()),
                numberFormat.format(totalCumulativeThreadAllocation.get(Thread.currentThread().getId())));
        runtime.gc();
        logger.trace("After  GC Runtime: {}", numberFormat.format(heapSize()));
        return heapSize();
    }

    public int getAverageMemoryUsed() {
        long averageMemoryUsed = totalMemory.longValue() / memorySamples.longValue();
        long aveMemoryUsedMb = averageMemoryUsed / (1024l * 1024l);
        return (int) aveMemoryUsedMb;
    }

    public int getAverageMemoryAllocatedPerDetection(Results results) {
        long totalAllocatedAllThreads = 0;
        for (long allocatedMemory: totalCumulativeThreadAllocation.values()) {
            totalAllocatedAllThreads += allocatedMemory;
        }
        long averageMemoryUsed = totalAllocatedAllThreads / results.count.get();
        return (int) averageMemoryUsed;
    }

    public void reset() {
        totalMemory.set(0);
        memorySamples.set(0);
        lastThreadAllocation.clear();
        totalCumulativeThreadAllocation.clear();
        threadMXBean.setThreadAllocatedMemoryEnabled(true);
        logHeapState();
        heapAtStart = heapSize();
        logger.info("Heap at start of test {}", numberFormat.format(heapAtStart));
    }

    public void logHeapState() {
        logger.info("Heap before GC {}", numberFormat.format(heapSize()));
        runtime.gc();
        logger.info("Heap after GC {}", numberFormat.format(heapSize()));
    }

    @Override
    public void prepare () {
        final long threadId = Thread.currentThread().getId();
        lastThreadAllocation.put(threadId, threadMXBean.getThreadAllocatedBytes(threadId));
        if (totalCumulativeThreadAllocation.get(threadId) == null) {
            totalCumulativeThreadAllocation.put(threadId, 0l);
        }
    }

    @Override
    public void process(Match match, Results result) throws IOException {
        final long threadId = Thread.currentThread().getId();
        long totalAllocatedNow = threadMXBean.getThreadAllocatedBytes(threadId);
        long allocated = totalAllocatedNow - lastThreadAllocation.get(threadId);
        totalCumulativeThreadAllocation.put(threadId, totalCumulativeThreadAllocation.get(threadId) + allocated);

        if (result.count.intValue() % 1000 == 0) {
            memorySamples.incrementAndGet();
            totalMemory.addAndGet(allocatedMemory());
        }
    }
}
