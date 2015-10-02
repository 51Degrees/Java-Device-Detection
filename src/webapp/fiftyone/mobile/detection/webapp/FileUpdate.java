package fiftyone.mobile.detection.webapp;

import java.io.File;
import java.util.Date;
import java.util.TimerTask;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
public class FileUpdate extends TimerTask {

    private Date lastModifiedDate = null;
    final private String binaryDataFilePath;
    final private FiftyOneDegreesListener listener;

    public FileUpdate(final FiftyOneDegreesListener listener, final String binaryDataFilePath) {
        super();

        this.listener = listener;
        this.binaryDataFilePath = binaryDataFilePath;
        lastModifiedDate = getDataFileDate();
    }

    @Override
    public void run() {
        final Date fileDate = getDataFileDate();
        if (lastModifiedDate == null && fileDate != null) {
            WebProvider.refresh();
        } else if (lastModifiedDate != null && fileDate != null && fileDate.after(lastModifiedDate)) {
            WebProvider.refresh();
        }
    }

    private Date getDataFileDate() {
        Date fileDate = null;
        final File currentFile = new File(binaryDataFilePath);
        if (currentFile.exists()) {
            fileDate = new Date(currentFile.lastModified());
        }
        return fileDate;
    }
}