package fiftyone.mobile.detection.webapp;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Disposable;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.UUID;
import javax.servlet.ServletContext;

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
public class WebProvider extends Provider implements Disposable {

    /**
     * Used to store the results for previous matches to reduce the number of
     * detection requests.
     */
    interface MatchResult extends Map<String, String[]> {
    }
    /**
     * Used to create a new instance of the active provider.
     */
    private static final Object lock = new Object();
    /**
     * The currently active web provider.
     */
    private static WebProvider activeProvider;
    /**
     * Provider initialised with the embedded device data.
     */
    private static WebProvider embeddedProvider;
    /**
     * Used to log information about activity.
     */
    private final static Logger logger = LoggerFactory.getLogger(WebProvider.class);
    /**
     * Used to store the result for the current request in the
     * HttpServletRequest's attribute collection.
     */
    private static final String RESULT_ATTIBUTE = "51D_RESULT";
    /**
     * The data file used by the data set of the provider if a stream factory
     * was used to create the provider.
     */
    private String sourceDataFile = null;
    
    /**
     * Constructs a new instance of the web provider using the embedded data.
     *
     * @throws IOException
     */
    public WebProvider() throws IOException {
        super(Constants.CACHE_SERVICE_INTERVAL);
    }

    /**
     * Constructs a new instance of the web provider connected to the dataset
     * provided.
     *
     * @param dataSet used by the provider.
     */
    public WebProvider(Dataset dataSet) {
        super(dataSet, Constants.CACHE_SERVICE_INTERVAL);
    }

    /**
     * Disposes of the data set created by the WebProvider.
     *
     * @throws IOException
     */
    @Override
    public void dispose() {
        // Dispose of the data set if it exists.
        if (super.dataSet != null) {
            super.dataSet.dispose();
        }
        
        // Dispose of the temporary data file if it exists.
        if (this.sourceDataFile != null) {
            File tempFile = new File(this.sourceDataFile);
            if (tempFile.exists()) {
                tempFile.delete();
                logger.debug(String.format(
                        "Deleted temporary data file '%s'",
                        this.sourceDataFile));
            }
        }
        
        // Set the static active provider to null if it's the same as the
        // one being destroyed.
        if (WebProvider.activeProvider == this) {
            WebProvider.activeProvider = null;
        }
    }

    /**
     * @return a reference to the embedded provider.
     */
    public static WebProvider getEmbeddedProvider() {
        if (embeddedProvider == null) {
            synchronized (lock) {
                if (embeddedProvider == null) {
                    try {
                        embeddedProvider = new WebProvider();
                    } catch (IOException ex) {
                        logger.error(
                                "Exception creating web provider from embedded data",
                                ex);
                    }
                }
            }
        }
        return embeddedProvider;
    }

    /**
     * @param sc servlet context for the request.
     * @return a reference to the active provider.
     */
    public static WebProvider getActiveProvider(ServletContext sc) {
        if (activeProvider == null) {
            synchronized (lock) {
                if (activeProvider == null) {
                    activeProvider = create(sc);
                }
            }
        }
        return activeProvider;
    }

    /**
     *
     * @param sc
     * @return the binary file path from the configuration file
     */
    static File getBinaryFilePath(ServletContext sc) {
        String value = sc.getInitParameter(Constants.BINARY_FILE_PATH);
        return value == null ? null
                : new File(String.format(
                "%s%s%s",
                sc.getRealPath("WEB-INF"),
                File.separator,
                value));
    }

    /**
     * Cleans up any temporary files that remain from previous providers.
     */
    private static void cleanTemporaryFiles(File[] files) {
        try {
            for (final File file : files) {
                try {
                    if (file.delete() == false) {
                        logger.debug(String.format(
                                "Could not delete temporary file '%s'. It may be "
                                + "in user by another provider.",
                                file));
                    }
                } catch (SecurityException ex) {
                    logger.debug(String.format(
                            "Exception deleting temporary file '%s'",
                            file),
                            ex);
                }
            }
        } catch (Exception ex) {
            logger.warn(
                    "Exception cleaning temporary files",
                    ex);
        }
    }

