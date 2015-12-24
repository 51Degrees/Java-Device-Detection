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
package fiftyone.mobile.detection;

import static fiftyone.mobile.detection.AutoUpdateStatus.*;
import fiftyone.mobile.detection.entities.Modes;
import fiftyone.mobile.detection.factories.CommonFactory;
import fiftyone.mobile.detection.factories.StreamFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;

/**
 * Used to fetch new device data from 51Degrees if a Premium or Enterprise 
 * licence key is available.
 * <p>
 * Requires a valid 51Degrees licence key and read/write access to the file
 * system folder where the downloaded file should be written.
 * <p>
 * Class only pulls the data file once per invocation and has no concept of the 
 * environment. For a sample implementation please see the AutoUpdate class in 
 * the 'Webapp' package.
 * <p>
 * When implementing custom auto updates keep in mind the following points:
 * <ul>
 *  <li>You need a licence key for the automatic update to work, 
 *  <a href="https://51degrees.com/compare-data-options">get a licence key</a>.
 *  <li>You need to implement two timers. The first timer should wake up the 
 *  second timer when the next update is ready (see below). The second timer 
 *  should attempt to download the data file by calling one of the update 
 *  functions in this class.
 *  <p>
 *  Please note that auto update will return a 
 *  {@link AutoUpdateStatus status code}. If the status code is 
 *  {@code AUTO_UPDATE_SUCCESS} then no further actions required, if the status 
 *  code is {@code AUTO_UPDATE_NOT_NEEDED} then no newer data is currently 
 *  available. All other codes indicate there is a problem with the update.
 *  <li>If you are using Stream mode you should not invoke automatic update 
 *  with the same file path as the one used for constructing the stream data 
 *  set. Stream data set retains a file lock on the underlying file to perform 
 *  detection lookups and the auto update will fail to replace the data file.
 *  Use a copy of the master file for device detection while leaving the master 
 *  data file free of locks.
 *  <li>Each data file (including 'Lite') contains a date when the next data 
 *  file will be released which can be accessed like: 
 *  <code>dataset.nextUpdate;</code>. The next update date is set by 51Degrees 
 *  when the data file gets generated. Use this date to avoid unnecessary 
 *  automatic update requests.
 *  <li>A licence key may be blacklisted, preventing further automatic updates 
 *  if we see excessive amount of traffic (i.e., if the amount of update 
 *  requests from your Web site or project becomes so large that it starts to 
 *  cause the quality of service to deteriorate for other clients).
 *  <p>
 *  If your key is blacklisted the update server will respond with 403 
 *  Forbidden. Please contact 51Degrees as soon as possible. We will not 
 *  blacklist your key without contacting you first.
 * </ul>
 */
public class AutoUpdate {
    
    /**
     * Stores critical data set attributes used to determine if the downloaded
     * data should be used to replace the current data file. Using this class
     * avoids the need for two Stream generated data sets to be held at
     * the same time reducing memory consumption.
     */
    private static class DataSetAttributes {
        /**
         * Date the data set was published.
         */
        final Date published;
        /**
         * Number of properties contained in the data set.
         */
        final int propertyCount;
        /**
         * Constructs a new instance of CriticalDataSetAttributes using the 
         * data set provided. Assumes the file passed to the constructor 
         * exists.
         * @param dataFile whose attributes should be copied.
         */
        DataSetAttributes(File dataFile) throws IOException {
            Dataset dataSet = StreamFactory.create(
                    dataFile.getAbsolutePath(), false);
            try {
                published = dataSet.published;
                propertyCount = dataSet.properties.size();
            }
            finally {
                dataSet.close();
            }
        }
    }
    
    /**
     * Used to obtain the lock for the critical section.
     */
    private static final Semaphore autoUpdateSignal = new Semaphore(1, true);
    
    /**
     * Size for the buffers used in this class.
     */
    private static final int INPUT_BUFFER = 4096;
    
