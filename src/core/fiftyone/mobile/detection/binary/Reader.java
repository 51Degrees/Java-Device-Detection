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
package fiftyone.mobile.detection.binary;

import fiftyone.mobile.detection.Constants.HandlerTypes;
import fiftyone.mobile.detection.*;
import fiftyone.mobile.detection.handlers.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.GZIPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Used to read device data into the detection provider.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Reader {
    
    /**
     * Creates a logger for this class
     */
    private static final Logger _logger = LoggerFactory.getLogger(Reader.class);

    /**
     * Creates a new instance of provider using the embedded device data.
     *
     * @returns a new Provider instance populated with lite data.
     */
    public static Provider create() {
        final Provider provider = new Provider();
        read(provider);
        return provider;
    }
    
    /**
     * Creates a new instance of provider using the embedded device data
     * with a ThreadPoolExecutor to improve detection performance.
     * @param threadPool to improve performance.
     * @return a new Provider instance populated with lite data.
     */
    public static Provider create(final ThreadPoolExecutor threadPool) {
        final Provider provider = new Provider(threadPool);
        read(provider);
        return provider;
    }

    /**
     * Creates a new instance of provider using the data file for source data.
     *
     * @param path The path to the premium data file. 
     * 
     * @returns a new Provider
     * instance populated with lite data.
     */
    public static Provider create(final String path) throws BinaryException{
        return create(path, null);
    }
    
    /**
     * Creates a new instance of provider with a ThreadPoolExecutor to improve
     * performance, using the data file for source data.
     *
     * @param path The path to the premium data file. 
     * @param threadPool to improve performance
     * @returns a new Provider
     * instance populated with lite data.
     */
    public static Provider create(final String path, 
            final ThreadPoolExecutor threadPool) 
            throws BinaryException{
        final Provider provider = new Provider(threadPool);
        if (read(provider, path)) {
            return provider;
        }
        throw new BinaryException("A provider could not be created from data"
                    + "at the specified path. The file may not exist or be in"
                    + "an invalid format.");
    }

    /**
     *
     * Reads the content of the embedded data file into the provider.
     *
     * @param provider a provider object ready to be filled with the embedded
     * data.
     * @return true if the provider was updated, otherwise false.
     */
    public static void read(final Provider provider) {
        try {
            final InputStream input = getEmbeddedDataStream();
            if (input != null) {
                read(provider, input);
                input.close();
            }
        } catch (BinaryException ex) {
            _logger.error(
                    "Exception reading provider data from embedded resource.",
                    ex);
        } catch (IOException ex) {
            _logger.error(
                    "Exception reading provider data from embedded resource.",
                    ex);
        }
    }

    /**
     *
     * Attempts to read in the data file located at the path provided and
     * populates the systems data structures. This is used to upgrade the system
     * to premium data by supplying the path to the premium data file.
     *
     * @param provider a provider object ready to be filled with the embedded
     * data.
     * @param path The path to the premium data file.
     * @return true if the provider was updated, otherwise false.
     */
    public static boolean read(final Provider provider, final String path) {
        try {
            final InputStream input = getDataStream(path);
            if (input != null) {
                read(provider, input);
                input.close();
                return true;
            }
        } catch (BinaryException ex) {
            _logger.warn(
                    String.format(
                    "Exception reading data stream from file '%s'.",
                    path));
        } catch (IOException ex) {
           _logger.warn(
                    String.format(
                    "Exception reading data stream from file '%s'.",
                    path));
        }
        return false;
    }

    /**
     *
     * Attempts to read in data array and populates the systems data structures.
     * This is used to upgrade the system to premium data by supplying the path
     * to the premium data file.
     *
     * @param provider a provider object ready to be filled with the embedded
     * data.
     * @param data data array containing the device data.
     * @return true if the provider was updated, otherwise false.
     */
    public static boolean read(final Provider provider, final byte[] data) {
        try {
            final GZIPInputStream input = new GZIPInputStream(
                    new ByteArrayInputStream(data));
            if (input != null) {
                read(provider, input);
                input.close();
                return true;
            }
        }
         catch (BinaryException ex) {
             _logger.warn(
                    "Exception reading data stream from data array.",
                    ex);
        } catch (IOException ex) {
            _logger.warn(
                    "Exception reading data stream from data array.",
                    ex);
        }
        return false;
    }

    /**
     *
     * Reads in 8 bytes, reverses them and returns the value as a long.
     *
     * @param in Stream of Binary Data.
     * @return A 64 bit int (long).
     * @throws IOException
     */
    private static long readInt64(final InputStream in) throws IOException {
        
        final byte[] buffer = readBytes(in, 8);
        //in.read(buffer, 0, 4);
        final ByteBuffer byteAllocation = ByteBuffer.wrap(buffer);
        return Long.reverseBytes(byteAllocation.getLong());
    }

    /**
     *
     * Reads in 4 bytes, reverses them and returns the value as an int.
     *
     * @param in Stream of Binary Data.
     * @return A 32 bit int.
     * @throws IOException
     */
    private static int readInt32(InputStream in) throws IOException {
        
        final byte[] buffer = readBytes(in, 4);
        
        final ByteBuffer byteAllocation = ByteBuffer.wrap(buffer);
        return Integer.reverseBytes(byteAllocation.getInt());
    }

    /**
     *
     * Reads in 2 bytes, reverses it and returns the value as a short.
     *
     * @param in Stream of Binary Data.
     * @return A 16 bit int (short).
     * @throws IOException
     */
    private static short readInt16(InputStream input) throws IOException {
        
        final byte[] buffer = readBytes(input, 2);
        
        final ByteBuffer byteAllocation = ByteBuffer.wrap(buffer);
        return Short.reverseBytes(byteAllocation.getShort());
    }
    
    private static int readUnsignedByte(final InputStream input) throws IOException {
        return input.read();
    }
    
    /**
     * Reads a boolean value from the input stream
     * 
     * @param in Stream of Binary Data
     * @return A boolean value
     * @throws IOException 
     */
    private static boolean readBoolean(final InputStream input) throws IOException {
        return input.read() != 0;
    }
    
    /**
     * 
     * Fills a buffer of the given length with data from the input stream.
     * 
     * @param in Stream of Binary Data
     * @param length The number of bytes to read
     * @return An array of bytes containing the data just read
     * @throws IOException 
     */
    private static byte[] readBytes(
            final InputStream input, 
            final int length) throws IOException {
        final byte[] buffer = new byte[length];
        
        int offset = 0;
        
        // this loop is needed as there is no guarantee the buffer will be filled
        // in a single pass. It frequently isn't.
        while(offset < length) {
            final int bytesReturned = input.read(buffer, offset, length - offset);
            if(bytesReturned == -1) // -1 indicates the file has ended
            {
                throw new IOException("The end of the data file has been reached.");
            }
            offset += bytesReturned;
        }
                
        return buffer;
    }

    /**
     *
     * Reads the next string value from the binary file.
     *
     * @param input Stream of Binary Data.
     * @return The next String value in the data file.
     * @throws IOException
     */
    private static String readString(InputStream input) throws IOException {
        final byte[] b = new byte[readStringLength(input)];
        
        //int length = in.read(b);
        for(int i = 0; i < b.length; i++)
        {
            b[i] = (byte)input.read();
        }
            
        return new String(b);
    }

    /**
     *
     * Calculates the length of the next string to be read. The file is encoded
     * using the c# String format. The length is encoded as an integer, which is
     * read 8 bits at time. The MSb indicates whether the Integer needs another
     * byte of data, 1 if true, 0 if not. The other 7 bits are part (or all) of
     * the integers value. For more information see <a
     * href="http://msdn.microsoft.com/en-us/library/system.io.binaryreader.readstring.aspx">C#
     * readString</a>
     *
     * @param input Stream of Binary Data.
     * @return The length of the next string to be read.
     * @throws IOException
     */
    private static int readStringLength(final InputStream input) throws IOException {
        int x = 0;
        int b;
        boolean stop = false;
        int shift = 0;
        do {
            b = Reader.readUnsignedByte(input);
            
            x = x | ((b & 127) << (7 * shift));
            shift++;
            if (!((b & 128) == 128)) {
                stop = true;
            }
        } while (stop == false);
        return x;
    }

    /**
     *
     * Reads in date from the binary data represented as 4 bytes.
     *
     * @param input Stream of Binary Data.
     * @return A date object.
     * @throws IOException
     */
    private static long readDate(InputStream input) throws IOException {
        long dt = readInt64(input);
        /*
         * converting from a c# datetime in 100 nanosecond intervals since 00:00
         * of Jan 1st 0000 to java date object of milliseconds since 00:00 Jan 1
         * 1970 the long value represents the amount of ticks (100 nanosecond
         * intervals) from 0000 to 1970
         */
        dt -= 621355968000000000L;
        //converts 100 nanosecond to milliseconds
        dt /= 10000;
        return dt;
    }

    /**
     * Provides a data stream from the embedded resource.
     *
     * @return a data stream from the embedded resource.
     */
    private static GZIPInputStream getEmbeddedDataStream() {
        try {
            return new GZIPInputStream(
                    Reader.class.getResourceAsStream(Constants.EMBEDDED_RESOURCE_PATH));
        } catch (IOException ex) {
            _logger.error(
                    "Exception creating data stream from embedded resource.",
                    ex);
            return null;
        }
    }

    /**
     *
     * Creates a InputStream from the 51Degrees.mobi data file. If the file
     * does not exist or can't be read then null is returned.
     *
     * @param path path to data file, null if embedded data being used.
     * @return InputStream containing data file, or null.
     * @throws BinaryException
     */
    private static GZIPInputStream getDataStream(final String path) {
        try {
            return new GZIPInputStream(
                    new FileInputStream(
                    new File(path)));
        } catch (IOException ex) {
            _logger.error(
                    String.format(
                    "Exception creating data stream from file '%s'.",
                    path));
            return null;
        }
    }

    /**
     *
     * Adds the data from the binary reader to the provider.
     *
     * @param provider The provider to have data added to.
     * @param reader Reader connected to the input stream.
     * @throws IOException
     * @throws BinaryException
     */
    private static void read(
            final Provider provider, 
            final InputStream reader) throws BinaryException, IOException {
        if (reader != null) {
            
            // Read the copyright notice. This is ignored.
            final String copyright = readString(reader);

            // Ignore any additional information at the moment. (should be certificate info)
            final String info = readString(reader);

            // Check the versions are correct.
            final int Major = readInt32(reader), Minor = readInt32(reader);
            if (!(Major == Constants.FORMAT_VRSION_MAJOR
                    && Minor == Constants.FORMAT_VERSION_MINOR)) {
                throw new BinaryException(
                        "The data provided is not supported by this version of fiftyonedegrees. "
                        + "The version provided is '"
                        + Major + "." + Minor + "' and the version expected is '"
                        + Constants.FORMAT_VRSION_MAJOR + "." + Constants.FORMAT_VERSION_MINOR + "'.");
            }

            // Load the data now that validation is completed.
            readStrings(reader, provider.getStrings());
            readHandlers(reader, provider);
            readDevices(reader, provider, null);
            readPublishedDate(reader, provider);
            readManifest(reader, provider);
            readDataSetName(reader, provider);
            readComponents(reader, provider);
        }
    }

    /**
     *
     * Reads the devices and any children.
     *
     * @param reader Binary Data stream being used.
     * @param provider The provider the device will be added to.
     * @param parent The parent of the device, or null if a root device.
     * @throws IOException
     */
    private static void readDevices(
            final InputStream reader, 
            final Provider provider, 
            final DeviceInfo parent) throws IOException {
        final short count = readInt16(reader);
        
        for (short i = 0; i < count; i++) {
            // Get the device id String.
            final int uniqueDeviceIDStringIndex = readInt32(reader);
            final String uniqueDeviceID =
                    uniqueDeviceIDStringIndex >= 0
                    ? provider.getStrings().get(uniqueDeviceIDStringIndex)
                    : "";

            DeviceInfo device;

            // Get the number of useragents available for the device.
            short userAgentCount = readInt16(reader);

            if (userAgentCount > 0) {
                // Read the 1st one, if one is present to assign to the master device.
                device = new DeviceInfo(
                        provider,
                        uniqueDeviceID,
                        readInt32(reader),
                        parent);

                // Add the device to the handlers.
                for (short index : readDeviceHandlers(reader)) {
                    provider.getHandlers().get(index).set(device);
                }

                // Reduce the number of useragents by 1 because we've read
                // the 1st one.
                userAgentCount--;
            } else {
                // Create the device and don't assign any useragents.
                device = new DeviceInfo(
                        provider,
                        uniqueDeviceID,
                        parent);
            }

            // Create new devices as children of this one to hold the
            // remaining user agent strings.
            for (int u = 0; u < userAgentCount; u++) {
                // Get the user agent String index and create a new
                // device.
                final DeviceInfo uaDevice = new DeviceInfo(
                        provider,
                        uniqueDeviceID,
                        readInt32(reader),
                        device);

                // Add the device to the handlers.
                for (short index : readDeviceHandlers(reader)) {
                    provider.getHandlers().get(index).set(uaDevice);
                }
            }

            // Add the device to the list of all devices.
            final int hashCode = device.getDeviceId().hashCode();
            if (provider.getAllDevices().containsKey(hashCode)) {
                // Yes. Add this device to the list.
                provider.getAllDevices().get(hashCode).add(device);
            } else {
                // No. So add the new device.
                List<BaseDeviceInfo> temp = new ArrayList<BaseDeviceInfo>();
                temp.add(device);
                provider.getAllDevices().put(hashCode, temp);
            }

            // Get the remaining properties and values to the device.
            readCollection(reader, device.getStringIndexedProperties());

            // Read the child devices.
            readDevices(reader, provider, device);
        }
    }

    /**
     *
     * Reads all the handler indexes that support this device.
     *
     * @param reader The Binary Data stream being used
     * @return The indexes that have been read
     * @throws IOException
     */
    private static List<Short> readDeviceHandlers(final InputStream reader) throws IOException {
        final List<Short> indexes = new ArrayList<Short>();
        final short count = readInt16(reader);
        for (int i = 0; i < count; i++) {
            indexes.add(readInt16(reader));
        }
        return indexes;
    }

    /**
     *
     * Reads the handler from the binary reader.
     *
     * @param reader The Binary Data stream being used.
     * @param provider The provider being populated.
     * @throws IOException
     * @throws BinaryException
     */
    private static void readHandlers(
            final InputStream reader, 
            final Provider provider) throws IOException, BinaryException {
        int count = readInt32(reader);
        for (int i = 0; i < count; i++) {
            Handler handler = createHandler(reader, provider);
            readHandlerRegexes(reader, handler.getCanHandleRegex());
            readHandlerRegexes(reader, handler.getCantHandleRegex());
            provider.getHandlers().add(handler);
        }
    }

    /**
     * Reads details about the components and properties updating the properties
     * loaded earlier in the file.
     *
     * @param reader BinaryReader of the input stream.
     * @param provider The provider the device will be added to.
     */
    private static void readComponents(final InputStream reader, final Provider provider) {
        try {
            final int count = readInt32(reader);
            for (int i = 0; i < count; i++) {
                // Get the names of the property and component.
                final int propertyIndex = readInt32(reader);
                final int componentIndex = readInt32(reader);

                // If the property and component names are valid then 
                // find the property object and set the component name.
                if (propertyIndex >= 0
                        && componentIndex >= 0) {
                    // Find the propety and update it's component.
                    final Property property = provider.getProperties().get(propertyIndex);
                    if (property != null) {
                        Components component = Components.Unknown;
                        final String componentName = provider.getStrings().get(componentIndex);
                        if ("HardwarePlatform".equals(componentName)) {
                            component = Components.Hardware;
                        } else if ("SoftwarePlatform".equals(componentName)) {
                            component = Components.Software;
                        } else if ("BrowserUA".equals(componentName)) {
                            component = Components.Browser;
                        } else if ("Crawler".equals(componentName)) {
                            component = Components.Crawler;
                        }
                        property.setComponent(component);
                    }
                }
            }
        } catch (IOException ex) {
            // Nothing we can do as data is not included.
            _logger.warn(
                    "EndOfStreamException reading components. Component details will not be available.",
                    ex);
        }
    }

    /**
     *
     * Reads the regular expressions used to determine if the user agent can be
     * handled by the handler.
     *
     * @param reader The Binary Data stream being used.
     * @param list A list of regular expressions to be populated.
     * @throws IOException
     */
    private static void readHandlerRegexes(
            final InputStream reader, 
            final List<HandleRegex> list) throws IOException {
        final int count = readInt16(reader);
        for (int i = 0; i < count; i++) {
            final String pattern = readString(reader);
            final HandleRegex regex = new HandleRegex(pattern);
            readHandlerRegexes(reader, regex.getChildren());
            list.add(regex);
        }
    }

    /**
     *
     * Reads a collection of properties.
     *
     * @param reader The Binary Data stream being used.
     * @param properties A custom Collection object of properties to be
     * populated.
     * @throws IOException
     */
    private static void readCollection(
            final InputStream reader, 
            final Collection properties) throws IOException {
        // Read the number of properties.
        final short propertiesCount = readInt16(reader);
        for (int p = 0; p < propertiesCount; p++) {
            // Get the index of the properties String.
            final int propertyNameStringIndex = readInt32(reader);

            // Read the number of values.
            final int valuesCount = Reader.readUnsignedByte(reader);

            // Read all the values.
            final List<Integer> values = new ArrayList<Integer>();
            for (int v = 0; v < valuesCount; v++) {
                values.add(readInt32(reader));
            }

            // Add the property and values.
            properties.put(propertyNameStringIndex, values);
        }
    }

    /**
     *
     * Creates the handler for the current provider.
     *
     * @param reader The Binary Data stream to be used.
     * @param provider Provider the new handler should be assigned to.
     * @return An instance of the new handler.
     * @throws IOException
     * @throws BinaryException
     */
    private static Handler createHandler(
            final InputStream reader, 
            final Provider provider) throws IOException, BinaryException {
        final HandlerTypes type = HandlerTypes.type(Reader.readUnsignedByte(reader));
        final int confidence = Reader.readUnsignedByte(reader);
        final String name = readString(reader);
        final boolean checkUAProfs = Reader.readBoolean(reader);

        switch (type) {
            case EDITDISTANCE:
                return new EditDistanceHandler(
                        provider, name, confidence, checkUAProfs);
            case REGEXSEGMENT:
                RegexSegmentHandler handler = new RegexSegmentHandler(
                        provider, name, confidence, checkUAProfs);
                readRegexSegmentHandler(reader, handler);
                return handler;
            case REDUCEDINITIALSTRING:
                return new ReducedInitialStringHandler(
                        provider, name, confidence, checkUAProfs, readString(reader));
        }
        throw new BinaryException("Type '" + type + "' is not a recognised handler.");
    }

    /**
     *
     * Reads the regular expressions and weights that form the handler.
     *
     * @param reader The Binary Data stream being used.
     * @param handler Handler being created.
     * @throws IOException
     */
    private static void readRegexSegmentHandler(
            final InputStream reader, 
            final RegexSegmentHandler handler) throws IOException {
        final int count = readInt16(reader);
        for (int i = 0; i < count; i++) {
            handler.getSegments().add(new RegexSegmentHandler.RegexSegment(readString(reader), readInt32(reader)));
        }
    }

    /**
     *
     * Reads the initial number of strings into the strings collection.
     *
     * @param reader The Binary Data stream being used.
     * @param strings Strings instance to be added to.
     * @throws IOException
     */
    private static void readStrings(
            final InputStream reader, 
            final Strings strings) throws IOException {
        final int count = readInt32(reader);
        for (int i = 0; i < count; i++) {
            final short length = readInt16(reader);
            final byte[] rawString = Reader.readBytes(reader, length);
            
            strings.add(new String(rawString));
        }
    }

    /**
     *
     * Reads the name of the data set.
     *
     * @param reader Data source being processed.
     * @param provider Provider the new handler should be assigned to.
     */
    private static void readDataSetName(
            final InputStream reader, 
            final Provider provider) {
        try {
            provider.setDataSetName(checkString(reader));
        } catch (IOException ex) {
            // Nothing we can do as data is not included.
            _logger.warn(
                    "EndOfStreamException reading data set name. DataSetName will be unknown.",
                    ex);
            provider.setDataSetName("Unknown");
        }
    }

    /**
     *
     * Reads the date and time the file was published.
     *
     * @param reader The Binary Data stream to be used.
     * @param provider Provider the new handler should be assigned to.
     */
    private static void readPublishedDate(
            final InputStream reader, 
            final Provider provider) {
        try {
            provider.setPublishedDate(new Date(readDate(reader)));
        } catch (IOException ex) {
            // Nothing we can do as date is not included.
            _logger.error(
                    "IOException reading published date: ",
                    ex);
            provider.setPublishedDate(new Date());
        }
    }

    /**
     *
     * Adds manifest information to the provider if the data file includes it.
     *
     * @param reader The Binary Data stream to be used
     * @param provider Provider the new handler should be assigned to.
     */
    private static void readManifest(
            final InputStream reader, 
            final Provider provider) {
        // Ensure any old properties are removed.
        provider.getProperties().clear();

        try {
            final int countOfProperties = readInt32(reader);
            for (int p = 0; p < countOfProperties; p++) {
                // Create the property.
                final Property property = new Property(
                        provider,
                        provider.getStrings().get(readInt32(reader)),
                        checkString(reader),
                        checkString(reader),
                        Reader.readBoolean(reader),
                        Reader.readBoolean(reader),
                        Reader.readBoolean(reader));

                // Add the values to the list.
                final int countOfValues = readInt32(reader);
                for (int v = 0; v < countOfValues; v++) {
                    final int x = readInt32(reader);
                    final Value value = new Value(
                            property,
                            provider.getStrings().get(x),
                            checkString(reader),
                            checkString(reader));
                    property.getValues().add(value);
                }

                // Finally add the property to the list of properties.
                provider.getProperties().put(
                        property.getNameStringIndex(),
                        property);
            }
        } catch (IOException ex) {
            // Nothing we can do. Clear the data.
             _logger.error(
                    "Manifest Exception.",
                    ex);
        }
    }

    /**
     *
     * Checks for a boolean value to indicate if the String is present. If it is
     * present then read the String and return it.
     *
     * @param reader The Binary Data stream to be used.
     * @return true if there, false else.
     * @throws IOException
     */
    private static String checkString(final InputStream reader) throws IOException {
        final boolean isPresent = Reader.readBoolean(reader);
        if (isPresent) {
            return readString(reader);
        }
        return null;
    }
}
