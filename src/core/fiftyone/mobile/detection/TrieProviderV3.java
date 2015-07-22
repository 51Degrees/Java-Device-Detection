package fiftyone.mobile.detection;

import fiftyone.mobile.detection.entities.stream.TriePool;
import fiftyone.properties.DetectionConstants;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

/**
 * Decision trie data structure provider.
 */
public class TrieProviderV3 extends TrieProvider {

    /**
     * Constructs a new instance of a Trie provider version 3.0.
     * @param copyright The copyright notice for the data file.
     * @param strings Array containing all strings in the output.
     * @param properties Array of properties.
     * @param devices Array of devices.
     * @param lookupList Lookups data array.
     * @param nodesLength The length of the node data.
     * @param nodesOffset The position of the start of the nodes in the 
     * file provided.
     * @param pool >Pool connected to the data source.
     * @throws FileNotFoundException 
     */
    public TrieProviderV3(String copyright, byte[] strings, byte[] properties, 
            byte[] devices, short[] lookupList, long nodesLength, long nodesOffset, 
            TriePool pool) throws FileNotFoundException {
        super(copyright, strings, properties, devices, lookupList, 
                nodesLength, nodesOffset, pool);
        
        String[] headers = DetectionConstants.DEVICE_USER_AGENT_HEADERS;
        int count = _properties.array().length / DetectionConstants.SIZE_OF_INT;
        ByteBuffer bb = ByteBuffer.wrap(_properties.array());
        for (int i = 0; i < count; i++) {
            String value = getStringValue(bb.getInt());
            _propertyIndex.put(value, i);
            _propertyNames.add(value);
            propertyHttpHeaders.add(headers);
        }
    }

}
