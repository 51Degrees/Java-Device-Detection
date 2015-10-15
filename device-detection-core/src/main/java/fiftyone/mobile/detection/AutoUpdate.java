/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

import fiftyone.mobile.detection.factories.StreamFactory;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;

/*
 * Used to fetch new device data from 51Degrees.com if a Premium or Enterprise 
 * licence has been installed.
 */
public class AutoUpdate {

    //Path to the compressed data file.
    private static String compressedTempFile = "";
    //Path to the uncompressed data file.
    private static String uncompressedTempFile = "";
    
    /**
     * Implements the main update logic. First the paths to temporary data files 
     * are initialised. Then a request is made to 51Degrees.com to check for an 
     * updated data file. One of the request headers is set to the last-modified 
     * date of the data file (if any). If the local data file is already of the 
     * latest version, then a 304 header 'Not Modified' is returned. Otherwise 
     * the file is downloaded in to a temporary location and uncompressed. The 
     * temporary file is then deleted. New data file is then validated and a 
     * check is carried out to determine if the old data file needs to be 
     * replaced. Finally, if the data file is replaced if required.
     *
     * @returns True if all stages completed successfully, False otherwise.
     * @param dataFilePath string representing path to 51Degrees data file.
     * @param licenseKeys An array of licence keys with at least one entry 
     * represented as strings.
     */
    private static boolean getNewDataset(   final String[] licenseKeys, 
                                            final String dataFilePath ) throws 
                                                    AutoUpdateException, 
                                                    FileNotFoundException, 
                                                    NoSuchAlgorithmException {
        try {
            //Initialize paths to temporary files.
            initTempFiles(dataFilePath);
            // Try to get the date the data was last modified. No existent files
            // or lite data do not need dates.
            final File oldDataFile = new File(dataFilePath);
            long lastModified = -1;
            if (oldDataFile.exists()) {
                final Dataset oldDataset = StreamFactory.create(dataFilePath, false);
                if (!oldDataset.getName().contains("Lite")) {
                    lastModified = oldDataFile.lastModified();
                }
                oldDataset.close();
            }
            System.gc();
            // Download the data to the temporary data file.
            if (!download(licenseKeys, lastModified, compressedTempFile)) {
                throw new AutoUpdateException("Download failed");
            }
            //Decompress data.
            decompressData(compressedTempFile, uncompressedTempFile);
            //Delete compressed file.
            File compressedFile = new File(compressedTempFile);
            if (compressedFile.delete() == false) {
                throw new AutoUpdateException("Auto update warning: could not "
                        + "delete the compressed temporary data file used for "
                        + "storing data downloaded from 51Degrees. Should not "
                        + "prevent the data file from updating.");
            }
            // Create a dataset and load the data in.
            final Dataset newDataSet = StreamFactory.create(uncompressedTempFile, true);
            //Test the new data and check if old one needs to be replaced.
            boolean copyFile = true;
            final File dataFile = new File(dataFilePath);
            // Confirm the new data is newer than current.
            if (dataFile.exists()) {
                final Dataset currentDataSet = StreamFactory.create(dataFilePath, false);
                copyFile = newDataSet.published.getTime() > currentDataSet.published.getTime() || 
                        !newDataSet.getName().equals(currentDataSet.getName());

                currentDataSet.close();
            }
            newDataSet.close();
            System.gc();
            //If the downloaded file is either newer, or has a different name.
            if (copyFile) {
                //Copy new file re-writing the contents of the current.
                File source = new File(uncompressedTempFile);
                File destination = new File(dataFilePath);
                
                boolean moved = source.renameTo(destination);
                if (!moved) {
                    throw new AutoUpdateException("Auto update failed: Could "
                            + "not replace existing data file with new one. "
                            + "Please verify the original data file is not used "
                            + "elsewhere in your code.");
                }
                source = null;
                destination = null;
                //Try to delete temp file.
                File tempMasterFile = new File(uncompressedTempFile);
                int count = 5;
                while (!tempMasterFile.delete()) {
                    if (count <= 0) {
                        throw new AutoUpdateException("Auto update warning: "
                                + "failed to delete temporary file used to "
                                + "store the uncompressed device data. This "
                                + "error is not critical for the auto update. "
                                + "Path to problem file: " 
                                + tempMasterFile.getAbsolutePath());
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        
                    } finally {
                        count--;
                    }
                }
                return true;
            } else {
                //No need to update. File names are the same. Dates of both 
                //files do not indicate an update is required.
                File f = new File(uncompressedTempFile);
                if (f.exists())
                    f.delete();
                return false;
            }
        } catch (IOException ex) {
            throw new AutoUpdateException(String.format(
                    "Exception reading data stream from server '%s'.",
                    DetectionConstants.AUTO_UPDATE_URL) + ex.getMessage());
        } catch (DataFormatException ex) {
            throw new AutoUpdateException("Auto update error: the data file "
                    + "downloaded from 51Degrees appears to be of the "
                    + "unsupported format. " 
                    + ex.getMessage());
        }
    }

