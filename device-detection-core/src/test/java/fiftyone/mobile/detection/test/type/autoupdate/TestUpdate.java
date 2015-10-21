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
import fiftyone.mobile.detection.AutoUpdateException;
import fiftyone.mobile.detection.test.TestType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 *
 */
@Category(TestType.DataSetLite.class)
public class TestUpdate {
    
    /**
     * Where is the data file supposed to be?
     */
    private static final String EXISTING_DATA_FILE = "D:\\51Degrees.dat";
    /**
     * A set of valid licence keys.
     */
    private String[] licenceKeys;
    
    @Before
    public void setUp() throws FileNotFoundException, IOException {
        //Make sure the data file gets replaced with a Lite one to emulate 
        //an existing file.
        String templateFile = System.getProperty("user.dir") 
                + "\\..\\data\\51Degrees-LiteV3.2.dat";
        // Delete existing file in case it's already of the latest version.
        File currentFile = new File(EXISTING_DATA_FILE);        
        if (currentFile.exists()) {
            currentFile.delete();
        }
        //Copy from template Lite file to the current data file.
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(templateFile);
            os = new FileOutputStream(EXISTING_DATA_FILE);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
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
     * @return array of Strings representing licence keys.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private String[] getLicenceKeys() throws FileNotFoundException, IOException {
        // Get all .lic files.
        File[] licenceKeyFiles = getLicenceKeyFiles();
        // Initialise the licence keys array with the relevant number of files.
        licenceKeys = new String[licenceKeyFiles.length];
        
        FileInputStream fis = null;
        BufferedReader br = null;
        
        for (int i = 0; i < licenceKeyFiles.length; i++) {
            try {
                fis = new FileInputStream(licenceKeyFiles[i].getAbsolutePath());
                br = new BufferedReader(new InputStreamReader(fis));
                //Assume .lic file has only one line.
                String line = br.readLine();
                if (line != null) {
                    licenceKeys[i] = line;
                } else {
                    licenceKeys[i] = "";
                }
            } finally {
                if (br != null) {
                    br.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
        }
        
    	return licenceKeys;
    }
    
    @Test
    @Category(TestType.DataSetLite.class)
    public void testUpdate() throws AutoUpdateException, FileNotFoundException, NoSuchAlgorithmException, IOException {
        /**
         * If true is returned then all is good. Anything else is not good.
         */
        if (AutoUpdate.update(getLicenceKeys(), EXISTING_DATA_FILE) != true) {
            fail("Auto update did not return true");
        }
    }
}
