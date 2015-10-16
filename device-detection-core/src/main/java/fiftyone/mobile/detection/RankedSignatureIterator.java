package fiftyone.mobile.detection;

/**
 * Used to iterate over the closest signatures.
 */
interface RankedSignatureIterator {

    /**
     * Resets the iterator to be used again.
     */
    void reset();

    /**
     * @return returns true if there are more elements in the next property.
     */
    boolean hasNext();

    /**
     * @return the next integer in the iteration.
     */
    int next();
}
