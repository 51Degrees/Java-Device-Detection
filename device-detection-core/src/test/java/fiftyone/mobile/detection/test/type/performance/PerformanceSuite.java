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

import fiftyone.mobile.detection.test.type.performance.enterprise.V31ArrayTest;
import fiftyone.mobile.detection.test.type.performance.enterprise.V31FileTest;
import fiftyone.mobile.detection.test.type.performance.enterprise.V31MemoryTest;
import fiftyone.mobile.detection.test.type.performance.enterprise.V32ArrayTest;
import fiftyone.mobile.detection.test.type.performance.enterprise.V32FileTest;
import fiftyone.mobile.detection.test.type.performance.enterprise.V32MemoryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    fiftyone.mobile.detection.test.type.performance.lite.V31FileTest.class,
    fiftyone.mobile.detection.test.type.performance.lite.V32FileTest.class,
    fiftyone.mobile.detection.test.type.performance.lite.V31ArrayTest.class,
    fiftyone.mobile.detection.test.type.performance.lite.V32ArrayTest.class,
    fiftyone.mobile.detection.test.type.performance.lite.V31MemoryTest.class,
    fiftyone.mobile.detection.test.type.performance.lite.V32MemoryTest.class,
    fiftyone.mobile.detection.test.type.performance.premium.V31FileTest.class,
    fiftyone.mobile.detection.test.type.performance.premium.V32FileTest.class,
    fiftyone.mobile.detection.test.type.performance.premium.V31ArrayTest.class,
    fiftyone.mobile.detection.test.type.performance.premium.V32ArrayTest.class,
    fiftyone.mobile.detection.test.type.performance.premium.V31MemoryTest.class,
    fiftyone.mobile.detection.test.type.performance.premium.V32MemoryTest.class,
    V31FileTest.class,
    V32FileTest.class,
    V31ArrayTest.class,
    V32ArrayTest.class,
    V31MemoryTest.class,
    V32MemoryTest.class})
public class PerformanceSuite {
}
