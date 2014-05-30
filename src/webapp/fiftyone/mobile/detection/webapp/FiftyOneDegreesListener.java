package fiftyone.mobile.detection.webapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
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
public class FiftyOneDegreesListener implements ServletContextListener {

    private final static Logger logger = LoggerFactory
            .getLogger(FiftyOneDegreesListener.class);

    /**
     * Timer used to check for changes at 51Degrees.
     */
    private Timer autoUpdateTimer;
    
    /**
     * Timer used to check for changes to the file on the disk.
     */
    private Timer fileUpdateTimer;

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        logger.debug("Starting 51Degrees Listener Initialisation");
        File binaryFile = WebProvider.getBinaryFilePath(
                contextEvent.getServletContext());
        if (binaryFile != null) {
            
            // Initialise the auto update service from 51Degrees if licence
            // keys are available.
            List<String> keys = getLicenceKeys(contextEvent.getServletContext());
            if (keys.size() > 0) {
                autoUpdateTimer = new Timer();
                autoUpdateTimer.schedule(new AutoUpdate(
                        binaryFile.getAbsolutePath(), 
                        keys),
                        Constants.AUTO_UPDATE_DELAYED_START * 1000,
                        Constants.AUTO_UPDATE_WAIT * 1000);
            }

            // Initialise the auto update based on file system checks.
            fileUpdateTimer = new Timer();
            fileUpdateTimer.schedule(
                    new FileUpdate(this, binaryFile.getAbsolutePath()),
                    Constants.FILE_CHECK_DELAYED_START * 1000,
                    Constants.FILE_CHECK_WAIT * 1000);
        }
        logger.debug("Finished 51Degrees Listener Initialisation");
    }

    /**
     * Gets the licence keys needed to get new versions of the data file
     * from 51Degrees.
     * @param contextEvent
     * @return 
     */
    private List<String> getLicenceKeys(final ServletContext sc) {
        final File licenceDirectory = new File(sc.getRealPath("WEB-INF"));
        final List<String> keys = new ArrayList<String>();
        for (File file : licenceDirectory.listFiles()) {
            if (file.getName().toLowerCase().endsWith("lic")) {
                try {
                    final Scanner in = new Scanner(new FileReader(file));
                    while (in.hasNextLine()) {
                        keys.add(in.nextLine());
                    }
                } catch (FileNotFoundException ex) {
                    // Nothing we can do as the file should be found as it
                    // was returned from the directory list.
                }
            }
        }
        return keys;
    }

    /**
     * Closes the listener and stops all the update timers.
     * @param sce 
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.debug("Destroying 51Degrees Listener");
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
        }
        if (fileUpdateTimer != null) {
            fileUpdateTimer.cancel();
        }
        logger.debug("Destroyed 51Degrees Listener");
    }
}