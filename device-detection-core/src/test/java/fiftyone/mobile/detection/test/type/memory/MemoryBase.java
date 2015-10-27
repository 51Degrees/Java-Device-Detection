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

package fiftyone.mobile.detection.test.type.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.DetectionTestSupport;
import fiftyone.mobile.detection.test.TestType;
import fiftyone.mobile.detection.test.common.MemoryMeasurementProcessor;
import fiftyone.mobile.detection.test.common.Results;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import org.junit.experimental.categories.Category;

@Category(TestType.TypeMemory.class)
public abstract class MemoryBase extends DetectionTestSupport {

    protected abstract Dataset getDataset();

    /**
     * Used to monitor memory usage through the test.
     */
    protected MemoryMeasurementProcessor memory = new MemoryMeasurementProcessor();;

    @Before
    public void setUpMemory () {
        // nothing special to do
    }

    @After
    public void tearDownMemory () {
        memory = null;
    }

    protected void userAgentsSingle(Iterable<String> userAgents, double maxAllowedMemory) throws IOException {
        memory.reset();
        Results results = Results.detectLoopSingleThreaded(new Provider(getDataset()), userAgents, memory);
        display(results, maxAllowedMemory);
    }

    protected void userAgentsMulti(Iterable<String> userAgents, double maxAllowedMemory) throws IOException {
        memory.reset();
        Results results = Results.detectLoopMultiThreaded(new Provider(getDataset()), userAgents, memory);
        display(results, maxAllowedMemory);
    }

    private void display(Results results, double maxAllowedMemory) {
        logger.info("Average Allocated per Detection {}", memory.getAverageMemoryAllocatedPerDetection(results));
        memory.logHeapState();
        String message = String.format("Average Used: '%dMB' Max Allowed: '%dMB'", memory.getAverageMemoryUsed(),(int) maxAllowedMemory);
        if (memory.getAverageMemoryUsed() < maxAllowedMemory) {
            logger.info(message);
        } else {
            logger.error(message);
        }
        assertTrue(message, memory.getAverageMemoryUsed() < maxAllowedMemory);
    }

}