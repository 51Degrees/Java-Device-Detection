package fiftyone.mobile.detection.trie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Reader used to create a provider from data structured in a decision tree
 * format.
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
     *
     * Creates a new provider from the trie data file supplied.
     *
     * @param dataFile Trie data file used to create the provider.
     * @return A new provider initialised with data from the file provided or
     * null if the file is unreadable.
     */
    public static Provider create(String dataFile) throws FileNotFoundException, IOException {
        File fileInfo = new File(dataFile);
        if (fileInfo.exists()) {
            try {
                // Create a new Random Access file, in readonly mode
                RandomAccessFile data = new RandomAccessFile(dataFile, "r");
                return create(data);
            } catch (IOException ex) {
                _logger.error("Exception reading data file: ", ex);
                throw ex;
            }
        } else {
            _logger.error(String.format("Could not find trie data file \"%s\".", dataFile));
            throw new FileNotFoundException();
        }
    }

    /**
     *
     * Creates a new provider from the Randomly accessed Tri data file.
     *
     * @param data File data used to create Provider.
     * @return A new Provider initialised with data from the file provided.
     * @throws IOException
     */
    private static Provider create(RandomAccessFile data) throws IOException {

        FileChannel reader = data.getChannel();

        // Skip over the version number.
        reader.position(reader.position() + 2);

        // Skip over the copyright notice.
        ByteBuffer temp = ByteBuffer.allocate(4);
        temp.order(ByteOrder.LITTLE_ENDIAN);
        reader.read(temp);
        long copy = readUInt(temp.array());

        reader.position(reader.position() + copy);

        // Create the new provider.
        return new Provider(
                // Read Strings
                read(reader),
                // Read Properties
                read(reader),
                // Read Devices
                read(reader),
                // Read Lookup Lists
                read(reader),
                // File Channel
                reader);
    }

    /**
     *
     * Determines how many bytes to read from a FileChannel into a ByteBuffer
     * then reads them.
     *
     * @param reader Files channel to read from.
     * @return Populated ByteBuffer.
     * @throws IOException
     */
    private static ByteBuffer read(FileChannel reader) throws IOException {
        ByteBuffer temp = ByteBuffer.allocate(4);
        reader.read(temp);
        ByteBuffer data = readFully((int) readUInt(temp.array()), reader);
        if (data == null) {
            System.err.println("data buffer is null");
        }

        return data;
    }

    /**
     *
     * Crates an unsigned integer from an array of 4 byte values and returns the
     * result as a long.
     *
     * @param value values to use.
     * @return value
     */
    private static long readUInt(byte[] value) {
        long l = 0;

        l |= (value[3] & 0xFF);
        l <<= 8;
        l |= (value[2] & 0xFF);
        l <<= 8;
        l |= (value[1] & 0xFF);
        l <<= 8;
        l |= (value[0] & 0xFF);

        if (l > (long) 2 * Integer.MAX_VALUE) {
            _logger.error("readUint: value is greater then unsigned integer.");
        }

        return l;
    }

    /**
     *
     * Reads a set amount of bytes from a FileChannel into a ByteBuffer.
     *
     * @param amount Amount to read.
     * @param reader File channel to read from.
     * @return Bytes Read from the file channel.
     * @throws IOException
     */
    private static ByteBuffer readFully(final int amount, FileChannel reader) throws IOException {
        // Get start position
        long start = reader.position();

        // Final Buffer to return
        ByteBuffer data = ByteBuffer.allocate(amount);
        data.order(ByteOrder.LITTLE_ENDIAN);

        int read = reader.read(data);
        if (read != amount) {
            _logger.error("ByteBuffer not read fully");
            throw new IOException("ByteBuffer not read fully");
        }

        reader.position(start + (long) amount);

        return data;
    }
}
