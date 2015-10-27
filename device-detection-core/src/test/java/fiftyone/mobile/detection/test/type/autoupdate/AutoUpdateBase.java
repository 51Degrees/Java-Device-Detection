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
package fiftyone.mobile.detection.test.type.autoupdate;

import fiftyone.mobile.detection.DetectionTestSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import static org.junit.Assert.fail;

/**
 * Base methods used to test the auto update functionality.
 */
public class AutoUpdateBase extends DetectionTestSupport{
    
    /**
     * The name of the data file to use during testing.
     */
    private static final String TEST_DATA_FILE = "51Degrees.dat";    
    
    /**
     * Make sure the data file gets replaced with a Lite one to emulate an
     * existing Lite data file.
     * @throws java.io.IOException
     */
    protected void setLiteDataFile() throws IOException {
        String templateFile = System.getProperty("user.dir") 
                + "\\..\\data\\51Degrees-LiteV3.2.dat";
        // Delete existing file in case it's already of the latest version.
        Files.deleteIfExists(Paths.get(getTestDataFile()));
        Files.copy(Paths.get(templateFile), Paths.get(getTestDataFile()), REPLACE_EXISTING);  
        logger.debug(getTestDataFile());
    }
    
    /**
     * @return the test data file.
     */
    protected String getTestDataFile() {
        File userDir = new File(System.getProperty("user.dir"));
        return userDir.getAbsolutePath() + "\\" + TEST_DATA_FILE;
    }
    
    /**
     * Removes all test files from the test directory. Some of the files may
     * have just been released
     */
    protected void cleanFiles() {
        File dir = new File(System.getProperty("user.dir"));
        File[] files = dir.listFiles(new FilenameFilter() { 
                    @Override
                    public boolean accept(File dir, String filename)
                        { return filename.startsWith(TEST_DATA_FILE); }
    	} );        
        for (final File file : files) {
            int iterations = 0;
            while (file.exists() &&
                   iterations < 10) {
                if (file.delete() == false) {
                    System.gc();
                    iterations++;
                }
            }
        }
    }
    
    /**
     * @return an array of all the .lic files in the working directory. Usually 
     * /Java-Device-Detection/device-detection-core
     */
    private File[] getLicenceKeyFiles() {
        File dir = new File(System.getProperty("user.dir"));
        return dir.listFiles(new FilenameFilter() { 
                    @Override
                    public boolean accept(File dir, String filename)
                        { return filename.endsWith(".lic"); }
    	} );
    }

    /**
     * All of the licence key files found need to be extracted into an array 
     * of keys.
     * @return array of Strings representing licence keys from available 
     * test files.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    protected String[] getLicenceKeys() 
            throws FileNotFoundException, IOException {
        ArrayList<String> licenceKeys = new ArrayList();
        for (File licenceKeyFile : getLicenceKeyFiles()) {
            FileInputStream fis = new FileInputStream(licenceKeyFile);
            try {
                InputStreamReader isr = new InputStreamReader(fis);
                try {
                    BufferedReader br = new BufferedReader(isr);
                    try {
                        String line = br.readLine();
                        while (line != null) {
                            if (line.trim().equals("") == false) {
                                licenceKeys.add(line.trim());
                            }
                            line = br.readLine();
                        }
                    } 
                    finally {
                        br.close();
                    }
                }
                finally {
                    isr.close();
                }
            } 
            finally {
                fis.close();
            }
        }
        
        // Check that there are licence keys. Without these no update test can
        // run.
        if (licenceKeys.isEmpty()) {
            fail("No licence keys were available in folder '" + 
                System.getProperty("user.dir") + "'. See " +
                "https://51degrees.com/compare-data-options to acquire valid " +
                "licence keys.");
        }
        
        // Return the keys as an array.
        String[] result = new String[licenceKeys.size()];
    	licenceKeys.toArray(result);
        for (String licenceKey : result) {
            logger.debug("Licence Key: " + licenceKey);
        }
        return result;
    }    
}
