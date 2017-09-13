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
 * This Source Code Form is ?Incompatible With Secondary Licenses?, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.detection.readers;

import java.io.File;

/**
 * Base class for file sources.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public abstract class SourceFileBase extends SourceBase {

    /**
     * The file containing the source data.
     */
    private final File fileInfo;
    
    /**
     * True if the file is temporary and should be deleted when the source 
     * is disposed of.
     */
    private final boolean isTempFile;
    
    /**
     * Construct a new source from file on disk.
     * 
     * @param fileName File source of the data.
     * @param isTempFile True if the file will be deleted when the 
     * source is disposed.
     */
    SourceFileBase(String fileName, boolean isTempFile) {
        this.fileInfo = new File(fileName);
        this.isTempFile = isTempFile;
    }
    
    /**
     * Delete the file if it's a temporary file and it still exists.
     * <p>
     * If the file is not deleted the first time then retry forcing garbage 
     * collection. If the file was used as a memory mapped buffer it may take
     * time for the buffer to be released after the file handle.
     */
    protected void deleteFile() {
        if (this.isTempFile) {
            int iterations = 0;
            while (getFile().exists() && iterations < 10) {
                if (getFile().delete() == false) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}
                    System.gc();
                    iterations++;
                }
            }
        }
    }
    
    /**
     * Returns the file used to construct this Source.
     * 
     * @return the file used to construct this Source.
     */
    protected File getFile() {
        return fileInfo;
    }
}
