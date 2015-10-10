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

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.test.DetectionTestSupport;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.test.common.Results;
import org.junit.After;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public abstract class Base extends DetectionTestSupport {
    
    /**
     * Data set used to perform the tests on.
     */
    protected Dataset dataSet;
    
    /**
     * Used to monitor memory usage through the test.
     */
    protected Measurements memory;
    
    /** 
     * The path to the data file to use to create the dataset.
     */
    protected final String dataFile;
    
    public Base(String dataFile) {
        this.dataFile = dataFile;
    }
    
    /**
     * Ensures the data set is disposed of correctly at the end of the test.
     * @throws Exception 
     */
    @After
    public void tearDown() throws Exception {
        disposing(true);
        System.out.printf("Disposed of data set from file '%s'\r\n", this.dataFile);
    }
    
    /**
     * Ensures the data set is disposed of correctly.
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable {
        disposing(false);
        super.finalize();
    }

    /**
     * Ensures resources used by the data set are closed and memory released.
     * @param disposing 
     */
    protected void disposing(boolean disposing) {
        if (dataSet != null) {
            dataSet.close();
            dataSet = null;
        }
    }
    
    public void setUp() {
        System.gc();
        System.out.println(); 
        System.out.printf("Test: %s\r\n", getMethodName());
        System.out.printf("Setup test with file '%s'\r\n", dataFile);
    }
    
    protected void userAgentsSingle(Iterable<String> userAgents,
            double maxAllowedMemory) throws IOException {
        memory.reset();
        Results.detectLoopSingleThreaded(
            new Provider(this.dataSet),
            userAgents,
            memory);
        System.out.printf(
            "Average Used: '%dMB'\r\nMax Allowed: '%dMB'\r\n", 
            memory.getAverageMemoryUsed(),
            (int)maxAllowedMemory);
        assertTrue(String.format(
                "Memory use was '%dMB' but max allowed '%dMB'",
                memory.getAverageMemoryUsed(),
                (int)maxAllowedMemory), 
            memory.getAverageMemoryUsed() < maxAllowedMemory);
    }

    protected void userAgentsMulti(Iterable<String> userAgents,
            double maxAllowedMemory) throws IOException {
        memory.reset();
        Results.detectLoopMultiThreaded(
                new Provider(this.dataSet),
                userAgents,
                memory);
        System.out.printf(
            "Average Used: '%dMB'\r\nMax Allowed: '%dMB'\r\n", 
            memory.getAverageMemoryUsed(),
            (int)maxAllowedMemory);
        assertTrue(String.format(
                "Memory use was '%dMB' but max allowed '%dMB'",
                memory.getAverageMemoryUsed(),
                (int)maxAllowedMemory),
            memory.getAverageMemoryUsed() < maxAllowedMemory);
    }
}