    /**
     * Uses the given license key to perform a device data update, writing the
     * data to the file system and filling providers from this factory instance
     * with it.
     *
     * @param licenseKey the licence key to submit to the server
     * @param dataFilePath path to the device data file
     * @return the result of the update to enable user reporting
     * @throws java.io.FileNotFoundException if a file could not be found.
     * @throws java.security.NoSuchAlgorithmException if MD5 not available.
     */
    public static AutoUpdateStatus update(
            final String licenseKey, 
            String dataFilePath)
            throws FileNotFoundException, NoSuchAlgorithmException, 
                   IllegalArgumentException, Exception {
        return update(new String[]{licenseKey}, dataFilePath);
    }
    
    /**
     * Uses the given license keys to perform a device data update, writing the
     * data to the file system and filling providers from this factory instance
     * with it.
     * 
     * @param licenseKeys the licence key to use for the update request.
     * @param binaryFilePath where the original data file is located.
     * @return the result of the update to enable user reporting
     * @throws java.io.FileNotFoundException if a file could not be found.
     * @throws java.security.NoSuchAlgorithmException if MD5 is not available.
     */
    public static AutoUpdateStatus update(
            final String[] licenseKeys, 
            String binaryFilePath) 
            throws FileNotFoundException, NoSuchAlgorithmException, 
                   IllegalArgumentException, Exception {
        if (licenseKeys == null || licenseKeys.length == 0) {
            throw new IllegalArgumentException(
                "At least one valid licence key is required to update device " +
                "data. See https://51degrees.com/compare-data-options to " +
                "acquire valid licence keys.");
        }
        
        final String[] validKeys = getValidKeys(licenseKeys);
        if (validKeys.length == 0) {
            throw new IllegalArgumentException(
                "The license key(s) provided were invalid. See " +
                "https://51degrees.com/compare-data-options to acquire valid " +
                "licence keys.");
        }
        return download(validKeys, binaryFilePath);
    }
    
    /**
     * Downloads and updates the premium data file.
     * 
     * @param licenseKeys the licence key to use for the update request.
     * @param binaryFilePath where the original data file is located.
     * @return the result of the download to enable user reporting
     */
    private static AutoUpdateStatus download(
            String[] licenceKeys, 
            String binaryFilePath) 
            throws IOException, InterruptedException, Exception {
        AutoUpdateStatus result = AutoUpdateStatus.AUTO_UPDATE_IN_PROGRESS;
        
        // Set the three files needed to support the download, verification
        // and eventual activation.
        File binaryFile = new File(binaryFilePath);
        File compressedTempFile = getTempFileName(binaryFilePath);
        File uncompressedTempFile = getTempFileName(binaryFilePath);

        try {
            /*
            Acquire a lock so that only one thread can enter this critical 
            section at any given time. This is required to prevent multiple 
            threads from performing the update simultaneously, i.e. if more 
            than one thread is capable of invoking AutoUpdate.
            */
            autoUpdateSignal.acquire();
            
            // Download the device data, decompress, check validity and finally
            // replace the existing data file if all okay.
            HttpURLConnection client = 
                    (HttpsURLConnection)fullUrl(licenceKeys).openConnection();
            result = downloadFile(binaryFile, compressedTempFile, client);
            client.disconnect();
            
            if (result == AutoUpdateStatus.AUTO_UPDATE_IN_PROGRESS) {
                result = checkedDownloadedFileMD5(
                    client, 
                    compressedTempFile);
            }
            
            if (result == AutoUpdateStatus.AUTO_UPDATE_IN_PROGRESS) {
                result = decompress(compressedTempFile, uncompressedTempFile);
            }

            if (result == AutoUpdateStatus.AUTO_UPDATE_IN_PROGRESS) {
               result = validateDownloadedFile(binaryFile, uncompressedTempFile);
            }
            
            if (result == AutoUpdateStatus.AUTO_UPDATE_IN_PROGRESS) {
                result = activateDownloadedFile(client, 
                        binaryFile, uncompressedTempFile);
            }
        } finally {
            try {
                if (compressedTempFile.exists()) {
                    compressedTempFile.delete();
                }
                if (uncompressedTempFile.exists()) {
                    uncompressedTempFile.delete();
                }
            } finally {
                // No matter what, release the critical section lock.
                autoUpdateSignal.release();
            }
        }
        return result;
    }
   
