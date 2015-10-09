package Memory;

import Memory.Enterprise.V31ArrayTest;
import Memory.Enterprise.V31FileTest;
import Memory.Enterprise.V31MemoryTest;
import Memory.Enterprise.V32ArrayTest;
import Memory.Enterprise.V32FileTest;
import Memory.Enterprise.V32MemoryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

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

@RunWith(Suite.class)
@Suite.SuiteClasses({
    Memory.Lite.V31FileTest.class,
    Memory.Lite.V32FileTest.class,
    Memory.Lite.V31ArrayTest.class,
    Memory.Lite.V32ArrayTest.class,
    Memory.Lite.V31MemoryTest.class,
    Memory.Lite.V32MemoryTest.class,
    Memory.Premium.V31FileTest.class,
    Memory.Premium.V32FileTest.class,
    Memory.Premium.V31ArrayTest.class,
    Memory.Premium.V32ArrayTest.class,
    Memory.Premium.V31MemoryTest.class,
    Memory.Premium.V32MemoryTest.class,
    V31FileTest.class,
    V32FileTest.class,
    V31ArrayTest.class,
    V32ArrayTest.class,
    V31MemoryTest.class,
    V32MemoryTest.class})
public class MemorySuite {
}