    /**
     * Copies the source file to the destination file.
     *
     * @param source data file.
     * @param destination data file path.
     */
    private static void copyFile(final File source, final String destination)
            throws FileNotFoundException, IOException {
        final FileInputStream inStream = new FileInputStream(
                source.getAbsolutePath());
        final FileOutputStream outStream = new FileOutputStream(
                destination);
        final byte[] buffer = new byte[1024 ^ 2];
        int length;
        while ((length = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, length);
        }
        inStream.close();
        outStream.close();
    }

    /**
     * @param binaryFilePath to the source data file
     * @param tempDirectory
     * @return a temporary file name for the data file.
     */
    private static String getTempFileName(File tempDirectory, File binaryFilePath) {
        return tempDirectory.getAbsolutePath() + String.format(
                "\\%s.%s.tmp",
                binaryFilePath.getName(),
                UUID.randomUUID().toString());
    }
    
    private static Date getDataFileDate(String fileName) throws IOException {
        Dataset dataset = StreamFactory.create(fileName);
        return dataset.published;
    }
    
    /**
     * Gets the file path of a temporary data file for use with a stream provider.
     * 
     * This method will create a file if one does not already exist.
     * @param tempDirectory directory where temp files should be searched for
     * and created.
     * @param binaryFile the binary source file to get a temporary copy of.
     * @returns a file path to a temporary working file.
     */
        private static String getTempWorkingFile(File tempDirectory, File binaryFile)
                throws FileNotFoundException, IOException {
            String tempFilePath = null;
            if (binaryFile.exists())
            {
                String binaryFilePath = binaryFile.getAbsolutePath();
                String binaryName = binaryFile.getName();
                // Get the publish date of the master data file.
                Date masterDate = getDataFileDate(binaryFilePath);

                // Check if there are any other tmp files.
                File[] files = tempDirectory.listFiles();
                for (File file : files) {
                    String filePath = file.getAbsolutePath();
                    String fileName = file.getName();
                    
                    if(!filePath.equals(binaryFilePath) &&
                    filePath.startsWith(binaryName) &&
                        filePath.endsWith(".tmp"))
                    {
                        // Check if temp file matches date of the master file.
                        try
                        {
                            Date tempDate = getDataFileDate(filePath);
                            if (tempDate.equals(masterDate))
                            {
                                logger.info("Using existing temp data file with published data %s - \"%s\"",
                                        tempDate.toString(),
                                        filePath);
                                return fileName;
                            }
                        }
                        catch (Exception ex) // Exception may occur if file is not a 51Degrees file, no action is needed.
                        {
                            logger.info("Error while reading temporary data file \"%s\": %s",
                                    filePath,
                                    ex.getMessage());
                        }
                    }
                }
                
                // No suitable temp file was found, create one in the
                //App_Data folder to enable the source file to be updated
                // without stopping the web site.
                tempFilePath = getTempFileName(tempDirectory, binaryFile);

                // Copy the file to enable other processes to update it.
                copyFile(binaryFile, tempFilePath);
                logger.info("Created temp data file - \"%s\"", tempFilePath);
                
            }
            return tempFilePath;
        }

