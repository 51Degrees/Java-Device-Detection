package MetaData;

import MetaData.Enterprise.V31FileTest;
import MetaData.Enterprise.V31MemoryTest;
import MetaData.Enterprise.V32FileTest;
import MetaData.Enterprise.V32MemoryTest;
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
    MetaData.Lite.V31FileTest.class,
    MetaData.Lite.V32FileTest.class,
    MetaData.Lite.V31MemoryTest.class,
    MetaData.Lite.V32MemoryTest.class,
    MetaData.Premium.V31FileTest.class,
    MetaData.Premium.V32FileTest.class,
    MetaData.Premium.V31MemoryTest.class,
    MetaData.Premium.V32MemoryTest.class,
    V31FileTest.class,
    V32FileTest.class,
    V31MemoryTest.class,
    V32MemoryTest.class})
public class MetaDataSuite {
}
