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

import fiftyone.mobile.detection.test.TestType;
import fiftyone.mobile.detection.test.type.memory.enterprise.V31EnterpriseMemoryArrayTest;
import fiftyone.mobile.detection.test.type.memory.enterprise.V31EnterpriseMemoryFileTest;
import fiftyone.mobile.detection.test.type.memory.enterprise.V31EnterpriseMemoryMemoryTest;
import fiftyone.mobile.detection.test.type.memory.enterprise.V32EnterpriseMemoryArrayTest;
import fiftyone.mobile.detection.test.type.memory.enterprise.V32EnterpriseMemoryFileTest;
import fiftyone.mobile.detection.test.type.memory.enterprise.V32EnterpriseMemoryMemoryTest;
import fiftyone.mobile.detection.test.type.memory.lite.*;
import fiftyone.mobile.detection.test.type.memory.premium.*;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    V31LiteMemoryFileTest.class,
    V32LiteMemoryFileTest.class,
    V31LiteMemoryArrayTest.class,
    V32LiteMemoryArrayTest.class,
    V31LiteMemoryMemoryTest.class,
    V32LiteMemoryMemoryTest.class,
    V31PremiumMemoryFileTest.class,
    V32PremiumMemoryFileTest.class,
    V31PremiumMemoryArrayTest.class,
    V32PremiumMemoryArrayTest.class,
    V31PremiumMemoryMemoryTest.class,
    V32PremiumMemoryMemoryTest.class,
    V31EnterpriseMemoryFileTest.class,
    V32EnterpriseMemoryFileTest.class,
    V31EnterpriseMemoryArrayTest.class,
    V32EnterpriseMemoryArrayTest.class,
    V31EnterpriseMemoryMemoryTest.class,
    V32EnterpriseMemoryMemoryTest.class})
@Categories.IncludeCategory(TestType.TypeMemory.class)
public class MemorySuite {
}
