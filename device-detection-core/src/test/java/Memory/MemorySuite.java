package Memory;

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
    Memory.Lite.V31File.class,
    Memory.Lite.V32File.class,
    Memory.Lite.V31Array.class,
    Memory.Lite.V32Array.class,
    Memory.Lite.V31Memory.class,
    Memory.Lite.V32Memory.class,
    Memory.Premium.V31File.class,
    Memory.Premium.V32File.class,
    Memory.Premium.V31Array.class,
    Memory.Premium.V32Array.class,
    Memory.Premium.V31Memory.class,
    Memory.Premium.V32Memory.class,
    Memory.Enterprise.V31File.class,
    Memory.Enterprise.V32File.class,
    Memory.Enterprise.V31Array.class,
    Memory.Enterprise.V32Array.class,
    Memory.Enterprise.V31Memory.class,
    Memory.Enterprise.V32Memory.class})
public class MemorySuite {
}