    /**
     *
     * Calculates the MD5 hash of the given data array.
     *
     * @param pathToFile calculate md5 of this file.
     * @return The MD5 hash of the given data.
     */
    private static String getMd5Hash(String pathToFile) 
                            throws  FileNotFoundException, 
                                    NoSuchAlgorithmException, 
                                    IOException {
        FileInputStream fis = null;
        MessageDigest md5 = null;
        try {
            //Allocate resources.
            fis = new FileInputStream(pathToFile);
            md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[2048];
            int bytesRead = -1;

            //Get the md5 and format as a string.
            while((bytesRead = fis.read(buffer)) != -1) {
                md5.update(buffer, 0, bytesRead);
            }
            byte[] md5Bytes = md5.digest();
            StringBuilder hashBuilder = new StringBuilder();
            for (int i = 0; i < md5Bytes.length; i++) {
                hashBuilder.append(String.format("%02X ", md5Bytes[i]));
            }
            md5Bytes = null;
            buffer = null;
            // The hash retrived from the responce header is in lower case with 
            // no spaces, must make sure this hash conforms to the scheme too.
            return hashBuilder.toString().toLowerCase().replaceAll(" ", "");
        } finally {
            //Release FileInputStream
            if (fis != null) {
                fis.close();
            }
            //Release MD5
            if (md5 != null) {
                md5 = null;
            }
        }
    }

    /**
     *
     * Verifies that the data has been downloaded correctly by comparing an MD5
     * hash off the downloaded data with one taken before the data was sent,
     * which is stored in a response header.
     *
     * @param client The Premium data download connection.
     * @param pathToFile path to compressed data file that has been downloaded.
     * @return True if the hashes match, else false.
     */
    private static boolean validateMD5( final HttpURLConnection client,
                                        String pathToFile) throws 
                                                    FileNotFoundException, 
                                                    NoSuchAlgorithmException, 
                                                    IOException {
        final String serverHash = client.getHeaderField("Content-MD5");
        final String downloadHash = getMd5Hash(pathToFile);
        return serverHash != null && serverHash.equals(downloadHash);
    }

    /**
     * Method joins given number of strings separating each by the specified 
     * separator. Used to construct the update URL.
     * @param seperator what separates the strings.
     * @param strings strings to join.
     * @return all of the strings combined in to one and separated by separator.
     */
    private static String joinString(final String seperator, final String[] strings) {
        final StringBuilder sb = new StringBuilder();
        int size = strings.length;
        for (int i = 0; i < size; i++) {
            sb.append(strings[i]);
            if (i < size - 1) {
                sb.append(seperator);
            }
        }
        return sb.toString();
    }

    /**
     * Constructs the URL needed to download Premium data.
     *
     * @param licenseKeys Array of licence key strings.
     * @return Premium data download URL.
     * @throws MalformedURLException
     */
    private static URL fullUrl(String[] licenseKeys) throws MalformedURLException {

        final String[] parameters = {
            "LicenseKeys=" + joinString("|", licenseKeys),
            "Download=True",
            "Type=BinaryV3"};
        String url = String.format("%s?%s", DetectionConstants.AUTO_UPDATE_URL, 
                joinString("&", parameters));
        return new URL(url);
    }

