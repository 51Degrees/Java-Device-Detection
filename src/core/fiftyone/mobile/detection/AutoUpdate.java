package fiftyone.mobile.detection;

import fiftyone.mobile.detection.factories.StreamFactory;
import fiftyone.properties.DetectionConstants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;

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

/*
 * Used to fetch new device data from 51Degrees.mobi if a premium licence has
 * been installed.
 */
public class AutoUpdate {

    /**
     * Downloads the latest Premium data and saves to disk if the data has been
     * downloaded correctly and is newer than data currently in that position
     * (if any) in that path.
     *
     * @returns a provider which has passed verification, or null if there was
     * no new data or the data provided failed validation.
     */
    private static Dataset getNewDataset(final String[] licenseKeys, final String dataFilePath) throws AutoUpdateException {
        try {
            // Try to get the date the data was last modified. No existent files
            // or lite data do not need dates.
            final File oldDataFile = new File(dataFilePath);
            long lastModified = -1;
            if (oldDataFile.exists()) {
                final Dataset oldDataset = StreamFactory.create(dataFilePath);
                if (!oldDataset.getName().contains("Lite")) {
                    lastModified = oldDataFile.lastModified();
                }
                oldDataset.dispose();
            }
            System.gc();
            // Get data as byte array.
            final byte[] content = download(licenseKeys, lastModified);
            if (content == null) {
                throw new AutoUpdateException("Device data download unsucessful. Update aborted.");
            } else {
                // Create a provider and read the data in.
                final Dataset newDataSet = StreamFactory.create(content);

                boolean copyFile = true;
                final File dataFile = new File(dataFilePath);
                // Confirm the new data is newer than current.
                if (dataFile.exists()) {

                    final Dataset currentDataSet = StreamFactory.create(dataFilePath);
                    copyFile = newDataSet.published.getTime() > currentDataSet.published.getTime() || 
                            !newDataSet.getName().equals(currentDataSet.getName());
                    
                    currentDataSet.dispose();
                }
                System.gc();
                //If the downloaded file is either newer, or has a different name.
                if (copyFile) {
                    // Save the data.
                    FileOutputStream fos = null;
                    for (int i = 0; i < 5; i++) {
                        try {
                            fos = new FileOutputStream(dataFile);
                            break;
                        } catch(Exception ex) {
                            Logger.getLogger(AutoUpdate.class.getName()).log(Level.SEVERE, "Problem opening file output stream to update existing data file. Retrying in 2 seconds.", ex);
                            System.gc();
                            //Wait for 2 seconds to wait for garbage collection 
                            //to attempt to release file lock.
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ex1) {
                                Logger.getLogger(AutoUpdate.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                    }
                    if (fos != null) {
                        fos.write(content);
                    } else {
                        throw new AutoUpdateException("Failed to write to "
                                + "original data file as it was locked");
                    }

                    fos.close();
                    // Sets the last modified time of the file downloaded.
                    dataFile.setLastModified(newDataSet.published.getTime());

                    return newDataSet;
                } else {
                    //No need to update. File names are the same. Dates of both 
                    //files do not indicate an update is required.
                    Logger.getLogger(AutoUpdate.class.getName()).log(Level.INFO,"Data file is already up to date.");
                    return null;
                }
            }
        } catch (IOException ex) {
            throw new AutoUpdateException(String.format(
                    "Exception reading data stream from server '%s'.",
                    DetectionConstants.AUTO_UPDATE_URL) + ex.getMessage());
        }
    }

    /**
     *
     * Calculates the MD5 hash of the given data array.
     *
     * @param value Data to calculate the hash with.
     * @return The MD5 hash of the given data.
     */
    private static String getMd5Hash(final byte[] value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // We can't calculate so return null. This should never happen.
            return null;
        }
        final byte[] data = md5.digest(value);
        final StringBuilder hashBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            hashBuilder.append(String.format("%02X ", data[i]));
        }
        // The hash retrived from the responce header is in lower case with no
        // spaces, so must make sure this hash conforms to the scheme too.
        return hashBuilder.toString().toLowerCase().replaceAll(" ", "");
    }

    /**
     *
     * Verifies that the data has been downloaded correctly by comparing an MD5
     * hash off the downloaded data with one taken before the data was sent,
     * which is stored in a response header.
     *
     * @param client The Premium data download connection.
     * @param data the data that has been downloaded.
     * @return True if the hashes match, else false.
     */
    private static boolean validateMD5(
            final HttpURLConnection client,
            final byte[] data) {
        final String serverHash = client.getHeaderField("Content-MD5");
        final String downloadHash = getMd5Hash(data);
        return serverHash != null && serverHash.equals(downloadHash);
    }

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
     * @return Premium data download url.
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
     */
    public static boolean update(final String licenseKey, String dataFilePath) throws AutoUpdateException {
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
     */
    public static boolean update(final String[] licenseKeys, String dataFilePath) 
            throws AutoUpdateException {

        if (licenseKeys == null || licenseKeys.length == 0) {
            throw new AutoUpdateException(
                    "Device data cannot be updated without a licence key.");
        }

        // If a valid license key exists then proceed
        final String[] validKeys = getValidKeys(licenseKeys);
        if (validKeys.length > 0) {
            // Download the provider getting an instance of a new provider.
            final Dataset dataset = getNewDataset(validKeys, dataFilePath);
            if (dataset != null) {
                dataset.dispose();
                return true;
            }
        } else {
            throw new AutoUpdateException(
                    "The license key(s) provided were invalid.");
        }
        
        return false;
    }

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
    private static byte[] download(final String[] licenseKeys, long lastModified) throws AutoUpdateException {
        HttpURLConnection client = null;
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

                // Read the content into a byte array.
                final byte[] content = new byte[client.getContentLength()];
                int position = 0;
                int bytesRead = client.getInputStream().read(content, position, content.length - position);
                while (bytesRead >= 0) {
                    position += bytesRead;
                    bytesRead = client.getInputStream().read(content, position, content.length - position);
                }

                // now validate with md5 hash
                if (validateMD5(client, content)) {
                    try {
                        return decompressData(content);
                    } catch (DataFormatException ex) {
                        throw new AutoUpdateException("Device data could not be decompressed. It is probably corrupt.");
                    }

                } else {
                    throw new AutoUpdateException("Device data update does not match hash values.");
                }
            } else {
                //Server response was not 200. Data download can not commence.
                StringBuilder message = new StringBuilder();
                message.append("Could not commence data file download. ");
                if(client.getResponseCode() == 429) {
                    message.append("Server response: 429 - too many download attempts. ");
                } else if (client.getResponseCode() == 304) {
                    message.append("Server response: 304 - not modified. You already have the latest data. ");
                } else {
                    message.append("Server response: ");
                    message.append(client.getResponseCode());
                }
                throw new AutoUpdateException(message.toString());
            }
        } catch (IOException ex) {
            throw new AutoUpdateException("Device data download failed: " + ex.getMessage());
        } finally {
            client.disconnect();
        }
    }

    private static byte[] decompressData(final byte[] content) throws IOException, DataFormatException {
        final ByteArrayInputStream bytein = new java.io.ByteArrayInputStream(content);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(content.length);
        final GZIPInputStream gzin = new GZIPInputStream(bytein);
        byte[] buf = new byte[1024];
        while (gzin.available() != 0) {
            int count = gzin.read(buf);
            if (count > 0) {
                bos.write(buf, 0, count);
            }
        }
        bos.close();
        byte[] fullData = bos.toByteArray();
        return fullData;
    }
}
