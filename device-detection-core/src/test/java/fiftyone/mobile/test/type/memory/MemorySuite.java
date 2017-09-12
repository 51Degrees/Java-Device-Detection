/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 *
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY:
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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

package fiftyone.mobile.test.type.memory;

import fiftyone.mobile.test.type.memory.enterprise.V31EnterpriseMemoryArrayTest;
import fiftyone.mobile.test.type.memory.enterprise.V31EnterpriseMemoryFileTest;
import fiftyone.mobile.test.type.memory.enterprise.V31EnterpriseMemoryMemoryTest;
import fiftyone.mobile.test.type.memory.enterprise.V32EnterpriseMemoryArrayTest;
import fiftyone.mobile.test.type.memory.enterprise.V32EnterpriseMemoryFileTest;
import fiftyone.mobile.test.type.memory.enterprise.V32EnterpriseMemoryMemoryTest;
import fiftyone.mobile.test.type.memory.lite.*;
import fiftyone.mobile.test.type.memory.premium.*;
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
public class MemorySuite {
}
