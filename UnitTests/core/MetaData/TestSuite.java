package MetaData;

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
 * This Source Code Form is “Incompatible With Secondary Licenses”, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */

@RunWith(Suite.class)
@Suite.SuiteClasses({
    MetaData.Lite.FileV31.class,
    MetaData.Lite.FileV32.class, 
    MetaData.Lite.MemoryV31.class, 
    MetaData.Lite.MemoryV32.class, 
    MetaData.Premium.FileV31.class,
    MetaData.Premium.FileV32.class, 
    MetaData.Premium.MemoryV31.class, 
    MetaData.Premium.MemoryV32.class,     
    MetaData.Enterprise.FileV31.class,
    MetaData.Enterprise.FileV32.class, 
    MetaData.Enterprise.MemoryV31.class, 
    MetaData.Enterprise.MemoryV32.class})
public class TestSuite {
}
