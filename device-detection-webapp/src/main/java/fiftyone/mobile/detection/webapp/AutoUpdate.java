/* *********************************************************************
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
 * ********************************************************************* */
package fiftyone.mobile.detection.webapp;

import fiftyone.mobile.detection.AutoUpdateStatus;
import fiftyone.mobile.detection.Dataset;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implements a TimerTask to request automatic update for the existing 
 * device data file.
 * <p>
 * The update requires a "Premium" or "Enterprise" licence key. "Lite" data 
 * files are currently not eligible for the automatic updates.
 * <a href="https://51degrees.com/compare-data-options">Get a licence key</a>.
 */
public class AutoUpdate extends TimerTask {

    private final String masterFilePath;
    private final String[] licenseKeys;
    private final static Logger logger = LoggerFactory
            .getLogger(AutoUpdate.class);

    public AutoUpdate(
            final String masterFilePath,
            final List<String> licenseKeys) {
        super();
        this.masterFilePath = masterFilePath;
        this.licenseKeys = licenseKeys.toArray(new String[licenseKeys.size()]);
    }

    /**
     * Implements the TimerTask by checking if an update is required and if so,
     * downloading the latest data.
     * <p>
     * A check is carried out to verify an update is required first. Then the 
     * update method of the AutoUpdate class of the core API is invoked to 
     * retrieve the latest data file. 
     * A {@link fiftyone.mobile.detection.AutoUpdateStatus status code} is then 
     * returned. If the code indicates a problem, then the problem gets logged. 
     * If the auto update completed successfully a message gets logged.
     * Finally, the {@link WebProvider} gets refreshed with the latest data.
     */
    @Override
    public void run() {
        //if (shouldUpdate()) {
            try {
                AutoUpdateStatus result = 
                    fiftyone.mobile.detection.AutoUpdate.update(
                        licenseKeys, 
                        masterFilePath);
                switch(result) {
                    case AUTO_UPDATE_SUCCESS:
                        SimpleDateFormat dateFormat = 
                                new SimpleDateFormat("y-MMM-d");
                        final File masterFile = new File(masterFilePath);
                        Date fileDate = new Date(masterFile.lastModified());
                        String dateStr = dateFormat.format(fileDate);
                        logger.info(String.format(
                                "Automatically updated binary data file '%s' "
                                + " with version published on the '%s'.",
                                masterFile,
                                dateStr));
                        WebProvider.refresh();
                        break;
                    case AUTO_UPDATE_HTTPS_ERR:
                        logger.info(
                                "An error occurred fetching the data file. " +
                                "Try again incase the error is temporary, or " +
                                "validate licence key and network " +
                                "configuration.");
                        break;
                    case AUTO_UPDATE_NOT_NEEDED:
                        logger.info(
                                "The data file is current and does not need " +
                                "to be updated.");
                        break;
                    case AUTO_UPDATE_IN_PROGRESS:
                        logger.info(
                                "Another update operation is in progress.");
                        break;
                    case AUTO_UPDATE_MASTER_FILE_CANT_RENAME:
                        logger.info(
                                "There is a new data file, but the master " +
                                "data file is locked and can't be replaced. " +
                                "Check file system permissions.");
                        break;
                    case AUTO_UPDATE_ERR_429_TOO_MANY_ATTEMPTS:
                        logger.info(
                                "Too many attempts have been made to " +
                                "download a data file from this public IP " +
                                "address or with this licence key. Try again " +
                                "after a period of time.");
                        break;
                    case AUTO_UPDATE_ERR_403_FORBIDDEN:
                        logger.info(
                                "Data not downloaded. The licence key is not " +
                                "valid.");
                        break;
                    case AUTO_UPDATE_ERR_MD5_VALIDATION_FAILED:
                        logger.info(
                                "Data was downloaded but the MD5 check " +
                                "failed.");
                        break;
                    case AUTO_UPDATE_NEW_FILE_CANT_RENAME:
                        logger.info(
                                "A data file was downloaded but could not be " +
                                "moved to become the new master file. Check " +
                                "file system permissions.");
                        break;
                    default:
                        logger.info("Could not update 51Degrees data file "
                                + "reason: " + result);
                        break;
                }
            } catch (Exception ex) {
                logger.warn(String.format(
                        "Exception auto updating file '%s'.",
                        masterFilePath),
                        ex);
            }
        //}
    }

    /**
     * Determines if 51Degrees should be queried to determine if a new data file
     * might be present.
     * @return True if the data file needs to be updated, otherwise false.
     * @throws IOException 
     */
    private boolean shouldUpdate() {
        boolean shouldUpdate = true;
        final File masterFile = new File(masterFilePath);
        // If no file exists an update is definitely required.
        try {
            if (masterFile.exists()) {
                Dataset dataset = null;
                try {
                    dataset = fiftyone.mobile.detection.AutoUpdate.
                            getDataSetWithHeaderLoaded(masterFile);
                    // Don't perform the update if the next update date from 
                    // the current data file is in the future.
                    if(new Date().before(dataset.nextUpdate)) {
                        shouldUpdate = false;
                    }
                } catch (IOException ex) {
                    // data file is probably corrupt, allow update
                    logger.debug(String.format(
                        "Exception checking file '%s'.",
                         masterFile), ex);
                }
                finally {
                    if (dataset != null) {
                        dataset.close();
                    }
                }
            }
        }
        catch (IOException ex) {
            logger.warn(String.format(
                "Exception checking update status of current date file '%s'",
                masterFile), ex);
            // Don't update as something has gone wrong with the file checking
            // process which might have more serious consequences.
            shouldUpdate = false;
        }
        return shouldUpdate;
    }
}
