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

package fiftyone.mobile.test.type.api.premium;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.factories.StreamFactory;
import fiftyone.mobile.Filename;
import fiftyone.mobile.TestType;
import fiftyone.mobile.test.type.api.ApiBase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import java.io.IOException;

/**
 *
 */
@Category(TestType.DataSetPremium.class)
public class V32PremiumApiTest extends ApiBase {

    private static String filename = Filename.PREMIUM_PATTERN_V32;
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
