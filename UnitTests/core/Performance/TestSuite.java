package Performance;

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
    Performance.Lite.V31File.class,
    Performance.Lite.V32File.class,
    Performance.Lite.V31Array.class,
    Performance.Lite.V32Array.class,
    Performance.Lite.V31Memory.class,
    Performance.Lite.V32Memory.class,
    Performance.Premium.V31File.class,
    Performance.Premium.V32File.class,
    Performance.Premium.V31Array.class,
    Performance.Premium.V32Array.class,
    Performance.Premium.V31Memory.class,
    Performance.Premium.V32Memory.class,
    Performance.Enterprise.V31File.class,
    Performance.Enterprise.V32File.class,
    Performance.Enterprise.V31Array.class,
    Performance.Enterprise.V32Array.class,
    Performance.Enterprise.V31Memory.class,
    Performance.Enterprise.V32Memory.class})
public class TestSuite {
}
