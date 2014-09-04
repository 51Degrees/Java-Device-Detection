package fiftyone.mobile.detection.webapp;

import fiftyone.mobile.detection.AutoUpdateException;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class AutoUpdate extends TimerTask {

    private final String masterFilePath;
    private String[] licenseKeys;
    final private static Logger logger = LoggerFactory
            .getLogger(AutoUpdate.class);

    public AutoUpdate(
            final String masterFilePath,
            final List<String> licenseKeys) {
        super();
        this.masterFilePath = masterFilePath;
        this.licenseKeys = licenseKeys.toArray(new String[licenseKeys.size()]);
    }

    @Override
    public void run() {
        if (shouldUpdate()) {
            try {
                boolean success = fiftyone.mobile.detection.AutoUpdate.update(
                        licenseKeys, masterFilePath);
                if (success) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("y-MMM-d");
                    final File masterFile = new File(masterFilePath);
                    Date fileDate = new Date(masterFile.lastModified());
                    String dateStr = dateFormat.format(fileDate);
                    logger.info(String.format(
                            "Automatically updated binary data file '%s' with "
                            + " version published on the '%s'.",
                            masterFile,
                            dateStr));
                    WebProvider.refresh();
                }
            } catch (AutoUpdateException ex) {
                logger.warn(String.format(
                        "Exception auto updating file '%s'",
                        masterFilePath),
                        ex);
            } catch (Exception ex) {
                logger.warn(String.format(
                        "Exception auto updating file '%s'",
                        masterFilePath),
                        ex);                
            }
        }
    }

    private boolean shouldUpdate() {
        // check if file exists
        boolean shouldUpdate = true;
        final File masterFile = new File(masterFilePath);
        // If no file exists an update is definitely required.
        if (masterFile.exists()) {
            try {
                Dataset dataset = StreamFactory.create(masterFilePath);
                // Check if the current data set needs an update. Lite data always
                // needs an update, non lite data only needs an update if the
                // nextUpdate member has expired.
                if(dataset.getName() != "Lite" && new Date().before(dataset.nextUpdate)) {
                    shouldUpdate = false;
                }
            } catch (IOException ex) {
                // data file is probably corrupt, allow update
            }
        }

        return shouldUpdate;
    }
}
