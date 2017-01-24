/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

package fiftyone.mobile.detection.test.type.metadata;

import fiftyone.mobile.detection.test.type.metadata.enterprise.V31EnterpriseMetadataFileTest;
import fiftyone.mobile.detection.test.type.metadata.enterprise.V31EnterpriseMetadataMemoryTest;
import fiftyone.mobile.detection.test.type.metadata.enterprise.V32EnterpriseMetadataFileTest;
import fiftyone.mobile.detection.test.type.metadata.enterprise.V32EnterpriseMetadataMemoryTest;
import fiftyone.mobile.detection.test.type.metadata.lite.V31LiteMetadataFileTest;
import fiftyone.mobile.detection.test.type.metadata.lite.V31LiteMetadataMemoryTest;
import fiftyone.mobile.detection.test.type.metadata.lite.V32LiteMetadataFileTest;
import fiftyone.mobile.detection.test.type.metadata.lite.V32LiteMetadataMemoryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    V31LiteMetadataFileTest.class,
    V32LiteMetadataFileTest.class,
    V31LiteMetadataMemoryTest.class,
    V32LiteMetadataMemoryTest.class,
    fiftyone.mobile.detection.test.type.metadata.premium.V31LiteMetadataFileTest.class,
    fiftyone.mobile.detection.test.type.metadata.premium.V32LiteMetadataFileTest.class,
    fiftyone.mobile.detection.test.type.metadata.premium.V31LiteMetadataMemoryTest.class,
    fiftyone.mobile.detection.test.type.metadata.premium.V32LiteMetadataMemoryTest.class,
    V31EnterpriseMetadataFileTest.class,
    V32EnterpriseMetadataFileTest.class,
    V31EnterpriseMetadataMemoryTest.class,
    V32EnterpriseMetadataMemoryTest.class})
public class MetaDataSuite {
}
