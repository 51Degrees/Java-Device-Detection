package fiftyone.mobile.detection.cache;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
class CacheStats {
    /**
     * The number of requests made to the cache.
     */
    private final AtomicLong requests = new AtomicLong(0);
    /**
     * The number of times an item was not available.
     */
    private final AtomicLong misses = new AtomicLong(0);

    private boolean enabled;

    public CacheStats() {
    }

    public void incRequests() {
        if (enabled) requests.incrementAndGet();
    }

    public void incMisses() {
        if (enabled) misses.incrementAndGet();
    }

    public long getRequests() {
        return enabled? requests.longValue() : -1;
    }

    public long getMisses() {
        return enabled ? misses.longValue() : -1;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void clearMisses() {
        this.misses.set(0);
    }

    public void clearRequests() {
        this.requests.set(0);
    }
}
