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

package fiftyone.mobile.detection;

import fiftyone.mobile.Filename;
import fiftyone.mobile.StandardUnitTest;
import fiftyone.mobile.detection.cache.LruCache;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.factories.StreamFactoryTest;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static fiftyone.mobile.detection.helper.MatchHelper.matchEquals;

/**
 * minimal testing that Provider works. Some of this is covered in the Factory and DatasetBuilder tests
 *
 * @see StreamFactoryTest
 * @see DatasetBuilderTest
 */
public class ProviderTest extends StandardUnitTest {

    // check we get the same results whatever way we create a dataset
    @Test
    public void testMemoryAndStreamSame () throws IOException {
        IndirectDataset cachedDataset = DatasetBuilder.file()
                .configureDefaultCaches()
                .build(Filename.LITE_PATTERN_V32);
        Provider cachedProvider = new Provider(cachedDataset, new LruCache(5000));

        IndirectDataset unCachedDataset = DatasetBuilder.file()
                .build(Filename.LITE_PATTERN_V32);
        Provider unCachedProvider = new Provider(unCachedDataset);

        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);
        Provider memoryProvider = new Provider(memoryDataset);

        FileInputStream is = new FileInputStream(Filename.GOOD_USERAGENTS_FILE);
        BufferedReader source = new BufferedReader(new InputStreamReader(is));
        String line;
        int count = 0;
        while ((line = source.readLine()) != null) {
            Match cachedMatch = cachedProvider.match(line);
            Match unCachedMatch = unCachedProvider.match(line);
            Match memoryMatch = memoryProvider.match(line);
            matchEquals(memoryMatch,cachedMatch);
            matchEquals(memoryMatch,unCachedMatch);
            count++;
        }
        logger.info("{} tests done", count);
        source.close();
        cachedDataset.close();
        unCachedDataset.close();
        memoryDataset.close();
    }
}