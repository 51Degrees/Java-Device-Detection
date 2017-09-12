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

package fiftyone.mobile.test.type.api;

import fiftyone.mobile.TestType;
import fiftyone.mobile.test.type.api.enterprise.V31EnterpriseApiTest;
import fiftyone.mobile.test.type.api.enterprise.V32EnterpriseApiTest;
import fiftyone.mobile.test.type.api.lite.V31LiteApiTest;
import fiftyone.mobile.test.type.api.lite.V32LiteApiTest;
import fiftyone.mobile.test.type.api.premium.V31PremiumApiTest;
import fiftyone.mobile.test.type.api.premium.V32PremiumApiTest;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
    V31LiteApiTest.class,
    V32LiteApiTest.class,
    V31EnterpriseApiTest.class,
    V32EnterpriseApiTest.class,
    V31PremiumApiTest.class,
    V32PremiumApiTest.class})
@Categories.IncludeCategory(TestType.TypeApi.class)
//@Categories.ExcludeCategory({TestType.DataSetEnterprise.class, TestType.DataSetPremium.class})
/**
 * For running outside Maven test context
 */
public class ApiSuite {
}
