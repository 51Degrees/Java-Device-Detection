package fiftyone.mobile.detection.webapp;

import fiftyone.mobile.detection.factories.MemoryFactory;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.UUID;

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

    final private static Logger logger = LoggerFactory
            .getLogger(FiftyOneDegreesListener.class);
    private WebProvider provider;
    private boolean memoryMode = false;
    private Timer autoUpdateTimer;
    private Timer fileUpdateTimer;
    private String binaryFilePath;
    private String tempDirectory;

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        System.out.println("Starting 51Degrees init");
        final String filepath = contextEvent.getServletContext().
                getInitParameter(Constants.BINARY_FILE_PATH);

        tempDirectory = contextEvent.getServletContext().getRealPath("WEB-INF");

        final String memoryModeStr = contextEvent.getServletContext().getInitParameter(Constants.MEMORY_MODE);
        memoryMode = Boolean.parseBoolean(memoryModeStr);

        cleanTemporaryFiles();
        binaryFilePath = loadFirstTimeWebProvider(contextEvent, filepath);
        if (binaryFilePath == null) {
            binaryFilePath = contextEvent.getServletContext().getRealPath(filepath);
        }
        List<String> keys = getLicenseKeys(contextEvent);
        if (keys.size() > 0) {
            autoUpdateTimer = new Timer();
            autoUpdateTimer.schedule(new AutoUpdate(this, binaryFilePath, keys),
                    Constants.AUTO_UPDATE_DELAYED_START,
                    Constants.AUTO_UPDATE_WAIT);
        }

        fileUpdateTimer = new Timer();
        fileUpdateTimer.schedule(new FileUpdate(this, binaryFilePath),
                Constants.FILE_CHECK_DELAYED_START,
                Constants.FILE_CHECK_WAIT);
        System.out.println("Finish 51Degrees init");
    }

    private WebProvider getWebProvider(String filepath) throws IOException {
        final WebProvider newProvider;
        if (memoryMode) {
            newProvider = new WebProvider(MemoryFactory.create(filepath, false));
        } else {
            String workingFilePath = getTempWorkingFile(filepath);
            newProvider = new WebProvider(StreamFactory.create(workingFilePath));
        }
        return newProvider;
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        if (provider != null) {
            provider.dispose();
            provider = null;
        }
    }

    public WebProvider getProvider() {
        return provider;
    }

    private String getTempWorkingFile(final String masterFilePath) throws IOException {
        final File masterFile = new File(masterFilePath);

        // Create a working temp file in the App_Data folder to enable the source
        // file to be updated without stopping the web site.
        final String tempFileName = String.format(
                "%s.%s.tmp",
                masterFile.getName(),
                UUID.randomUUID().toString());

        // Copy the file to enable other processes to update it.
        final File tempFile = new File(tempDirectory, tempFileName);

        final FileInputStream inStream = new FileInputStream(masterFile);
        final FileOutputStream outStream = new FileOutputStream(tempFile);

        final byte[] buffer = new byte[1024];

        int length;
        //copy the file content in bytes 
        while ((length = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, length);
        }

        inStream.close();
        outStream.close();

        return tempFile.getAbsolutePath();
    }

    private List<String> getLicenseKeys(final ServletContextEvent contextEvent) {
        final File dir = new File(contextEvent.getServletContext().getRealPath("WEB-INF"));
        final List<String> keys = new ArrayList<String>();
        for (String file : dir.list()) {
            if (file.endsWith(".lic")) {

                final File licenseFile = new File(contextEvent.getServletContext().getRealPath(file));
                try {
                    final Scanner in = new Scanner(new FileReader(licenseFile.getAbsolutePath()));
                    while (in.hasNextLine()) {
                        keys.add(in.nextLine());
                    }
                } catch (IOException ex) {
                    // Nothing to do, let the loop continue.
                }
            }
        }
        return keys;
    }

    private void cleanTemporaryFiles() {
        try {
            File dir = new File(tempDirectory);

            for (String file : dir.list()) {
                if (file.endsWith(".tmp")) {

                    try {
                        File tempFile = new File(tempDirectory, file);
                        if (!tempFile.delete()) {
                            logger.debug(
                                    String.format("Failure deleting file '%s'.", file));
                        }
                    } catch (Exception ex) {
                        logger.debug(
                                String.format(
                                "Exception deleting temporary file '%s'",
                                file), ex);
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Exception cleaning temporary files",
                    ex);
        }
    }

    public void refreshWebProvider() {
        try {
            if (new File(binaryFilePath).exists()) {
                provider = getWebProvider(binaryFilePath);
                cleanTemporaryFiles();
            }
        } catch (IOException ex) {
            logger.error("Device data could not be reloaded.", ex);
        }
    }

    /**
     * Sets the Web Provider for the first time.
     *
     * @param contextEvent
     * @param filepath
     * @return A string with the absolute file path of the loaded data file, or
     * null if the data was embedded.
     */
    public String loadFirstTimeWebProvider(final ServletContextEvent contextEvent, final String filepath) {
        String result = null;
        try {
            if (new File(filepath).exists()) {
                provider = getWebProvider(filepath);
                result = filepath;
            } else {

                File dir = new File(contextEvent.getServletContext().getRealPath("WEB-INF"));
                for (String resource : dir.list()) {
                    if (resource.endsWith(filepath)) {
                        File dataFile = new File(dir.getAbsolutePath(), resource);
                        if (dataFile.exists()) {
                            result = dataFile.getAbsolutePath();
                            provider = getWebProvider(result);
                        }
                    }
                }
            }

            if (provider != null) {
                logger.info("51Degrees.mobi stream provider initialised");
            } else {
                provider = new WebProvider();
                logger.info("51Degrees.mobi embedded provider initialised");
            }

            contextEvent.getServletContext().setAttribute(
                    Constants.WEB_PROVIDER_KEY, this);
            logger.info("51Degrees.mobi Listener initialised");



        } catch (IOException e) {
            logger.info("51Degrees.mobi Listener initialisation failed: ", e);
        }
        return result;
    }
}