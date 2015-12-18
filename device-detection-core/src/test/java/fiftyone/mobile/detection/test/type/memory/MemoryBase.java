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

import com.sun.management.ThreadMXBean;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.DetectionTestSupport;
import static fiftyone.mobile.detection.DetectionTestSupport.fileExists;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.factories.StreamFactory;
import fiftyone.mobile.detection.test.common.MemoryMeasurementProcessor;
import fiftyone.mobile.detection.test.common.Results;
import java.io.File;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;

import static org.junit.Assert.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MemoryBase extends DetectionTestSupport {

    private static NumberFormat numberFormat = NumberFormat.getNumberInstance();
    
    protected abstract Dataset getDataset();
    
    protected static Logger logger = LoggerFactory.getLogger(MemoryBase.class);

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
        logger.info("Average Allocated per Detection '{}' ", 
                numberFormat.format(
                        memory.getAverageMemoryAllocatedPerDetection(results)));
        memory.logHeapState();
        String message = String.format(
                "Average Used: '%dMB' Max Allowed: '%dMB'", 
                memory.getAverageMemoryUsed(),(int) maxAllowedMemory);
        if (memory.getAverageMemoryUsed() < maxAllowedMemory) {
            logger.info(message);
        } else {
            logger.error(message);
        }
        assertTrue(message, memory.getAverageMemoryUsed() < maxAllowedMemory);
    }
    
    public double getExpectedMemoryUsage(double fileSizeMultiplier, String fileName) {
        //(file size in bytes * multiplier) / bytes in Mb
        return (new File(fileName).length() * fileSizeMultiplier) / (1024 * 1024);
    }
    
    /**
     * Universal method to return any version of the dataset based on the 
     * provided parameters.
     * 
     * @param fileName data file to construct the Pattern dataset from.
     * @param memoryMode true if memory factory should be used, false otherwise.
     * @param maxAllowedMemoryMegs optional parameter. Number of MB that the 
     * data file initialisation is expected to use. Used for testing.
     * @param fileAsByteArray used with stream mode. The memoryMode parameter 
     *      must be false and this parameter not null.
     * @param fullDataInit used with memory factory. Indicates if the dataset 
     *      should be completely initialised before dataset is returned.
     *      memoryMode parameter must be true.
     * @return Initialised Dataset object or null if file does not exist.
     * @throws IOException if there was a problem accessing data file.
     */
    protected static Dataset getInitialisedDataset(
            String fileName, boolean memoryMode, long maxAllowedMemoryMegs, 
            byte[] fileAsByteArray, boolean fullDataInit) throws IOException {
        Dataset ds = null;
        if (fileExists(fileName)) {
            NumberFormat nf = NumberFormat.getNumberInstance();
            MemoryMeasurementProcessor memory = 
                    new MemoryMeasurementProcessor();
            memory.reset();
            ThreadMXBean threadMXBean = 
                    (ThreadMXBean)ManagementFactory.getThreadMXBean();
            threadMXBean.setThreadAllocatedMemoryEnabled(true);
            
            long memoryBefore = 
                    threadMXBean.getThreadAllocatedBytes(
                                                Thread.currentThread().getId());
            logger.debug("Memory before: {}", nf.format(memoryBefore));
            if (memoryMode) {
                if (fullDataInit) {
                    // Fully initialised memory-resident dataset.
                    ds = MemoryFactory.create(fileName, true);
                } else {
                    // Memory-resident dataset with lazy load.
                    ds = MemoryFactory.create(fileName);
                }
            } else {
                if (fileAsByteArray == null) {
                    // Dataset connected to file.
                    ds = StreamFactory.create(fileName, false);
                } else {
                    // Dataset connected to a file in the form of a byte array.
                    ds = StreamFactory.create(fileAsByteArray);
                }
            }
            memory.logHeapState();
            long memoryAfter = 
                    threadMXBean.getThreadAllocatedBytes(
                                                Thread.currentThread().getId());
            logger.debug("Memory after: {}",nf.format(memoryAfter));
            long memoryUse = memoryAfter - memoryBefore;
            logger.debug("FinMemory consumed loading detection file: {} ", 
                    nf.format(memoryUse));
            //assertTrue(memoryUse <= maxAllowedMemoryMegs * 1024 * 1024);
        }
        return ds;   
    }
}