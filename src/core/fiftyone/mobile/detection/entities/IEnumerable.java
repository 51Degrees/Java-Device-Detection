package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.readers.BinaryReader;
import java.util.Iterator;

/**
 * Class implements the logic of IEnumerable in C#. When you need to read a 
 * specific number of integer objects from the data file it's best to do so 
 * on the as-needed basis instead of loading everything in to memory. This class 
 * provides a way to read the integers one by one.
 */
public class IEnumerable implements Iterator<Integer>, Iterable<Integer> {
    /**
     * Reader set to the position at the start of the list.
     */
    private final BinaryReader reader;
    /**
     * The number of integers to read to form the array.
     */
    int max;
    /**
     * Current position.
     */
    int count;

    /**
     * An enumerable that can be used to read through the entries.
     * @param reader Reader set to the position at the start of the list.
     * @param max 
     */
    public IEnumerable(BinaryReader reader, int max) {
        this.count = 0;
        this.reader = reader;
        this.max = max;
    }

    /**
     * Get the next element.
     * @return integer entry.
     */
    public int getNext() {
        Integer value = null;
        if (count < max) {
            value = reader.readInt32();
            count++;
            return value;
        }
        return value;
    }

    /**
     * Returns true if there are more values to read, false otherwise.
     * @return true if there are more values to read, false otherwise.
     */
    @Override
    public boolean hasNext() {
        return count < max;
    }

    /**
     * Reads the next integer and returns it.
     * @return the next integer in a sequence. Null if current > max.
     */
    @Override
    public Integer next() {
        return getNext();
    }

    /**
     * Not supported.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns this object.
     * @return this object.
     */
    @Override
    public Iterator<Integer> iterator() {
        return this;
    }
    
}
