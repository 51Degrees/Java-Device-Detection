package fiftyone.mobile.detection.cache;

/**
 * Contains everything needed to build a cache.
 * Currently, an {@link ICacheBuilder} and an integer size parameter.
 */
public class CacheOptions implements ICacheOptions {
    private int size;
    private ICacheBuilder builder;

    public CacheOptions(int size, ICacheBuilder builder){
        this.size = size;
        this.builder = builder;
    }

    public int getSize() {
        return size;
    }

    public ICacheBuilder getBuilder(){
        return builder;
    }

    public void setBuilder(ICacheBuilder builder){
        this.builder = builder;
    }
}