    /**
     * Method performs the actual download by setting up and sending request and 
     * processing the response.
     * @param dataFile File object of the current data file.
     * @param compressedTempFile File object to write compressed downloaded 
     * content into.
     * @param client HTTP client configured with the download URL.
     * @return The current status of the overall process.
     */
    private static AutoUpdateStatus downloadFile(
            File binaryFile, 
            File compressedTempFile, 
            HttpURLConnection client) throws IOException {
        AutoUpdateStatus result = AUTO_UPDATE_IN_PROGRESS;

        // Set the last modified header if available from the current
        // binary data file.
        if (binaryFile.exists()) {
            client.setIfModifiedSince(binaryFile.lastModified());
        }

        // If the response is okay then download the file to the temporary
        // compressed data file. If not then set the response code 
        // accordingly.
        if (client.getResponseCode() == HttpsURLConnection.HTTP_OK) {
            InputStream inputStream = client.getInputStream();
            try {
                FileOutputStream outputStream = new FileOutputStream(
                    compressedTempFile);
                try {
                    downloadFile(inputStream, outputStream);
                }
                finally {
                    outputStream.close();
                }
            }
            finally {
                inputStream.close();
            }
        } else {
            //Server response was not 200. Data download can not commence.
            if(client.getResponseCode() == 429) {
                result = AUTO_UPDATE_ERR_429_TOO_MANY_ATTEMPTS;
            } else if (client.getResponseCode() == 304) {
                result = AUTO_UPDATE_NOT_NEEDED;
            } else if(client.getResponseCode() == 403) {
                result = AUTO_UPDATE_ERR_403_FORBIDDEN;
            } else {
                result = AUTO_UPDATE_HTTPS_ERR;
            }
        }
        
        return result;
    }
    
    /**
     * Verifies that the data has been downloaded correctly by comparing an MD5
     * hash off the downloaded data with one taken before the data was sent,
     * which is stored in a response header.
     *
     * @param client The Premium data download connection.
     * @param pathToFile path to compressed data file that has been downloaded.
     * @return True if the hashes match, else false.
     */
    private static AutoUpdateStatus checkedDownloadedFileMD5(
            final HttpURLConnection client, final File compressedTempFile) 
            throws NoSuchAlgorithmException, IOException {
        AutoUpdateStatus status = AUTO_UPDATE_IN_PROGRESS;
        final String serverHash = client.getHeaderField("Content-MD5");
        final String downloadHash = getMd5Hash(compressedTempFile);
        if (serverHash == null ||
            downloadHash.equals(serverHash) == false) {
            status = AUTO_UPDATE_ERR_MD5_VALIDATION_FAILED;
        }
        return status;
    }
    
