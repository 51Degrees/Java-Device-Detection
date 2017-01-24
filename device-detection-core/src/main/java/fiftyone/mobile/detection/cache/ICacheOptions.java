package fiftyone.mobile.detection.cache;

/**
 * Represents an object that contains everything needed to build a cache.
 * Currently, an {@link ICacheBuilder} and an integer size parameter.
 */
public interface ICacheOptions {
    int getSize();

    ICacheBuilder getBuilder();
    void setBuilder(ICacheBuilder builder);
}
