package Performance;

import common.Results;
import fiftyone.mobile.detection.TrieProvider;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

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
public abstract class TrieBase {
    
    protected TrieProvider provider;
    protected String dataFile;
    protected int setUpTime;
    protected int maxInitialiseTime = 500;
    protected int guidanceTime = 1;
    
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
    
    protected abstract int getMaxSetupTime();
    
    @Test
    protected void randomUserAgentMulti() {
        userAgentMulti(common.UserAgentGenerator.getRandomUserAgents());
    }
    
    @Test
    protected void randomUserAgentsMultiAll() {
        userAgentmultiAll(common.UserAgentGenerator.getRandomUserAgents());
    }
    
    @Test
    protected void randomUserAgentsSingle() {
        userAgentsSingle(common.UserAgentGenerator.getRandomUserAgents());
    }
    
    @Test
    protected void randomUserAgentsSingleAll() {
        userAgentsSingleAll(common.UserAgentGenerator.getRandomUserAgents());
    }
    
    @Test
    protected void uniqueUserAgentsMulti() {
        userAgentMulti(common.UserAgentGenerator.getUniqueUserAgents());
    }
    
    @Test
    protected void uniqueUserAgentsMultiAll() {
        userAgentmultiAll(common.UserAgentGenerator.getUniqueUserAgents());
    }
    
    @Test
    protected void uniqueUserAgentsSingle() {
        userAgentsSingle(common.UserAgentGenerator.getUniqueUserAgents());
    }
    
    @Test
    protected void uniqueUserAgentsSingleAll() {
        userAgentsSingleAll(common.UserAgentGenerator.getUniqueUserAgents());
    }
    
    public TrieBase(String dataFile) {
        this.dataFile = dataFile;
    }
    
    public void setUp() {
        System.out.println();
        System.out.printf("Setup test with file '%s'\r\n", dataFile);
    }
    
    protected common.Results userAgentMulti(Iterable<String> userAgents) {
        return common.Utils.detectLoopMultiThreaded(provider, userAgents);
    }
    
    protected common.Results userAgentmultiAll(Iterable<String> userAgents) {
        Results results = common.Utils.detectLoopMultiThreaded(provider, userAgents);
        System.out.printf("Values check sum: '%d'\r\n", results.checkSum.longValue());
        assertTrue(
            String.format("Average time of '%d' ms exceeded guidance time of '%d' ms",
                results.getAverageTime(),
                guidanceTime),
            results.getAverageTime() < guidanceTime);
        return results;
    }
    
    protected common.Results userAgentsSingle(Iterable<String> userAgents) {
        return common.Utils.detectLoopSingleThreaded(provider, userAgents);
    }
    
    protected common.Results userAgentsSingleAll(Iterable<String> userAgents) {
        Results results = common.Utils.detectLoopSingleThreaded(provider, userAgents);
        System.out.println(String.format("Values check sum: '%s'", results.checkSum));
        try {
            assertTrue(results.getAverageTime() < getGuidanceTime());
        } catch (AssertionError e) {
            System.out.println("Average time of "+results.getAverageTime()+" exceeded the "
                    + "guidance time of "+getGuidanceTime());
        }
        return results;
    }
}
