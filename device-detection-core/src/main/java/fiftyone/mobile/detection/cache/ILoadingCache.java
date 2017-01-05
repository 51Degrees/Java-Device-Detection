package fiftyone.mobile.detection.cache;

import java.io.IOException;

/**
 * Extension of general cache contract to provide for getting a value with a particular
 * value loaded. Primarily used to allow the value loader to be an already instantiated value of the
 * type V to avoid construction costs of that value. (In other words the loader has the signature
 * extends V implements IValueLoader.
 * <p>
 * Used only in UA Matching.
 * @author jo
 */
public interface ILoadingCache<K,V> extends ICache <K,V> {
    V get(K key, IValueLoader<K, V> loader) throws IOException;
}
