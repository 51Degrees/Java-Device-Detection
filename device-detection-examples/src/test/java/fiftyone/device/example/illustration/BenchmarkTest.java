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

package fiftyone.device.example.illustration;

import fiftyone.device.example.Shared;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class BenchmarkTest {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(BenchmarkTest.class);
    private Benchmark bm;
    
    @Before
    public void setUp() throws IOException {
        bm = new Benchmark(Shared.getGoodUserAgentsFile());
    }

    @After
    public void tearDown() throws IOException {
    }

    @Test
    public void testStreamFile() throws Exception {
        logger.info("Stream File"); 
        testDataSet(StreamFactory.create(Shared.getLitePatternV32(), false));
    }
    
    @Test
    public void testStreamMemory() throws Exception {
        logger.info("Stream Array / Memory"); 
        testDataSet(StreamFactory.create(Benchmark.fileAsBytes(Shared.getLitePatternV32())));
    }
   
    @Test
    public void testMemory() throws Exception {
        logger.info("Memory"); 
        testDataSet(MemoryFactory.create(Shared.getLitePatternV32(), true));
    }
    
    private void testDataSet(Dataset dataSet)
            throws IOException, InterruptedException {
        double baseLine = 0;
        try {
            for (int i = Runtime.getRuntime().availableProcessors(); i > 0; i--) {
                bm.run(dataSet, i);
                logger.info("Concurrent threads '{}'", i); 
                logger.info("Average detection per thread '{}' ms", 
                        bm.getAverageDetectionTimePerThread());
                if (baseLine == 0) {
                    baseLine = bm.getAverageDetectionTimePerThread();
                }
                else {
                    assertTrue(bm.getAverageDetectionTimePerThread() < baseLine);
                }
            }
        }
        finally {
            dataSet.close();
        }
    }
}