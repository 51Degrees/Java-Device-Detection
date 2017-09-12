/* *********************************************************************
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
 * ********************************************************************* */

package fiftyone.mobile.detection.factories;

import com.google.common.io.Files;
import fiftyone.mobile.Filename;
import fiftyone.mobile.StandardUnitTest;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.IndirectDataset;
import fiftyone.mobile.detection.helper.ViableProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static fiftyone.mobile.detection.helper.DatasetHelper.cacheTests;
import static fiftyone.mobile.detection.helper.DatasetHelper.compareDatasets;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Validate StreamFactory
 */
public class StreamFactoryTest extends StandardUnitTest {

    // create a 3.2 pattern provider using factory create method
    @Test
    public void testCreate32FromFilename () throws Exception {
        File temp = File.createTempFile("Test",".dat");
        File source = new File(Filename.LITE_PATTERN_V32);
        Files.copy(source, temp);

        Dataset dataset = StreamFactory.create(temp.getPath());
        ViableProvider.ensureViableProvider(new Provider(dataset));
        dataset.close();
        // temp must still exist after close
        assertTrue(temp.exists());

        // assess whether temporary file gets deleted
        dataset = StreamFactory.create(temp.getPath(), true);
        ViableProvider.ensureViableProvider(new Provider(dataset));
        dataset.close();
        // temp must NOT still exist after close
        assertFalse(temp.exists());

    }

    // create 3.1 pattern provider using factory create method
    @Test
    public void testCreate31FromFilename () throws Exception {
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V31);
        ViableProvider.ensureViableProvider(new Provider(dataset));
    }

    // tests to see if Stream and Memory load the same thing (default caches)
    @Test
    public void testMemoryStreamDatasetConsistentDefault () throws IOException {

        IndirectDataset indirectDataset =
                StreamFactory.create(Filename.LITE_PATTERN_V32);
        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);

        compareDatasets(indirectDataset, memoryDataset);
    }

    // see if the cache metrics etc work when using default cache.
    @Test
    public void testDefaultCache () throws Exception {
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32);
        Provider provider = new Provider(dataset, 20);

        cacheTests(provider);
    }
}