/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

package fiftyone.mobile.detection.webapp;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TimerTask;

/**
 * Checks the master file at regular intervals to spot any changes. If a change 
 * is detected, then the current provider gets replaced with the new data 
 * from the master file.
 * <p>
 * This is particularly useful if you perform manual updates as it allows to 
 * propagate the new data to the provider without restarting the Web server.
 * <p>
 * This timer will also come in handy if you upgrade to a pay-for version of 
 * data or downgrade to "Lite" data.
 * <p>
 * You should not access objects of this class directly or instantiate new 
 * objects using this class as they are part of the internal logic.
 */
public class FileUpdate extends TimerTask {

    private Date lastModifiedDate = null;
    final private String binaryDataFilePath;
    final private FiftyOneDegreesListener listener;

    /**
     * Creates a new object with the necessary information to start the timer 
     * checks.
     * 
     * @param listener FiftyOneDegreesListener.
     * @param binaryDataFilePath path to data file.
     */
    public FileUpdate(final FiftyOneDegreesListener listener, 
                      final String binaryDataFilePath) {
        super();
        this.listener = listener;
        this.binaryDataFilePath = binaryDataFilePath;
        lastModifiedDate = getDataFileDate();
    }

    @Override
    public void run() {
        final Date fileDate = getDataFileDate();
         try {
            if (lastModifiedDate == null && fileDate != null) {
            WebProvider.refresh();
            } else if (lastModifiedDate != null && fileDate != null && fileDate.after(lastModifiedDate)) {
                WebProvider.refresh();
            }
        } catch (IOException ex) {
            //TODO: log this exception.
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