    /**
     * Uses the given license key to perform a device data update, writing the
     * data to the file system and filling providers from this factory instance
     * with it.
     *
     * @param licenseKey the licence key to submit to the server
     * @param dataFilePath path to the device data file
     * @return true for a successful update. False can indicate that data was
     * unavailable, corrupt, older than the current data or not enough memory
     * was available. In that case the current data is used.
     * @throws AutoUpdateException exception detailing problem during the update
     * @throws java.io.FileNotFoundException
     * @throws java.security.NoSuchAlgorithmException
     */
    public static boolean update(   final String licenseKey, 
                                    String dataFilePath ) throws 
                                                AutoUpdateException, 
                                                FileNotFoundException, 
                                                NoSuchAlgorithmException {
        return update(new String[]{licenseKey}, dataFilePath);
    }

    /**
     * Uses the given license key to perform a device data update. This method 
     * allows you to specify the location of the original data file as well as 
     * the two temporary data files used to store the data at intermediate 
     * stages of the update.
     * 
     * @param licenseKey the licence key to use for the update request.
     * @param dataFilePath where the original data file is located.
     * @param compressedFile where the compressed data file should be located.
     * @param uncompressedFile where the uncompressed data file should be located.
     * @return True if update was successful, False otherwise.
     * @throws AutoUpdateException 
     * @throws java.io.FileNotFoundException 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public static boolean update(   final String licenseKey, 
                                    String dataFilePath, 
                                    String compressedFile, 
                                    String uncompressedFile ) throws 
                                                    AutoUpdateException, 
                                                    FileNotFoundException, 
                                                    NoSuchAlgorithmException {
        compressedTempFile = compressedFile;
        uncompressedTempFile = uncompressedFile;
        return update(new String[]{licenseKey}, dataFilePath);
    }
    
    /**
     * Uses the given license key to perform a device data update, writing the
     * data to the file system and filling providers from this factory instance
     * with it.
     *
     * @param licenseKeys the licence keys to submit to the server
     * @param dataFilePath path to device data file
     * @return true for a successful update. False can indicate that data was
     * unavailable, corrupt, older than the current data or not enough memory
     * was available. In that case the current data is used.
     * @throws AutoUpdateException exception detailing problem during the update
     * @throws java.io.FileNotFoundException
     * @throws java.security.NoSuchAlgorithmException
     */
    public static boolean update(   final String[] licenseKeys, 
                                    String dataFilePath ) throws 
                                                    AutoUpdateException, 
                                                    FileNotFoundException, 
                                                    NoSuchAlgorithmException {

        if (licenseKeys == null || licenseKeys.length == 0) {
            throw new AutoUpdateException(
                    "Device data cannot be updated without a licence key.");
        }

        // If a valid license key exists then proceed
        final String[] validKeys = getValidKeys(licenseKeys);
        if (validKeys.length > 0) {
            // Download and verify the data. Return the result.
            return getNewDataset(validKeys, dataFilePath);
        } else {
            throw new AutoUpdateException(
                    "The license key(s) provided were invalid.");
        }
    }

    /**
     * Validate the supplied keys to exclude keys from 3rd party products from 
     * being used.
     * @param licenseKeys an array of licence key strings to validate.
     * @return an array of valid licence keys.
     */
    private static String[] getValidKeys(final String[] licenseKeys) {
        final List<String> validKeys = new ArrayList<String>();
        for (String key : licenseKeys) {
            final Matcher m = DetectionConstants.LICENSE_KEY_VALIDATION_REGEX.matcher(key);
            if (m.matches()) {
                validKeys.add(key);
            }
        }
        return validKeys.toArray(new String[validKeys.size()]);
    }

