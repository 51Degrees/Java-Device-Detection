package fiftyone.mobile.detection.cache;

import fiftyone.mobile.detection.MatchState;

/**
 *
 */
@SuppressWarnings("unused")
public class NoOpCache <K> implements Cache <K,MatchState> {
    MatchState matchState;

    public NoOpCache() {
        this.matchState = new MatchState();
    }

    @Override
    public void resetCache() {

    }

    @Override
    public void clearRequests() {

    }

    @Override
    public void clearMisses() {

    }

    @Override
    public void setLoader(Loader<K, MatchState> loader) {

    }

    @Override
    public long getCacheRequests() {
        return 0;
    }

    @Override
    public long getCacheMisses() {
        return 0;
    }

    @Override
    public boolean enableStats(boolean enable) {
        return false;
    }

    @Override
    public MatchState get(K key) {
        return matchState;
    }

    @Override
    public MatchState get(K key, MatchState resultInstance) {
        return resultInstance;
    }

    @Override
    public MatchState getIfCached(K key) {
        return matchState;
    }

    @Override
    public MatchState get(K key, Loader<K, MatchState> loader) {
        return matchState;
    }
}
