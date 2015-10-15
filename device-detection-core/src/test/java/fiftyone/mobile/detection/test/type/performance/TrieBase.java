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

package fiftyone.mobile.detection.test.type.performance;

import fiftyone.mobile.detection.DetectionTestSupport;
import fiftyone.mobile.detection.test.common.Results;
import fiftyone.mobile.detection.TrieProvider;
import org.junit.After;
import static org.junit.Assert.assertTrue;

public abstract class TrieBase extends DetectionTestSupport {
    
    protected TrieProvider provider;
    protected String dataFile;
    protected int setUpTime;
    protected int maxInitialiseTime = 500;
    protected int guidanceTime = 1;
    
    public TrieBase(String dataFile) {
        this.dataFile = dataFile;
    }
    
    protected void initialiseTime() {
        assertTrue(String.format(
            "Initialisation time '%dms' greater than maximum allowed '%dms'",
            this.setUpTime,
            this.getMaxSetupTime()),
            this.setUpTime < this.getMaxSetupTime());
    }
    
    protected int getGuidanceTime() {
        return 1;
    }
    
    protected int getMaxSetupTime() {
        return 500;
    }
    
    /* Test support methods */
    
    protected fiftyone.mobile.detection.test.common.Results userAgentMulti(Iterable<String> userAgents) {
        return Results.detectLoopMultiThreaded(provider, userAgents);
    }
    
    protected fiftyone.mobile.detection.test.common.Results userAgentmultiAll(Iterable<String> userAgents) {
        Results results = Results.detectLoopMultiThreaded(provider, userAgents);
        System.out.printf("Values check sum: '%d'\r\n", results.checkSum.longValue());
        assertTrue(
            String.format("Average time of '%d' ms exceeded guidance time of '%d' ms",
                results.getAverageTime(),
                guidanceTime),
            results.getAverageTime() < guidanceTime);
        return results;
    }
    
    protected fiftyone.mobile.detection.test.common.Results userAgentsSingle(Iterable<String> userAgents) {
        return Results.detectLoopSingleThreaded(provider, userAgents);
    }
    
    protected fiftyone.mobile.detection.test.common.Results userAgentsSingleAll(Iterable<String> userAgents) {
        Results results = Results.detectLoopSingleThreaded(provider, userAgents);
        System.out.println(String.format("Values check sum: '%s'", results.checkSum));
        try {
            assertTrue(results.getAverageTime() < getGuidanceTime());
        } catch (AssertionError e) {
            System.out.println("Average time of "+results.getAverageTime()+" exceeded the "
                    + "guidance time of "+getGuidanceTime());
        }
        return results;
    }
    
    /* Dispose */
    
    @After
    public void tearDown() throws Exception {
        disposing(true);
        System.out.printf("Disposed of data set from file '%s'\r\n", this.dataFile);
    }
    
    protected void disposing(boolean disposing) {
        if (provider != null) {
            provider.close();
            provider = null;
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        disposing(false);
        super.finalize();
    }
}
