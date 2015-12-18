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

package fiftyone.mobile.detection.test.type.memory.enterprise;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Filename;
import fiftyone.mobile.detection.test.TestType;
import fiftyone.mobile.detection.test.common.UserAgentGenerator;
import fiftyone.mobile.detection.test.type.memory.MemoryBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

@Category({TestType.DataSetEnterprise.class, TestType.TypeMemory.class})
public class V31EnterpriseMemoryMemoryTest extends MemoryBase {

    private static final String filename = Filename.ENTERPRISE_PATTERN_V31;
    private static Dataset dataset;
    private static final double FILE_SIZE_MULTIPLIER = 6;

    @Override
    protected Dataset getDataset() {
        return dataset;
    }

    @BeforeClass
    public static void setUp() throws IOException {
        dataset = getInitialisedDataset(filename, true, 1200, null, false);
    }

    @Before
    public void checkFileExists() {
        assumeFileExists(filename);
    }

    @AfterClass
    public static void tearDown() throws IOException {
        if (dataset != null) dataset.close();
        dataset = null;
    }

    @Test
    @Category({TestType.DataSetEnterprise.class, TestType.TypeMemory.class})
    public void uniqueUserAgentsMulti() throws IOException {
        super.userAgentsMulti(UserAgentGenerator.getUniqueUserAgents(), 
                getExpectedMemoryUsage(FILE_SIZE_MULTIPLIER, filename));
    }

    @Test
    public void uniqueUserAgentsSingle() throws IOException {
        super.userAgentsSingle(UserAgentGenerator.getUniqueUserAgents(), 
                getExpectedMemoryUsage(FILE_SIZE_MULTIPLIER, filename));
    }

    @Test
    public void randomUserAgentsMulti() throws IOException {
        super.userAgentsMulti(UserAgentGenerator.getRandomUserAgents(), 
                getExpectedMemoryUsage(FILE_SIZE_MULTIPLIER, filename));
    }

    @Test
    public void randomUserAgentsSingle() throws IOException {
        super.userAgentsSingle(UserAgentGenerator.getRandomUserAgents(), 
                getExpectedMemoryUsage(FILE_SIZE_MULTIPLIER, filename));
    }

    @Test
    public void badUserAgentsMulti() throws IOException {
        super.userAgentsMulti(UserAgentGenerator.getBadUserAgents(), 
                getExpectedMemoryUsage(FILE_SIZE_MULTIPLIER, filename));
    }

    @Test
    public void badUserAgentsSingle() throws IOException {
        super.userAgentsSingle(UserAgentGenerator.getBadUserAgents(), 
                getExpectedMemoryUsage(FILE_SIZE_MULTIPLIER, filename));
    }
}
