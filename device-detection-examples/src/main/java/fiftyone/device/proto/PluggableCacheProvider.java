package fiftyone.device.proto;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Provider;

/**
 * Subclass of provider that accepts a device cache.
 *
 * Equally one could extend dataset to contain that concept.
 *
 * Also note - the cache statistics interfaces on Provider need removing
 * @author jo
 */
public class PluggableCacheProvider extends Provider {
    public PluggableCacheProvider(Dataset dataset, fiftyone.device.proto.Cache cache){
        super(dataset);
    }
    public PluggableCacheProvider(Dataset dataset, javax.cache.Cache cache){
        super(dataset);
    }
    public PluggableCacheProvider(Dataset dataset, com.google.common.cache.Cache cache){
        super(dataset);
    }
}
