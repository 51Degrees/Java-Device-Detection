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

package fiftyone.mobile.detection.factories;

import fiftyone.mobile.Filename;
import fiftyone.mobile.StandardUnitTest;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Provider;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

import static fiftyone.mobile.detection.helper.ViableProvider.ensureViableProvider;
import static org.junit.Assert.assertTrue;

/**
 * Minimal tests for the instantiation of MemoryFactory
 */
public class MemoryFactoryTest extends StandardUnitTest {

    @Test
    public void testCreateFromStream() throws Exception {
        File testFile = new File(Filename.LITE_PATTERN_V32);
        FileInputStream fileInputStream = new FileInputStream(testFile);
        try {
            Dataset dataset = MemoryFactory.create(fileInputStream);
            try {
                Provider provider = new Provider(dataset);
                ensureViableProvider(provider);
            } finally {
                if (dataset != null) dataset.close();
            }
        } finally {
            // check the stream can still be read i.e.
            // that it is still open (as specified by the method contract)
            int available = fileInputStream.available();
            assertTrue("Stream must be open", available > 0);
            fileInputStream.close();
        }
    }
}