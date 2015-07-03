package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.entities.IEnumerable;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Creates new instances of IEnumerable.
 */
public class IEnumerableFactory {
    
    /**
     * Get a new instance of IEnumerable.
     * @param reader Reader set to the position at the start of the list.
     * @param max The number of integers to read to form the array.
     * @return 
     */
    public static IEnumerable create(BinaryReader reader, int max) {
        return new IEnumerable(reader, max);
    }
    
}