    /**
     * Forces the provider to update current ActiveProvider with new data.
     *
     * @param sc
     * @return
     */
    private static WebProvider create(ServletContext sc) {
        WebProvider provider = null;

        // Use the web-inf folder as the temporary folder.
        final File tempDirectory = new File(sc.getRealPath("WEB-INF"));

        // True if the data should be loaded into memory.
        final boolean memoryMode =
                "True".equalsIgnoreCase(
                sc.getInitParameter(Constants.MEMORY_MODE));

        // Get the binary file path from the configuration if it's provided
        // otherwise null.
        final File binaryFile = getBinaryFilePath(sc);

        // If the binary file path exists then try and clear previous
        // temporary files.
        if (binaryFile != null) {

            // Removes any old temporary files from previous providers.
            cleanTemporaryFiles(tempDirectory.listFiles(new FilenameFilter() {
                /**
                 * The regular expression used to evaluate temporary files.
                 */
                private final String filterRegex =
                        String.format("%s\\..+\\.tmp", binaryFile.getName());

                /**
                 * Returns true if the name of the file is a temporary file
                 * related to the master binary data file.
                 *
                 * @param dir
                 * @param name
                 * @return
                 */
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches(filterRegex);
                }
            }));

            try {
                // Does a binary file exist?
                if (binaryFile.exists()) {
                    if (memoryMode) {
                        logger.info(String.format(
                                "Creating memory provider from binary data file '%s'.",
                                binaryFile.getAbsolutePath()));
                        provider = new WebProvider(MemoryFactory.create(
                                binaryFile.getAbsolutePath()));
                    } else {
                        String tempFile = getTempWorkingFile(
                                tempDirectory,
                                binaryFile);
                        logger.info(String.format(
                                "Creating stream provider from binary data file '%s'.",
                                tempFile));
                        provider = new WebProvider(StreamFactory.create(tempFile));
                        
                        provider.sourceDataFile = tempFile;
                    }
                    logger.info(String.format(
                            "Created provider from binary data file '%s'.",
                            binaryFile.getAbsolutePath()));
                }
            } catch (Exception ex) {
                // Record the exception in the log file.
                logger.error(String.format(
                        "Exception processing device data from binary file '%s'. "
                        + "Enable debug level logging and try again to help identify"
                        + " cause.",
                        binaryFile),
                        ex);
                // Reset the provider to enable it to be created from the embedded 
                // data.
                provider = null;
            }
        }

        // Does the provider exist and has data been loaded?
        if (provider == null || provider.dataSet == null) {
            // No so initialise it with the embeddded binary data so at least 
            // we can do something.
            provider = getEmbeddedProvider();
        }

        return provider;
    }

    /**
     * Converts the request headers into a map and then passed the map to the
     * base implementation of the matcher.
     *
     * @param request
     * @return
     */
    public Match match(HttpServletRequest request) throws IOException {
        HashMap headers = new HashMap<String, String>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Add the first value for the header name, if any, to the map.
            Enumeration<String> headerValues = request.getHeaders(headerName);
            if (headerValues.hasMoreElements()) {
                headers.put(headerName, headerValues.nextElement());
            }
        }
        return super.match(headers);
    }

    /**
     * Obtains the match result from the request container, then the session and
     * then if not matched before performs a new match for the request storing
     * the result for future reference.
     *
     * @param request details of the HTTP request
     * @return a match object with properties associated with the device
     * @throws ServletException
     * @throws IOException
     */
    public static Map<String, String[]> getResult(final HttpServletRequest request)
            throws IOException {
        boolean hasOverrides = ProfileOverride.hasOverrides(request);
        Map<String, String[]> results = (Map<String, String[]>) request.getAttribute(RESULT_ATTIBUTE);
        //HttpSession session = request.getSession();

        // If there are no results already for the request, or there are 
        // overrides from client JavaScript then get the updated/new results.
        if (results == null || hasOverrides) {
            synchronized (request) {
                // Try getting the results again in case another thread has 
                // processed them and added to the request attributes.
                results = (Map<String, String[]>) request.getAttribute(RESULT_ATTIBUTE);
                if (results == null || hasOverrides) {

                    if (results == null) {
                        // Get the match and store the list of properties and 
                        // values in the context and session.
                        Match match = getActiveProvider(
                                request.getServletContext()).match(request);
                        if (match != null) {
                            // Allow other feautre detection methods to override
                            // priofiles.
                            ProfileOverride.override(request, match);
                            
                            request.setAttribute(RESULT_ATTIBUTE, match.getResults());
                            results = match.getResults();
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Forces a new active provider to be created by setting the current one to
     * null.
     */
    public static void refresh() {
        if (activeProvider != null) {
            synchronized (lock) {
                if (activeProvider != null) {
                    WebProvider oldProvider = activeProvider;
                    activeProvider = null;
                    oldProvider.dispose();
                }
            }
        }
    }
}