    /**
     * Reads a source GZip file and writes the uncompressed data to destination 
     * file.
     * @param sourcePath path to GZip file to load from.
     * @param destinationPath path to file to write the uncompressed data to.
     * @returns current state of the update process
     * @throws IOException
     * @throws DataFormatException 
     */
    private static AutoUpdateStatus decompress(
            File sourcePath, 
            File destinationPath) throws IOException {
        AutoUpdateStatus status = AUTO_UPDATE_IN_PROGRESS;
        FileInputStream fis = new FileInputStream(sourcePath);
        try {
            FileOutputStream fos = new FileOutputStream(destinationPath);
            try {
                GZIPInputStream gzis = new GZIPInputStream(fis);
                try {
                    byte[] buffer = new byte[INPUT_BUFFER];
                    int len;
                    while ((len = gzis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                finally {
                    gzis.close();
                }
            }
            finally {
                fos.close();
            }
        }
        finally {
            fis.close();
        }
        return status;
    }
    
   /**
     * Method compares the downloaded data file to the existing data file to 
     * check if the update is required. This will prevent file switching if the 
     * data file was downloaded but is not newer than the existing data file.
     * 
     * The following conditions must be met for the data file to be considered 
     * newer than the current master data file:
     * 1. Current master data file does not exist.
     * 2. If the published dates are not the same.
     * 3. If the number of properties is not the same.
     * 
     * @param binaryFile the current file to compare against.
     * @param decompressedTempFile path to the decompressed downloaded file
     * @return current state of the update process
     */
    private static AutoUpdateStatus validateDownloadedFile(
            File binaryFile, 
            File decompressedTempFile) 
            throws IOException {
        AutoUpdateStatus status = AUTO_UPDATE_IN_PROGRESS;
        if (decompressedTempFile.exists()) {
            
            // This will throw an exception if the downloaded data file can't
            // be used to get the required attributes. The exception is a key
            // part of the validation process.
            DataSetAttributes tempAttrs = new DataSetAttributes(
                    decompressedTempFile);
            
            // If the current binary file exists then compare the two for the
            // same published date and same properties. If either value is 
            // different then the data file should be accepted. If they're the 
            // same then the update is not needed.
            if (binaryFile.exists()) {
                DataSetAttributes binaryAttrs = new DataSetAttributes(
                    binaryFile);
                if (binaryAttrs.published != tempAttrs.published ||
                    binaryAttrs.propertyCount != tempAttrs.propertyCount) {
                    status = AUTO_UPDATE_IN_PROGRESS;
                } else {
                    status = AUTO_UPDATE_NOT_NEEDED;
                }    
            }
        }
        return status;
    }    
    
    /**
     * Method represents the final stage of the auto update process. The 
     * uncompressed file is swapped in place of the existing master file.
     * @param client HttpURLConnection object to get the Last-Modified HTTP 
     * header value.
     * @param uncompressedTempFile File object containing the uncompressed 
     * version of the data file downloaded from 51Degrees update server.
     * @param binaryFile path to a binary data that should be set to the 
     * downloaded data
     * @return current state of the update process
     */
    private static AutoUpdateStatus activateDownloadedFile(
            HttpURLConnection client, 
            File binaryFile,
            File uncompressedTempFile) throws Exception {
        
        AutoUpdateStatus status = AUTO_UPDATE_IN_PROGRESS;
        boolean backedUp = true;
        File tempCopyofCurrentMaster = new File(
                binaryFile.getAbsolutePath() + ".replacing");
        try {
            // Keep a copy of the old data in case we need to go back to it.
            if (binaryFile.exists()) {
                backedUp = renameTo(binaryFile, tempCopyofCurrentMaster);
            }
            
            // If the backup of the master data file exists then switch the 
            // files.
            if (backedUp) {
                // Copy the new file to the master file.
                if (renameTo(uncompressedTempFile, binaryFile)) {
                    // Set the binary file's last modified date to the one 
                    // provided from the web server with the download. This
                    // date will be used when checking for future updates to
                    // avoid downloading the file if there is no update.
                    binaryFile.setLastModified(client.getLastModified());
                    status = AUTO_UPDATE_SUCCESS;
                }
                else {
                    status = AUTO_UPDATE_NEW_FILE_CANT_RENAME;
                }
            } 
            else {
                status = AUTO_UPDATE_MASTER_FILE_CANT_RENAME;
            }
            
        } catch (Exception ex) {
            if (binaryFile.exists() == false &&
                tempCopyofCurrentMaster.exists() == true) {
                renameTo(tempCopyofCurrentMaster, binaryFile);
            }
            throw ex;
        } finally {
            if (tempCopyofCurrentMaster.exists()) {
                tempCopyofCurrentMaster.delete();
            }
        }
        return status;
    }
    
    /**
     * Uses the provided input and output streams to download the device data.
     * @param inputStream connected to the input data source
     * @param outputStream connected to the output data destination
     * @throws IOException 
     */
    private static void downloadFile(
            InputStream inputStream,
            FileOutputStream outputStream) throws IOException {
        byte[] buffer = new byte[INPUT_BUFFER];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }        
    }
    
    /**
     * Creates a data set populate just with the header information. Used to get
     * the published date and the number of properties available.
     * @param binaryFile path to a binary data file uncompressed
     * @return data set with header data only loaded
     */
    private static Dataset getDataSetWithHeaderPopulated(File binaryFile) 
                                                            throws IOException {
        Dataset dataSet = null; 
        if (binaryFile.exists()) {
            dataSet = new Dataset(
                new Date(binaryFile.lastModified()),
                Modes.FILE);
            FileInputStream fileInputStream = new FileInputStream(binaryFile);
            try {
                BinaryReader reader = new BinaryReader(fileInputStream);
                try {
                    CommonFactory.loadHeader(dataSet, reader);
                }
                finally {
                    reader.close();
                }
            }
            finally {
                fileInputStream.close();
            }
        }        
        return dataSet;
    }
    
    /**
     * Validate the supplied keys to exclude keys from 3rd party products from 
     * being used.
     * 
     * @param licenseKeys an array of licence key strings to validate.
     * @return an array of valid licence keys.
     */
    private static String[] getValidKeys(final String[] licenseKeys) {
        final List<String> validKeys = new ArrayList<String>();
        for (String key : licenseKeys) {
            final Matcher m = 
                DetectionConstants.LICENSE_KEY_VALIDATION_REGEX.matcher(key);
            if (m.matches()) {
                validKeys.add(key);
            }
        }
        return validKeys.toArray(new String[validKeys.size()]);
    }
    
    /**
     * Constructs the URL needed to download Enhanced device data.
     *
     * @param licenseKeys Array of licence key strings.
     * @return Premium data download URL.
     * @throws MalformedURLException
     */
    private static URL fullUrl(String[] licenseKeys) 
            throws MalformedURLException {
        final String[] parameters = {
            "LicenseKeys=" + Utilities.joinString("|", licenseKeys),
            "Download=True",
            "Type=BinaryV32"};
        String url = String.format("%s?%s", DetectionConstants.AUTO_UPDATE_URL, 
                Utilities.joinString("&", parameters));
        return new URL(url);
    }
    
    /**
     * Calculates the MD5 hash of the given data array.
     *
     * @param fileToCheck calculate MD5 of this file.
     * @return The MD5 hash of the given data.
     */
    private static String getMd5Hash(File fileToCheck) 
            throws FileNotFoundException, NoSuchAlgorithmException, 
                   IOException {
        FileInputStream fis = new FileInputStream(fileToCheck);
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[INPUT_BUFFER];
            int bytesRead;

            //Get the md5 and format as a string.
            while((bytesRead = fis.read(buffer)) != -1) {
                md5.update(buffer, 0, bytesRead);
            }
            byte[] md5Bytes = md5.digest();
            StringBuilder hashBuilder = new StringBuilder();
            for (int i = 0; i < md5Bytes.length; i++) {
                hashBuilder.append(String.format("%02X ", md5Bytes[i]));
            }

            // The hash retrived from the responce header is in lower case with 
            // no spaces, must make sure this hash conforms to the scheme too.
            return hashBuilder.toString().toLowerCase(Locale.ENGLISH)
                    .replaceAll(" ", "");            
        }
        finally {
            fis.close();
        }
    }
    
    /**
     * Method initialises path to the a temporary file used during the auto 
     * update process.
     * <p>
     * The original data file does not have to exist, but the directory provided 
     * must exist and the path should not be a directory.
     * 
     * @param dataFilePath string path to the master data file.
     */
    private static File getTempFileName(String dataFilePath) {
        File dataFile = new File(dataFilePath);
        StringBuilder sb = new StringBuilder();
        sb.append(dataFile.getAbsolutePath());
        sb.append(".");
        sb.append(UUID.randomUUID());
        sb.append(".tmp");
        return new File(sb.toString());
    }
    
    /**
     * Renames the source file to the destination file.
     * <p>
     * Sometimes the source file may still be locked by a previous memory
     * mapped file operation. In such instances the file can not be renamed. 
     * The method will try to rename the file 10 times forcing garbage 
     * collection if possible after each failed attempt. If the file still
     * can't be renamed then false will be returned.
     * 
     * @param sourceFile file to be renamed
     * @param destFile destination file name
     * @return true if the source file was renamed, otherwise false.
     */
    private static boolean renameTo(File sourceFile, File destFile) {
        boolean result = false;
        int iterations = 0;
        while (sourceFile.exists() &&
               iterations < 10) {
            result = sourceFile.renameTo(destFile);
            if (result == false) {
                System.gc();
                iterations++;
            }
        }
        return result;
    }
}
