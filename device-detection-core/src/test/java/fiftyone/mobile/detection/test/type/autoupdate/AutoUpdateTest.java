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
package fiftyone.mobile.detection.test.type.autoupdate;

import fiftyone.mobile.detection.AutoUpdate;
import fiftyone.mobile.detection.AutoUpdateStatus;
import static fiftyone.mobile.detection.AutoUpdateStatus.AUTO_UPDATE_NOT_NEEDED;
import static fiftyone.mobile.detection.AutoUpdateStatus.AUTO_UPDATE_SUCCESS;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.test.TestType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.NoSuchAlgorithmException;
import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests an upgrade an an existing Lite data file.
 */
@Category(TestType.DataSetPremium.class)
public class AutoUpdateTest extends AutoUpdateBase {
    
    @Before
    public void setUp() throws IOException {
        super.cleanFiles();
    }
    
    @After
    public void tearDown() throws IOException {
        super.cleanFiles();
    }
    
    @Test
    @Category(TestType.DataSetPremium.class)
    public void testUpgradeLite() throws
            FileNotFoundException, NoSuchAlgorithmException, 
            IOException, Exception {
        super.setLiteDataFile();
        update();
    }
    
    @Test
    @Category(TestType.DataSetPremium.class)
    public void testDownloadNew() throws 
            FileNotFoundException, NoSuchAlgorithmException, 
            IOException, Exception {
        File f = new File(super.getTestDataFile());
        if (f.exists()) {
            if (f.delete() == false) {
                fail("Could not delete existing file");
            }
        }
        update();
    }
    
    @Test
    @Category(TestType.DataSetPremium.class)
    public void testDownloadNotModified() throws 
            FileNotFoundException, NoSuchAlgorithmException, 
            IOException, Exception {
        testDownloadNew();
        
        // Check that the standard auto update method works.
        String[] licenceKeys = super.getLicenceKeys();
        if (licenceKeys.length > 0) {
            AutoUpdateStatus result = AutoUpdate.update(
                    licenceKeys, 
                    super.getTestDataFile());
            if (result != AUTO_UPDATE_NOT_NEEDED) {
                fail("Data file should not have needed updating.");
            }
        
        
            // Test a direct request also works correctly. Avoids the check which
            // compares the file contents.
            HttpURLConnection client = (HttpURLConnection)AutoUpdate.fullUrl(
                        licenceKeys).openConnection();
            client.setIfModifiedSince(
                    new File(super.getTestDataFile()).lastModified());
            if (client.getResponseCode() != 304) {
                fail("304 response should be returned as file already exists.");
            }
        }
    }
    
    // Snippet Start
    private void update() throws NoSuchAlgorithmException, 
                                 IllegalArgumentException, 
                                 Exception {
        String[] licenceKeys = super.getLicenceKeys();
        if (licenceKeys.length > 0) {
            AutoUpdateStatus result = AutoUpdate.update(
                    licenceKeys, 
                    super.getTestDataFile());
            if (result != AUTO_UPDATE_SUCCESS) {
                logger.debug("Status code was: " + result.toString());
                fail("Data file update process failed.");
            }
            Dataset dataSet = MemoryFactory.create(super.getTestDataFile());
            try {
                if (dataSet.getName().equals("Lite"))
                {
                    logger.debug("Data set name was: " + dataSet.getName());
                    fail("Data set name was 'Lite'.");
                }
            }
            finally {
                dataSet.close();
            }        
        }
    }
    // Snippet End
}
