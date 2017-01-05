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

package fiftyone.mobile.test;

import fiftyone.mobile.test.type.api.ApiSuite;
import fiftyone.mobile.test.type.httpheader.HttpHeaderSuite;
import fiftyone.mobile.test.type.memory.MemorySuite;
import fiftyone.mobile.test.type.metadata.MetaDataSuite;
import fiftyone.mobile.test.type.performance.PerformanceSuite;
import org.junit.runner.RunWith;

@RunWith(org.junit.runners.Suite.class)
@org.junit.runners.Suite.SuiteClasses({
    ApiSuite.class,
    MemorySuite.class,
    MetaDataSuite.class,
    HttpHeaderSuite.class,
    PerformanceSuite.class})
/**
 * Container for all suites when not running Maven tests
 */
public class SuiteOfSuites {
}
