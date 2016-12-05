package fiftyone.device.proto;

/**
 * @author jo
 */
interface CacheLoader <K, V> {
    V load(K key);
}
