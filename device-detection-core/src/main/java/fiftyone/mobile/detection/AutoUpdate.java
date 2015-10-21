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
import fiftyone.mobile.detection.factories.StreamFactory;
import fiftyone.properties.DetectionConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;

/**
 * Used to fetch new device data from 51Degrees.com if a Premium or Enterprise 
 * licence has been installed.
 */
public class AutoUpdate {
    /**
     * Maximum number of threads allowed inside the critical section.
     */
    private static final int THREADS = 1;
    /**
     * Used to obtain the lock for the critical section.
     */
    private static final Semaphore autoUpdateSignal = new Semaphore(THREADS, true);
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
        
        AutoUpdateStatus status = AUTO_UPDATE_IN_PROGRESS;
        // If a valid license key exists then proceed
        final String[] validKeys = getValidKeys(licenseKeys);
        if (validKeys.length > 0) {
            // Download and verify the data. Return the result.
            status = download(validKeys, dataFilePath);
        } else {
            throw new AutoUpdateException(
                    "The license key(s) provided were invalid.");
        }
        boolean result = true;
        if (status != AUTO_UPDATE_SUCCESS) {
            result = false;
        }
        return result;
    }
    
    /**
     * Downloads and updates the premium data file.
     * 
     * @param licenceKeys
     * @param dataFilePath
     * @return 
     */
    private static AutoUpdateStatus download(   String[] licenceKeys, 
                                                String dataFilePath ) 
                                                throws AutoUpdateException {
        AutoUpdateStatus status = AUTO_UPDATE_IN_PROGRESS;
        
        File dataFile = new File(dataFilePath);
        if (dataFile.isDirectory() == true) {
            return AUTO_UPDATE_MASTER_FILE_IS_DIR;
        }
        File compressedTempFile = getTempFileName(dataFilePath);
        File uncompressedTempFile = new File(dataFilePath+".new");
        HttpURLConnection client = null;
        try {
            /*
            Acquire a lock so that only one thread can enter this critical 
            section at any given time. This is required to prevent multiple 
            threads from performing the update simultaneously, i.e. if more 
            than one thread is capable of invoking AutoUpdate.
            */
            autoUpdateSignal.acquire();
            client = (HttpsURLConnection) fullUrl(licenceKeys).openConnection();
            
            status = downloadFile(dataFile, compressedTempFile, licenceKeys, client);
            
            // Validate the MD5 hash of the download.
            if (status == AUTO_UPDATE_IN_PROGRESS) {
                status = checkDownloadedFileMD5(client, compressedTempFile.getAbsolutePath());
            }
            
            // Decompress the data file ready to create the data set.
            if (status == AUTO_UPDATE_IN_PROGRESS) {
                status = decompress(compressedTempFile.getAbsolutePath(), 
                                    uncompressedTempFile.getAbsolutePath());
            }
            
            // Validate that the data file can be used to create a provider.
            if (status == AUTO_UPDATE_IN_PROGRESS) {
                status = validateDownloadedFile(dataFile, 
                                    uncompressedTempFile);
            }
            
            // Replace the data file downloaded for future use.
            if (status == AUTO_UPDATE_IN_PROGRESS) {
                status = activateDownloadedFile(client, uncompressedTempFile, dataFile);
            }
            
        } catch (InterruptedException ex) {
            status = AUTO_UPDATE_GENERIC_FAILURE;
            throw new AutoUpdateException(ex.getMessage());
        } catch (MalformedURLException ex) {
            status = AUTO_UPDATE_HTTPS_ERR;
            throw new AutoUpdateException(ex.getMessage());
        } catch (IOException ex) {
            status = AUTO_UPDATE_ERR_READING_STREAM;
            throw new AutoUpdateException(ex.getMessage());
        } finally {
            try {
                // Try releasing resources.
                if (client != null) {
                    client.disconnect();
                }
                compressedTempFile.delete();
                uncompressedTempFile.delete();
            } finally {
                // No matter what, release the critical section lock.
                autoUpdateSignal.release();
            }
        }
        return status;
    }
    
    /**
     * Method performs the actual download by setting up and sending request and 
     * processing the response.
     * @param dataFile File object of the current data file.
     * @param compressedTempFile File object to write compressed downloaded 
     * content into.
     * @param licenceKeys Array of licence key strings to use with auto update 
     * request.
     * @return The current status of the overall process.
     */
    private static AutoUpdateStatus downloadFile(   File dataFile, 
                                                    File compressedTempFile, 
                                                    String[] licenceKeys,
                                                    HttpURLConnection client ) 
                                                    throws AutoUpdateException {
        
        long lastModified = getLastModified(dataFile);
        AutoUpdateStatus status = AUTO_UPDATE_IN_PROGRESS;
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        byte[] buffer = new byte[INPUT_BUFFER];
        
        try {
            
            // Set the last modified, if available.
            if (lastModified != -1) {
                final Date modifiedDate = new Date(lastModified);
                final SimpleDateFormat dateFormat = 
                        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                client.setRequestProperty("Last-Modified", 
                        dateFormat.format(modifiedDate));
            }
            
            // If data is available then see if it's a new data file.
            if (client.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                //Allocate resources for the download.
                inputStream = client.getInputStream();
                outputStream = new FileOutputStream(
                        compressedTempFile.getAbsolutePath());
                int bytesRead = -1;
                
                // Transfer data.
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } else {
                //Server response was not 200. Data download can not commence.
                if(client.getResponseCode() == 429) {
                    status = AUTO_UPDATE_ERR_429_TOO_MANY_ATTEMPTS;
                } else if (client.getResponseCode() == 304) {
                    status = AUTO_UPDATE_NOT_NEEDED;
                } else if(client.getResponseCode() == 403) {
                    status = AUTO_UPDATE_ERR_403_FORBIDDEN;
                } else {
                    status = AUTO_UPDATE_HTTPS_ERR;
                }
            }
        } catch (MalformedURLException ex) {
            status = AUTO_UPDATE_HTTPS_ERR;
            throw new AutoUpdateException(ex.getMessage());
        } catch (IOException ex) {
            status = AUTO_UPDATE_ERR_READING_STREAM;
            throw new AutoUpdateException(ex.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    throw new AutoUpdateException(ex.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    throw new AutoUpdateException(ex.getMessage());
                }
            }
            buffer = null;
        }
        return status;
    }
    
    /**
     * Method returns the last Last-Modified timestamp if the data file exists, 
     * can be read and is not a Lite data file. Given a valid licence key, the 
     * Lite data file will be replaced in any case, so no Last-Modified is 
     * necessary in the request.
     * 
     * @param dataFile File object of current master data file.
     * @return Last-Modified timestamp as long.
     */
    private static long getLastModified(File dataFile) throws AutoUpdateException {
        long lastModified = -1;
        Dataset oldDataset = null;
        try {
            if (dataFile.exists()) {
                oldDataset = StreamFactory.create(
                        dataFile.getAbsolutePath(), false);
                if (!oldDataset.getName().contains("Lite")) {
                    lastModified = dataFile.lastModified();
                }
                oldDataset.close();
            }
        } catch (IOException ex) {
            throw new AutoUpdateException(ex.getMessage());
            // Do nothing. There was a problem reading the data, but in this
            // context we don't care as we only need to return last modified.
        } finally {
            if (oldDataset != null) {
                try {
                    oldDataset.close();
                } catch (IOException ex) {
                    throw new AutoUpdateException(ex.getMessage());
                }
            }
        }
        return lastModified;
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
            "Type=BinaryV32"};
        String url = String.format("%s?%s", DetectionConstants.AUTO_UPDATE_URL, 
                joinString("&", parameters));
        return new URL(url);
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
    private static AutoUpdateStatus checkDownloadedFileMD5(
            final HttpURLConnection client, String pathToFile) throws AutoUpdateException {
        AutoUpdateStatus status = AUTO_UPDATE_IN_PROGRESS;
        try {
            final String serverHash = client.getHeaderField("Content-MD5");
            final String downloadHash = getMd5Hash(pathToFile);
            
            if (serverHash.equals(downloadHash) == false) {
                status = AUTO_UPDATE_ERR_MD5_VALIDATION_FAILED;
            }
        } catch (NoSuchAlgorithmException ex) {
            status = AUTO_UPDATE_GENERIC_FAILURE;
            throw new AutoUpdateException(ex.getMessage());
        } catch (IOException ex) {
            status = AUTO_UPDATE_DATA_INVALID;
            throw new AutoUpdateException(ex.getMessage());
        }
        return status;
    }
    
    /**
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
            byte[] buffer = new byte[INPUT_BUFFER];
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
     * Method joins given number of strings separating each by the specified 
     * separator. Used to construct the update URL.
     * 
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
     * Reads a source GZip file and writes the uncompressed data to destination 
     * file.
     * @param sourcePath path to GZip file to load from.
     * @param destinationPath path to file to write the uncompressed data to.
     * @throws IOException
     * @throws DataFormatException 
     */
    private static AutoUpdateStatus decompress( String sourcePath, 
                                                String destinationPath) throws AutoUpdateException {
        //Allocate resources.
        AutoUpdateStatus status = AUTO_UPDATE_IN_PROGRESS;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        GZIPInputStream gzis = null;
        byte[] buffer = null;
        
        try {
            fis = new FileInputStream(sourcePath);
            fos = new FileOutputStream(destinationPath);
            gzis = new GZIPInputStream(fis);
            buffer = new byte[INPUT_BUFFER];
            int len = 0;

            //Extract compressed content.
            while ((len = gzis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } catch (FileNotFoundException ex) {
            status = AUTO_UPDATE_ERR_READING_STREAM;
            throw new AutoUpdateException(ex.getMessage());
        } catch (IOException ex) {
            status = AUTO_UPDATE_ERR_READING_STREAM;
            throw new AutoUpdateException(ex.getMessage());
        } finally {
            // Release resources.
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    throw new AutoUpdateException(ex.getMessage());
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    throw new AutoUpdateException(ex.getMessage());
                }
            }
            if (gzis != null) {
                try {
                    gzis.close();
                } catch (IOException ex) {
                    throw new AutoUpdateException(ex.getMessage());
                }
            }
            buffer = null;
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
     * 1. Current master data file does not exist, hence the downloaded file is 
     * newer.
     * 2. If a Dataset could not be constructed from the old data file.
     * 3. If the published dates are not the same.
     * 4. If the number of properties is not the same.
     * 
     * @param dataFile
     * @param decompressedTempFile
     * @return 
     */
    private static AutoUpdateStatus validateDownloadedFile(File dataFile, 
                                                File decompressedTempFile) throws AutoUpdateException {
        AutoUpdateStatus status = AUTO_UPDATE_IN_PROGRESS;
        
        Dataset oldDataset = null;
        Dataset newDataset = null;
        
        try {
            if (dataFile.exists() == true) {
                oldDataset = StreamFactory.create(
                        dataFile.getAbsolutePath(), false);
                newDataset = StreamFactory.create(
                        decompressedTempFile.getAbsolutePath(), false);
                
                if (    oldDataset == null ||
                        oldDataset.published != newDataset.published ||
                        oldDataset.getProperties().size() != 
                                    newDataset.getProperties().size()) {
                    status = AUTO_UPDATE_IN_PROGRESS;
                } else {
                    status = AUTO_UPDATE_NOT_NEEDED;
                }
            }
            //If the data file does not exist, then nothing to compare.
        } catch (IOException ex) {
            status = AUTO_UPDATE_ERR_READING_STREAM;
            throw new AutoUpdateException(ex.getMessage());
        } finally {
            if (oldDataset != null) {
                try {
                    oldDataset.close();
                } catch (IOException ex) {
                    throw new AutoUpdateException(ex.getMessage());
                }
            }
            if (newDataset != null) {
                try {
                    newDataset.close();
                } catch (IOException ex) {
                    throw new AutoUpdateException(ex.getMessage());
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
     * @param dataFile File object of the current master data file to be 
     * replaced with a newer version.
     * @return 
     */
    private static AutoUpdateStatus activateDownloadedFile(
            HttpURLConnection client, 
            File uncompressedTempFile, 
            File dataFile) throws AutoUpdateException {
        
        AutoUpdateStatus status = AUTO_UPDATE_SUCCESS;
        String tempPath = dataFile.getAbsolutePath()+".tmp";
        File currentMasterFileTempCopy = new File(tempPath);
        Dataset newDataset = null;
        boolean backUpComplete = true;
        
        try {
            //Move current master data file to temp copy in case we may need it.
            if (dataFile.exists()) {
                if (currentMasterFileTempCopy.exists()) {
                    currentMasterFileTempCopy.delete();
                }
                if (dataFile.renameTo(currentMasterFileTempCopy) == false) {
                    throw new AutoUpdateException("Failed to move file.");
                }
            }

            // Either the master data file was successfully renamed, or 
            // the data file does not exist.
            if (status == AUTO_UPDATE_SUCCESS) {
                if (uncompressedTempFile.renameTo(dataFile) == false) {
                    status = AUTO_UPDATE_ERR_FILE_RENAME_FAILED;
                } else {
                    // Sets the last modified time of the file downloaded to the 
                    // one provided in the HTTP header, or if not valid then the 
                    // published date of the data set.
                    long lastModified = client.getLastModified();
                    if (lastModified == 0) {
                        newDataset = StreamFactory.create(  
                                dataFile.getAbsolutePath(), 
                                false);
                        lastModified = newDataset.published.getTime();
                    }
                    dataFile.setLastModified(lastModified);
                }
            }
        } catch (Exception ex) {
            status = AUTO_UPDATE_GENERIC_FAILURE;
            throw new AutoUpdateException(ex.getMessage());
        } finally {
            if (newDataset != null) {
                try {
                    newDataset.close();
                } catch (IOException ex) {
                    throw new AutoUpdateException(ex.getMessage());
                }
            }
        }
        return status;
    }
    
    /**
     * 
     * @param source
     * @param destination
     * @return 
     */
    private static boolean copyTo(File source, File destination) {
        InputStream inStream = null;
	OutputStream outStream = null;
        boolean result = true;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(destination);
            byte[] buffer = new byte[INPUT_BUFFER]; 
            int length;
            
            while((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
            
        } catch (FileNotFoundException ex) {
            result = false;
        } catch (IOException ex) {
            result = false;
        } finally {
            if (inStream == null) {
                try {
                    inStream.close();
                } catch (IOException ex) {
                    
                }
            }
            if (outStream == null) {
                try {
                    outStream.close();
                } catch (IOException ex) {
                    
                }
            }
        }
        return result;
    }
}
