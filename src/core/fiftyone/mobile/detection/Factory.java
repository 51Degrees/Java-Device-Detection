/* *********************************************************************
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

import fiftyone.mobile.detection.binary.Reader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ThreadPoolExecutor;
import javax.net.ssl.HttpsURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class used to create a Provider if you have a premium license agreement. The
 * class will automatically update the Provider that is being used.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Factory {

    /**
     * The current active provider.
     */
    private Provider _activeProvider;
    /**
     * Path to the premium data file if provided.
     */
    private File _binaryFilePath = null;
    /**
     * Timer used to obtain new premium data over the web if a licence key
     * is provided during initialisation.
     */
    private Timer _autoUpdateTimer = null;
    /**
     * Timer used to checked for new versions of the data file if a binary file
     * path was provided during initialisation.
     */
    private Timer _fileCheckerTimer = null;
    /**
     * Creates a logger for this class
     */
    private static final Logger _logger = LoggerFactory.getLogger(Factory.class);
    /**
     * Holds reference to a threadpool passed by the developer. Thread pools are
     * no longer created automatically
     */
    private ThreadPoolExecutor _threadPool = null;
    /**
     * A string holding the licence key passed in the initialize method
     */
    private String _licenceKey = null;

    /**
     * Initialises the Factory Object using default values. No licence key will
     * be available and the auto update process will not operate. Embedded
     * device data will be used.
     */
    public void initialize() {
        // initialize will only ever throw an exception if path
        // is not null.
        try
        {
            initialize(null, null, null);
        }
        catch(Exception ex){
            _logger.warn(
                "Initialize exception",
                ex);
        }
    }
    
    /**
     * Initialises the Factory Object using default values. No licence key will
     * be available and the auto update process will not operate. Embedded
     * device data will be used.
     * @param threadPool 
     */
    public void initialize(final ThreadPoolExecutor threadPool){
        // initialize will only ever throw an exception if path
        // is not null.
        try
        {
            initialize(null, null, threadPool);
        }
        catch(IOException ex){
            _logger.warn(
                "Initialize exception",
                ex);
        }
    }

    /**
     * Initialises the Factory Object. If no data file is found at the path
     * location supplied then embedded Lite data will be used.
     *
     * @param path File name and path of the Premium data file.
     */
    public void initialize(final String path) throws IOException {
        initialize(path, null, null);
    }
    
    /**
     * Initialises the Factory Object. If no data file is found at the path
     * location supplied then embedded Lite data will be used.
     *
     * @param path File name and path of the Premium data file.
     * @param threadPool for improved performance
     * @throws Exception 
     */
    public void initialize(final String path, final ThreadPoolExecutor threadPool)
            throws IOException {
        initialize(path, null, threadPool);
    }
    
    /**
     * Initialises the Factory Object. If no data file is found at the path
     * location supplied then embedded Lite data will initially be used. A
     * licence key can be used to automatically update the data file when new
     * versions become available. If the file cannot be found and the licenceKey
     * is null an exception will be thrown
     *
     * @param path File name and path of the Premium data file.
     * @param licenceKey The License key.
     */
    public void initialize(final String path, final String licenceKey) 
            throws IOException {
        initialize(path, licenceKey, null);
    }

    /**
     * Initialises the Factory Object. If no data file is found at the path
     * location supplied then embedded Lite data will initially be used. A
     * licence key can be used to automatically update the data file when new
     * versions become available. If the file cannot be found and the licenceKey
     * is null an exception will be thrown
     *
     * @param path File name and path of the Premium data file.
     * @param licenceKey The License key.
     * @param threadPool for improved performance
     */
    public synchronized void initialize(
            final String path, 
            final String licenceKey, 
            final ThreadPoolExecutor threadPool)
            throws IOException{
        
        _threadPool = threadPool;
        
        _binaryFilePath = null;
         
        if(licenceKey != null) {
            LicenceKey.setKey(licenceKey);
        }

        // If the path provided is valid then use that one.
        if (path != null
                && path.isEmpty() == false) {
            _binaryFilePath = new File(path);
        }

        /*
         * Try creating the active provider with the file path that we now have.
         * If the path is null or the provider can't be created use the embedded
         * data.
         */
        final Provider provider = createProvider();
        if(_binaryFilePath != null)
        {
            if(Reader.read(provider,_binaryFilePath.getAbsolutePath()) == false)
            {
                if(licenceKey == null)
                {
                    throw new IOException("A provider could not be created from data "
                    + "at the specified path and there is no licence to create a data "
                    + "file at that location in the future. The file may not exist or "
                    + "be in an invalid format.");
                }
                _logger.info(String.format("No data at '%s'. Lite data will be used "
                        + "until premium data can be downloaded with the licence "
                        + "key.", path));
                Reader.read(provider);
            }
            // data path used, therefore external data should be
            // checked regularly
            _fileCheckerTimer = new Timer(
                    "51Degrees.mobi File Checker Timer",
                     true);
            _fileCheckerTimer.schedule(
                    new FileAutoChecker(this),
                    Constants.FILE_CHECK_DELAYED_START,
                    Constants.FILE_CHECK_SLEEP);
        }
        else // no file path supplied, use embedded
        {
            Reader.read(provider);
        }
        
        if (_binaryFilePath != null && 
                LicenceKey.getKey() != null && 
                Constants.AUTO_UPDATE_DELAYED_START > 0) 
        {
            /*
             * The data file was valid and we have a valid licence key so start 
             * the auto update timer to check for new data files in the future.
             */
            _autoUpdateTimer = new Timer(
                "51Degrees.mobi Auto Update Timer",
                true);
            _autoUpdateTimer.schedule(
                    new AutoUpdater(this),
                    Constants.AUTO_UPDATE_DELAYED_START,
                    Constants.AUT0_UPDATE_SLEEP);
        }
        // Finally set the active provider for the first time.
        setActiveProvider(provider);
    }
    
    /**
     * Gets the ThreadPoolExecutor associated with the Factory
     * @return a ThreadPoolExecutor, or null if the factory does not have a 
     * threadpool
     */
    public ThreadPoolExecutor getThreadPool()
    {
        return _threadPool;
    }

    /**
     * Switch the active provider over to the one provided. Leave it for the 
     * garbage collector to destroy the old provider when it's no longer 
     * referenced.
     *
     * @param provider new provider with more current data.
     */
    protected void setActiveProvider(Provider provider) {
        _activeProvider = provider;
    }

    /**
     * Returns the active provider with the most current data available.
     * Requires Premium device data to return anything other than embedded data.
     * 
     * The instance returned should not be stored when updates are enabled to 
     * ensure the most recent data is always being used.
     * 
     * @return The latest 51Degrees.mobi provider.
     */
    public Provider getProvider() {
        return _activeProvider;
    }

    /**
     * Providers the location of the Data file.
     *
     * @return The Premium data file.
     */
    protected File getBinaryFilePath() {
        return _binaryFilePath;
    }

    /**
     * Creates a new provider of the type generated by the factory.
     *
     * @return a new provider ready to be loaded with data.
     */
    protected Provider createProvider() {
        return new Provider(_threadPool);
    }
    
    /**
     * Uses the licence key given to the factory in initialize method to perform
     * a device data update, writing the data to the file system and filling 
     * providers from this factory instance with it.
     * 
     * @return true for a successful update. False can indicate that data was
     * unavailable, no licence key was supplied, corrupt, older than the current
     * data or not enough memory was available. In that case the current data is
     * used.
     */
    public boolean update() {
        
        return update(_licenceKey);
    }
    
    /**
     * Uses the given license key to perform a device data update, writing the data to
     * the file system and filling providers from this factory instance with it.
     * 
     * @param licenceKey the licence key to submit to the server
     * @return true for a successful update. False can indicate that data was
     * unavailable, corrupt, older than the current data or not enough memory
     * was available. In that case the current data is used.
     */
    public boolean update(final String licenceKey) {

        // get roughly how much memory is available
        final long usedMemory = Runtime.getRuntime().totalMemory() - 
                Runtime.getRuntime().freeMemory();
        final long availableMemory = Runtime.getRuntime().maxMemory() - 
                usedMemory;
        
        // only attempt an update if there is more than 100mb available
        if (availableMemory < Constants.AUTO_UPDATE_REQUIRED_FREE_MEMORY) {
            _logger.warn("JVM does not have enough free memory to update. "
                    + "Consider increasing the memory limit for your JVM.");
        } else {
            try {
                // If a valid license key exists then proceed
                LicenceKey.setKey(licenceKey);
                if (LicenceKey.getKey() == null) {
                    _logger.warn("Device data cannot be updated without a licence key.");
                }
                else {

                    // Download the provider getting an instance of a new provider.
                    final Provider provider = getNewProvider();

                    if (provider != null) {
                        setActiveProvider(provider);
                        return true;
                    }

                }
            }
            catch (OutOfMemoryError ex) {
                _logger.warn("Auto update could not complete as the process ran out "
                        + "of memory. Try allocation the JVM with more memory.", ex);
            }
        }
        return false;
    }
    
    /**
     * Downloads and validates data, returning a byte array or null if download
     * or validation was unsuccessful.
     */
    private byte[] download() {
        HttpsURLConnection client = null;
        try {
            // Open the connection to download the latest data file.
            client = (HttpsURLConnection) fullUrl().openConnection();
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
                    return content;
                }                
                else {
                    _logger.warn("Device data update does not match hash values.");
                }
            }
            else
            {
                _logger.warn("Unable to connect with 51Degrees update server. "
                        + "The update server may be temporarily down or unreachable "
                        + "from this location.");
            }
        } catch (IOException ex) {
            _logger.warn("Device data download failed.", ex);
        }
        finally {
            client.disconnect();
        }
        return null;
    }

    /**
     * Downloads the latest Premium data and saves to disk if the data has been
     * downloaded correctly.
     *
     * @returns a provider which has passed verification, or null if there was
     * no new data or the data provided failed validation.
     */
    private Provider getNewProvider() {
        try {
            _logger.info("Starting data update.");

            // get new data as byte array
            final byte[] content = download();
            if (content == null) {
                _logger.warn("Device data download unsucessful. Update aborted.");
            } else {
                _logger.debug(String.format("Data update resonse received, %d bytes loaded.", content.length));

                // Create a provider and read the data in.
                final Provider provider = createProvider();
                _logger.debug("Provider created");
                // Check this is new data based on publish data and number of
                // available properties.
                if (Reader.read(provider, content)) { // Provider successfully created.

                    if (provider.getPublishedDate().getTime() > getProvider().getPublishedDate().getTime()) {
                        _logger.debug("Data to be saved and loaded.");
                        // Save the data and force the factory to reload.
                        final File dataFile = getBinaryFilePath();
                        final FileOutputStream fos = new FileOutputStream(dataFile);
                        fos.write(content);

                        fos.close();
                        _logger.debug("Device data file written.");
                        // Sets the last modified time of the file downloaded.
                        dataFile.setLastModified(provider.getPublishedDate().getTime());

                        final String date = provider.getPublishedDate().toString();
                        final String msg = String.format("New device data published on %s successfully installed ", date);
                        _logger.info(msg);
                        
                        return provider;
                    } else {
                        _logger.info("Device data not loaded. It is not newer than the current data.");
                    }

                } else {
                    _logger.warn("Device data could not be loaded. Data is likely or "
                            + "corrupt or in a newer format. If this error occurs often "
                            + "consider updating the detector.");
                }
            }
        } catch (IOException ex) {
            _logger.warn(String.format(
                    "Exception reading data stream from server '%s'.",
                    Constants.AUTO_UPDATE_URL),
                    ex);
        }
        return null;
    }

    /**
     * Constructs the URL needed to download Premium data.
     *
     * @return Premium data download url.
     * @throws MalformedURLException
     */
    private static URL fullUrl() throws MalformedURLException {
        final List<String> parameters = Arrays.asList(
                "LicenseKeys=" + LicenceKey.getKey(),
                "Download=True",
                "Type=Binary");
        String url = Constants.AUTO_UPDATE_URL + "?";
        for (int i = 0; i < parameters.size(); i++) {
            url += parameters.get(i);
            if (i < (parameters.size() - 1)) {
                url += "&";
            }
        }
        return new URL(url);
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
            final HttpsURLConnection client, 
            final byte[] data) {
        final String serverHash = client.getHeaderField("Content-MD5");
        return serverHash != null
                && serverHash.equals(getMD5Hash(data));
    }

    /**
     *
     * Calculates the MD5 hash of the given data array.
     *
     * @param value Data to calculate the hash with.
     * @return The MD5 hash of the given data.
     */
    private static String getMD5Hash(final byte[] value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // We can't calculate so return null.
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
     * Stops the auto update timers and destroys the active provider. 
     * Assuming no other providers are present then the 
     */
    public void destroy() throws Throwable {
        _logger.trace("Destroying 51Degrees.mobi Factory");
        // Stop the auto update timers as they're no longer needed.
        if (_autoUpdateTimer != null) {
            _autoUpdateTimer.cancel();
            _autoUpdateTimer.purge();
        }
        if (_fileCheckerTimer != null) {
            _fileCheckerTimer.cancel();
            _fileCheckerTimer.purge();
        }
        
        _logger.trace("Destroyed 51Degrees.mobi Factory");
    }
}