/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2015 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

package fiftyone.mobile.detection.test.type.api.enterprise;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.factories.StreamFactory;
import fiftyone.mobile.detection.Filename;
import fiftyone.mobile.detection.test.TestType;
import fiftyone.mobile.detection.test.type.api.ApiBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import java.io.IOException;

/**
 *
 */
@Category(TestType.DataSetEnterprise.class)
public class V32EnterpriseApiTest extends ApiBase {

    private static String filename = Filename.ENTERPRISE_PATTERN_V32;
    private static Dataset dataset;
    private static Provider provider;

    @BeforeClass
    public static void createDataset() throws IOException {
        if (fileExists(filename)) {
            dataset = StreamFactory.create(filename, false);
            provider = new Provider(dataset);
        }
    }

    @Before
    public void checkFileExists() {
        assumeFileExists(filename);
    }

    @AfterClass
    public static void dispose() throws IOException {
        if (dataset != null) dataset.close();
        dataset = null;
        provider = null;
    }

    @Override
    public Provider getProvider() {
        return provider;
    }

    @Override
    public Dataset getDataset() {
        return dataset;
    }

}