    /**
     * Downloads and validates data, returning a byte array or null if download
     * or validation was unsuccessful.
     *
     * @param licenseKeys an array of keys to fetch a new data file with.
     * @return a decompressed byte array containing the data.
     */
    private static boolean download(final String[] licenseKeys, 
                                    long lastModified, 
                                    String pathToTempFile) throws 
                                                    AutoUpdateException, 
                                                    FileNotFoundException, 
                                                    NoSuchAlgorithmException,
                                                    IOException {
        //Declare resources so that they can be released in finally block.
        HttpURLConnection client = null;
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            // Open the connection to download the latest data file.
            client = (HttpsURLConnection) fullUrl(licenseKeys).openConnection();

            // Check if a date has been supplied and send it
            if (lastModified != -1) {
                final Date modifiedDate = new Date(lastModified);
                final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                client.setRequestProperty("Last-Modified", dateFormat.format(modifiedDate));
            }

            // If data is available then see if it's a new data file.
            if (client.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                //Allocate resources for the download.
                inputStream = client.getInputStream();
                outputStream = new FileOutputStream(pathToTempFile);
                byte[] buffer = new byte[4096];
                int bytesRead = -1;
                
                //Download.
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                //Release resources.
                outputStream.close();
                inputStream.close();
                buffer = null;
                
                // now validate with md5 hash
                if (validateMD5(client, pathToTempFile)) {
                    return true;
                } else {
                    throw new AutoUpdateException("Hash validation failed. "
                            + "Hash of the downloaded data file does not equal "
                            + "to the control hash provided as part of the "
                            + "server response.");
                }
            } else {
                //Server response was not 200. Data download can not commence.
                StringBuilder message = new StringBuilder();
                message.append("Could not commence data file download. ");
                if(client.getResponseCode() == 429) {
                    message.append("Server response: 429 -  ");
                    message.append("too many download attempts.");
                } else if (client.getResponseCode() == 304) {
                    message.append("Server response: 304 - not modified. ");
                    message.append("You already have the latest data.");
                } else if(client.getResponseCode() == 403) {
                    message.append("Server response: 403 - forbidden. ");
                    message.append("Your key is blacklisted. Please contact ");
                    message.append("51Degrees support as soon as possible.");
                } else {
                    message.append("Server response: ");
                    message.append(client.getResponseCode());
                }
                throw new AutoUpdateException(message.toString());
            }
        } catch (IOException ex) {
            throw new AutoUpdateException("Device data download failed: " 
                    + ex.getMessage());
        } finally {
            //Release resources.
            if (client != null) {
                client.disconnect();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Reads a source GZip file and writes the uncompressed data to destination 
     * file.
     * @param sourcePath path to GZip file to load from.
     * @param destinationPath path to file to write the uncompressed data to.
     * @throws IOException
     * @throws DataFormatException 
     */
    private static void decompressData(String sourcePath, String destinationPath) 
            throws IOException, DataFormatException {
        //Allocate resources.
        FileInputStream fis = new FileInputStream(sourcePath);
        FileOutputStream fos = new FileOutputStream(destinationPath);
        GZIPInputStream gzis = new GZIPInputStream(fis);
        byte[] buffer = new byte[1024];
        int len = 0;
        
        //Extract compressed content.
        while ((len = gzis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        
        //Release resources.
        fos.close();
        fis.close();
        gzis.close();
        buffer = null;
    }
    
    /**
     * Method initialises path to the two temporary files used during the auto 
     * update process. Depending on the access method used, the data files can 
     * be set by the user in which case this method will do nothing. If the user 
     * does not set the paths, then a path  will be derived from the path of the 
     * original data file.
     * 
     * The original data file does not have to exist, but the directory provided 
     * must exist and the path should not be a directory.
     * 
     * @param originalFile string path to the master (original) data file.
     * @throws AutoUpdateException if directory is provided instead of file.
     */
    private static void initTempFiles(String originalFile) throws AutoUpdateException {
        //Derive compressed data file path from original data fiel path.
        if (compressedTempFile.isEmpty()) {
            File dataFile = new File(originalFile);
            if (!dataFile.isDirectory()) {
                StringBuilder sb = new StringBuilder();
                sb.append(dataFile.getAbsolutePath());
                sb.append(".");
                sb.append(UUID.randomUUID());
                sb.append(".tmp");
                compressedTempFile = sb.toString();
            } else {
                throw new AutoUpdateException("Auto update failed, could not "
                        + "create a temporary compressed file as path to the "
                        + "original file appears to be a directory and not a "
                        + "file.");
            }
        }
        if (uncompressedTempFile.isEmpty()) {
            File dataFile = new File(originalFile);
            if (!dataFile.isDirectory()) {
                StringBuilder sb = new StringBuilder();
                sb.append(dataFile.getAbsolutePath());
                sb.append(".new");
                uncompressedTempFile = sb.toString();
            } else {
                throw new AutoUpdateException("Auto update failed, could not "
                        + "create a temporary uncompressed file as path to the "
                        + "original file appears to be a directory and not a "
                        + "file.");
            }
        }
    }
}
