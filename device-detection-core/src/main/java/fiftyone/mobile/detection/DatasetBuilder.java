/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2015 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 *
 * This Source Code Form is the subject of the following patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY:
 * European Patent Application No. 13192291.6; and
 * United States Patent Application Nos. 14/085,223 and 14/085,301.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 *
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.detection;

import fiftyone.mobile.detection.cache.*;
import fiftyone.mobile.detection.entities.*;
import fiftyone.mobile.detection.entities.Map;
import fiftyone.mobile.detection.entities.headers.Header;
import fiftyone.mobile.detection.entities.memory.MemoryFixedList;
import fiftyone.mobile.detection.entities.memory.PropertiesList;
import fiftyone.mobile.detection.entities.stream.IntegerList;
import fiftyone.mobile.detection.factories.*;
import fiftyone.mobile.detection.factories.stream.NodeStreamFactoryV31;
import fiftyone.mobile.detection.factories.stream.NodeStreamFactoryV32;
import fiftyone.mobile.detection.factories.stream.ProfileStreamFactory;
import fiftyone.mobile.detection.readers.BinaryReader;


import java.io.File;
import java.io.IOException;
import java.util.*;

import static fiftyone.mobile.detection.DatasetBuilder.CacheType.*;

/**
 * Allows construction of a {@link Dataset}.
 * <p>
 * Use as follows:<br>
 *<pre><code>
 *      // for stream dataset read from byte array buffer
 *      Dataset dataset = DatasetBuilder.buffer()
 *          // to use caching
 *          .addDefaultCaches()
 *          .build(array);
 *
 *      // for stream dataset read from file
 *      Dataset dataset = DatasetBuilder.file()
 *          // to use caching (recommended)
 *          .addDefaultCaches()
 *          // if a temporary file (deleted on dataset close)
 *          .setTempFile()
 *          // to set the date explicitly
 *          .lastModified(date)
 *          .build(filename);
 *          
 * </code></pre>
 */

public class DatasetBuilder {

    /* Default Cache sizes */
    public static final int STRINGS_CACHE_SIZE = 5000;
    public static final int NODES_CACHE_SIZE = 15000;
    public static final int VALUES_CACHE_SIZE = 5000;
    public static final int PROFILES_CACHE_SIZE = 600;
    public static final int SIGNATURES_CACHE_SIZE = 500;

    private static ICacheBuilder lruBuilder = LruCache.builder();

    // TODO Fill me in!
    private static EnumMap<CacheType, ICacheOptions> defaultCacheSizes =
            new EnumMap<CacheType, ICacheOptions>(CacheType.class);
    static {
        defaultCacheSizes.put(StringsCache, new CacheOptions(STRINGS_CACHE_SIZE, lruBuilder));
        defaultCacheSizes.put(NodesCache, new CacheOptions(NODES_CACHE_SIZE, lruBuilder));
        defaultCacheSizes.put(ValuesCache, new CacheOptions(VALUES_CACHE_SIZE, lruBuilder));
        defaultCacheSizes.put(ProfilesCache, new CacheOptions(PROFILES_CACHE_SIZE, lruBuilder));
        defaultCacheSizes.put(SignaturesCache, new CacheOptions(SIGNATURES_CACHE_SIZE, lruBuilder));
    }

    // TODO Fill me in!
    private static EnumMap<CacheType, ICacheOptions> MtCacheSizes =
            new EnumMap<CacheType, ICacheOptions>(CacheType.class);
    static {
        MtCacheSizes.put(StringsCache, new CacheOptions(STRINGS_CACHE_SIZE, lruBuilder));
        MtCacheSizes.put(NodesCache, new CacheOptions(NODES_CACHE_SIZE, lruBuilder));
        MtCacheSizes.put(ValuesCache, new CacheOptions(VALUES_CACHE_SIZE, lruBuilder));
        MtCacheSizes.put(ProfilesCache, new CacheOptions(PROFILES_CACHE_SIZE, lruBuilder));
        MtCacheSizes.put(SignaturesCache, new CacheOptions(SIGNATURES_CACHE_SIZE, lruBuilder));
    }

    // TODO Fill me in!
    private static EnumMap<CacheType, ICacheOptions> StCacheSizes =
            new EnumMap<CacheType, ICacheOptions>(CacheType.class);
    static {
        StCacheSizes.put(StringsCache, new CacheOptions(STRINGS_CACHE_SIZE, lruBuilder));
        StCacheSizes.put(NodesCache, new CacheOptions(NODES_CACHE_SIZE, lruBuilder));
        StCacheSizes.put(ValuesCache, new CacheOptions(VALUES_CACHE_SIZE, lruBuilder));
        StCacheSizes.put(ProfilesCache, new CacheOptions(PROFILES_CACHE_SIZE, lruBuilder));
        StCacheSizes.put(SignaturesCache, new CacheOptions(SIGNATURES_CACHE_SIZE, lruBuilder));
    }

    // TODO Fill me in!
    private static EnumMap<CacheType, ICacheOptions> StlmCacheSizes =
            new EnumMap<CacheType, ICacheOptions>(CacheType.class);
    static {
        StlmCacheSizes.put(StringsCache, new CacheOptions(STRINGS_CACHE_SIZE, lruBuilder));
        StlmCacheSizes.put(NodesCache, new CacheOptions(NODES_CACHE_SIZE, lruBuilder));
        StlmCacheSizes.put(ValuesCache, new CacheOptions(VALUES_CACHE_SIZE, lruBuilder));
        StlmCacheSizes.put(ProfilesCache, new CacheOptions(PROFILES_CACHE_SIZE, lruBuilder));
        StlmCacheSizes.put(SignaturesCache, new CacheOptions(SIGNATURES_CACHE_SIZE, lruBuilder));
    }

    // TODO Fill me in!
    private static EnumMap<CacheType, ICacheOptions> MtlmCacheSizes =
            new EnumMap<CacheType, ICacheOptions>(CacheType.class);
    static {
        MtlmCacheSizes.put(StringsCache, new CacheOptions(STRINGS_CACHE_SIZE, lruBuilder));
        MtlmCacheSizes.put(NodesCache, new CacheOptions(NODES_CACHE_SIZE, lruBuilder));
        MtlmCacheSizes.put(ValuesCache, new CacheOptions(VALUES_CACHE_SIZE, lruBuilder));
        MtlmCacheSizes.put(ProfilesCache, new CacheOptions(PROFILES_CACHE_SIZE, lruBuilder));
        MtlmCacheSizes.put(SignaturesCache, new CacheOptions(SIGNATURES_CACHE_SIZE, lruBuilder));
    }

    /**
     * Cache types for Stream Dataset
     */
    public enum CacheType {
        StringsCache, NodesCache, ValuesCache, ProfilesCache, SignaturesCache
    }

    @SuppressWarnings("unused")
    public interface CacheSet {
        java.util.Map<CacheType, ICacheOptions> getCacheConfiguration();
    }

    public enum CacheTemplate implements CacheSet {
        Default(defaultCacheSizes),
        SingleThreadLowMemory(StlmCacheSizes),
        SingleThread(StCacheSizes),
        MultiThreadLowMemory(MtlmCacheSizes),
        MultiThread(MtCacheSizes);

        private EnumMap<CacheType, ICacheOptions> configuration =
                new EnumMap<CacheType, ICacheOptions>(CacheType.class);
        
        CacheTemplate(EnumMap<CacheType, ICacheOptions> configuration) {
            this.configuration.putAll(configuration);
        }

        @Override
        public java.util.Map<CacheType, ICacheOptions> getCacheConfiguration(){
            return configuration;
        }
    }
    
    private java.util.Map<CacheType, ICacheOptions> cacheMap = new EnumMap<CacheType, ICacheOptions>(CacheType.class);

    // prevent direct construction
    private DatasetBuilder() {

    }

    /**
     * Create a stream file dataset
     */
    public static BuildFromFile file() {
        return new DatasetBuilder().new BuildFromFile();
    }

    /**
     * Create a stream buffer dataset
     */
    public static BuildFromBuffer buffer() {
        return new DatasetBuilder().new BuildFromBuffer();
    }


    /**
     * Holds cache methods for buffer and file stream mode
     */
    @SuppressWarnings("WeakerAccess")
    public class Cachable<T extends Cachable<T>> {

        // no direct instantiation
        private Cachable () {

        }

        /**
         * Set a cache builder to use for the specified type of cache
         * @param cacheType The cache type
         * @param builder The cache builder used to create the cache.
         *                If null is passed then the specified cache type
         *                will operate without a cache.
         * @return The {@link DatasetBuilder}
         */
        public T setCacheBuilder(CacheType cacheType, ICacheBuilder builder){
            if(cacheMap.containsKey(cacheType)) {
                cacheMap.get(cacheType).setBuilder(builder);
            } else {
                cacheMap.put(cacheType, new CacheOptions(
                        CacheTemplate.Default.getCacheConfiguration().get(cacheType).getSize(), builder));
            }
            return (T) this;
        }

        /**
         * Set cache builders for multiple cache types
         * @param map A map of cache types and associated cache builders.
         *            Where a null builder is supplied the associated cache
         *            type will operate without a cache
         * @return The {@link DatasetBuilder}
         */
        public T setCacheBuilders(java.util.Map<CacheType, ICacheBuilder> map){
            for (CacheType cacheType: map.keySet()) {
                setCacheBuilder(cacheType, map.get(cacheType));
            }
            return (T) this;
        }

        /**
         * Set the builder and size parameter for the specified cache type
         * @param cacheType the type
         * @param options An {@link ICacheOptions} object that
         *                specifies a cache builder and size to use when
         *                constructing the specified cache type
         */
        public T configureCache(CacheType cacheType, ICacheOptions options) {
            cacheMap.put(cacheType, options);
            //noinspection unchecked
            return (T) this;
        }

        /**
         * Set builders and size parameters for multiple cache types
         * @param map a map of {@link ICacheBuilder} and size parameters
         *            to use when constructing each cache type
         */
        public T configureCaches(java.util.Map<CacheType, ICacheOptions> map) {
            cacheMap.putAll(map);
            //noinspection unchecked
            return (T) this;
        }

        /**
         * Initialises the {@link DatasetBuilder} with the default cache configuration.
         * Individual elements of this configuration can be overridden by using the
         * ConfigureCache, ConfigureCaches, SetCacheBuilder and SetCacheBuilders methods
         */
        public T configureDefaultCaches() {
            configureCaches(CacheTemplate.Default.getCacheConfiguration());
            //noinspection unchecked
            return (T) this;
        }

        /**
         * Add cache configuration from a predefined cache template.
         * Individual elements of this configuration can be overridden by using the
         * ConfigureCache, ConfigureCaches, SetCacheBuilder and SetCacheBuilders methods
         * @param template A {@link CacheTemplate} that defines the desired
         *                 cache configuration
         */
        public T configureCachesFromTemplate(CacheTemplate template) {
            return configureCachesFromCacheSet(template);
        }

        /**
         * Add caches from a CacheSet
         * To add, say, your own Template with the default LruCache
         * do as follows:
         * <p>
         * <code>
         *     addCachesFromCacheSet(myCacheSet, LruCache.builder())
         * </code>
         *
         * @param set the template
         */
        public T configureCachesFromCacheSet(CacheSet set) {
            configureCaches(set.getCacheConfiguration());
            //noinspection unchecked
			return (T) this;
        }
    }

    /**
     * Buffer dataset builder
     */
    public class BuildFromBuffer extends Cachable<BuildFromBuffer>{

        // cannot be instantiated directly
        private BuildFromBuffer() {

        }

        /**
         * build the dataset from a buffer
         * @param buffer the buffer
         */
        public IndirectDataset build(byte[] buffer) throws IOException {
            IndirectDataset dataSet = new IndirectDataset(buffer, Modes.MEMORY_MAPPED);
            loadForStreaming(dataSet, cacheMap);
            return dataSet;
        }
    }

    /**
     * File dataset builder
     */
    @SuppressWarnings("WeakerAccess")
    public class BuildFromFile extends Cachable<BuildFromFile> {

        private boolean isTempFile = false;
        private Date lastModified = null;

        // cannot be instantiated directly
        private BuildFromFile() {

        }

        /**
         * If this dataset is built from a file, delete the file after close
         */
        public BuildFromFile setTempFile() {
            isTempFile = true;
            return this;
        }

        /**
         * If this dataset is built from a file
         * @param isTemp if true, delete the file after close
         */
        public BuildFromFile setTempFile(boolean isTemp) {
            isTempFile = isTemp;
            return this;
        }

        /**
         * If this dataset is built from a file, override the creation date
         * @param date the date
         */
        public BuildFromFile lastModified(Date date) {
            lastModified = date;
            return this;
        }

        /**
         * build the dataset from a file
         * @param filename the filename to build from
         */
        public IndirectDataset build(String filename) throws IOException {
            Date modDate = lastModified;
            if (modDate == null) {
                modDate = new Date(new File(filename).lastModified());
            }
            IndirectDataset dataSet = new IndirectDataset(filename, modDate, Modes.FILE, isTempFile);
            loadForStreaming(dataSet, cacheMap);
            return dataSet;
        }
    }

    /*
    public Memory memory() {
        return new DatasetBuilder().new Memory();
    }

    public class Memory extends DatasetBuilder {

        public Memory init() {
            init = true;
            return this;
        }

        public Memory init(boolean isInit) {
            init = isInit;
            return this;
        }

        public Dataset build(String filename) throws IOException {
            Date modDate = lastModified;
            if (modDate.equals(DATE_NONE)) {
                modDate = new Date(new File(filename).lastModified());
            }
            Dataset dataSet = new Dataset(modDate, Modes.MEMORY);
            loadForMemory(dataSet, init);
            return dataSet;
        }

        public Dataset build(byte[] buffer) throws IOException {
            Date modDate = lastModified;
            if (modDate.equals(DATE_NONE)) {
                modDate = new Date();
            }
            Dataset dataSet = new Dataset(modDate, Modes.MEMORY);
            loadForMemory(dataSet, init);
            return dataSet;
        }
    }
*/

    /**
     * Class adapts an EntityFactory to a Loader
     *
     * @param <V> type of the entity
     */
    private static class EntityLoader<V> implements IValueLoader<Integer, V> {

        final IndirectDataset dataset;
        final BaseEntityFactory<V> entityFactory;
        final Header header;
        boolean fixedLength = false;

        EntityLoader(Header header, IndirectDataset dataset, BaseEntityFactory<V> entityFactory) {
            this.dataset = dataset;
            this.entityFactory = entityFactory;
            this.header = header;
            try {
                getEntityFactory().getLength();
                fixedLength = true;
            } catch (UnsupportedOperationException ignored) {
                // expected for variable length entities
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public V load(Integer key) throws IOException {
            BinaryReader reader = dataset.pool.getReader();
            try {
                if (fixedLength) {
                    reader.setPos(header.getStartPosition()
                            + (getEntityFactory().getLength() * key));
                } else {
                    reader.setPos(header.getStartPosition() + key);
                }
                return entityFactory.create(dataset, key, reader);
            } finally {
                dataset.pool.release(reader);
            }
        }

        @SuppressWarnings("WeakerAccess")
        public int nextPosition(int position, V result) throws IOException {
            if (fixedLength) {
                return ++position;
            } else {
                // this method supported only for variable length entities
                return position + getEntityFactory().getLength(result);
            }
        }

        @SuppressWarnings("WeakerAccess")
        public BaseEntityFactory<V> getEntityFactory() {
            return entityFactory;
        }

        public Header getHeader() {
            return header;
        }

    }

    /**
     * A cacheing entity loader that uses an {@link LruCache}
     *
     * @param <V> type of entity
     */
    private static class LruEntityLoader<V> extends EntityLoader<V> {

        private LruCache<Integer, V> cache;

        LruEntityLoader(final Header header, final IndirectDataset dataset, final BaseEntityFactory<V> entityFactory, LruCache<Integer, V> cache) {
            super(header, dataset, entityFactory);
            this.cache = cache;
            this.cache.setCacheLoader(new EntityLoader<V>(header, dataset, entityFactory));
        }

        @Override
        public V load(Integer key) throws IOException {
            return cache.get(key);
        }
    }

    /**
     * A caching entity loader that uses a {@link IPutCache}
     *
     * @param <V> type of entity
     */

    private static class CachedEntityLoader<V> extends EntityLoader<V> {

        private IPutCache<Integer, V> cache;

        CachedEntityLoader(Header header, IndirectDataset dataset, BaseEntityFactory<V> entityFactory, IPutCache<Integer, V> cache) {
            super(header, dataset, entityFactory);
            this.cache = cache;
        }

        @Override
        public V load(Integer key) throws IOException {
            V value;
            value = cache.get(key);
            if (value == null) {
                value = super.load(key);
                if (value != null) {
                    cache.put(key, value);
                }
            }
            return value;
        }
    }

    /**
     * Implementation of IReadOnlyList for Streams
     *
     * @param <T> type of entity
     */
    private static class StreamList<T extends BaseEntity> implements IReadonlyList<T> {

        private EntityLoader<T> loader;

        StreamList(EntityLoader<T> loader) {
            this.loader = loader;
        }

        @Override
        public T get(int i) throws IOException {
            return loader.load(i);
        }

        @Override
        public int size() {
            return this.loader.getHeader().getCount();
        }

        @Override
        public void close() throws IOException {

        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                // the item number
                int count = 0;
                // the position in the file or the item number (as above)
                // depending on whether the entity is fixed or variable size
                int position = 0;
                // number of elements
                int total = loader.getHeader().getCount();

                @Override
                public boolean hasNext() {
                    return count < total;
                }

                @Override
                public T next() {
                    try {
                        if (count >= total) {
                            throw new NoSuchElementException();
                        }
                        T result = get(position);
                        count++;
                        position = loader.nextPosition(position, result);
                        return result;
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove not supported");

                }
            };
        }
    }

    /**
     * helper to create an appropriate loader for a cached list given the cache type
     *
     * @param header  the header defining the list this will create the loader for
     * @param cache   the cache, or null
     * @param dataset the dataset
     * @param factory the factory for the type
     * @param <T>     the type
     * @return an entity loader
     */
    @SuppressWarnings("unchecked")
    private static <T> EntityLoader<T> getLoaderFor(Header header, ICache cache, IndirectDataset dataset, BaseEntityFactory factory) {
        EntityLoader loader;
        if (cache == null) {
            loader = new EntityLoader(header, dataset, factory);
        } else if (cache instanceof LruCache) {
            loader = new LruEntityLoader(header, dataset, factory, (LruCache) cache);
        } else if (cache instanceof IPutCache) {
            loader = new CachedEntityLoader(header, dataset, factory, (IPutCache) cache);
        } else {
            throw new IllegalStateException("Cache must be null, LruCache or IPutCache");
        }
        return loader;

    }

    /**
     * Load the necessary values from the data
     * file in to the Dataset. Stream mode only loads the essential information
     * such as file headers.
     *
     * @param dataSet The dataset object to load in to.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("null")
    private static void loadForStreaming(IndirectDataset dataSet, java.util.Map<CacheType, ICacheOptions> cacheConfiguration) throws IOException {
        BinaryReader reader = dataSet.pool.getReader();
        try {
            java.util.Map<CacheType, ICache> cacheMap = buildCaches(cacheConfiguration);

            dataSet.setCacheMap(cacheMap);
            reader.setPos(0);
            //Load headers that are common for both V31 and V32.
            CommonFactory.loadHeader(dataSet, reader);

            EntityLoader<AsciiString> loader = getLoaderFor(new Header(reader), cacheMap.get(StringsCache), dataSet, new AsciiStringFactory());
            dataSet.strings = new StreamList<AsciiString>(loader);

            MemoryFixedList<Component> components;
            switch (dataSet.versionEnum) {
                case PatternV31:
                    components = new MemoryFixedList<Component>(
                            dataSet, reader, new ComponentFactoryV31());
                    break;
                case PatternV32:
                    components = new MemoryFixedList<Component>(
                            dataSet, reader, new ComponentFactoryV32());
                    break;

                default:
                    throw new IllegalStateException("Unknown data version number");
            }
            dataSet.components = components;

            MemoryFixedList<Map> maps = new MemoryFixedList<Map>(
                    dataSet, reader, new MapFactory());
            dataSet.maps = maps;

            PropertiesList properties = new PropertiesList(
                    dataSet, reader, new PropertyFactory());
            dataSet.properties = properties;

            EntityLoader<Value> valueLoader = getLoaderFor(new Header(reader), cacheMap.get(ValuesCache),
                    dataSet, new ValueFactory());
            dataSet.values = new StreamList<Value>(valueLoader);

            EntityLoader<Profile> profileLoader = getLoaderFor(new Header(reader), cacheMap.get(ProfilesCache),
                    dataSet, new ProfileStreamFactory());
            dataSet.profiles = new StreamList<Profile>(profileLoader);

            switch (dataSet.versionEnum) {
                case PatternV31:
                    EntityLoader<Signature> signature31Loader = getLoaderFor(new Header(reader), cacheMap.get(SignaturesCache),
                            dataSet, new SignatureFactoryV31(dataSet));
                    dataSet.signatures = new StreamList<Signature>(signature31Loader);
                    break;
                case PatternV32:
                    EntityLoader<Signature> signature32Loader = getLoaderFor(new Header(reader), cacheMap.get(SignaturesCache),
                            dataSet, new SignatureFactoryV32(dataSet));
                    dataSet.signatures = new StreamList<Signature>(signature32Loader);
                    dataSet.signatureNodeOffsets =
                            new IntegerList(dataSet, reader);
                    dataSet.nodeRankedSignatureIndexes =
                            new IntegerList(dataSet, reader);
                    break;
            }
            dataSet.rankedSignatureIndexes = new IntegerList(dataSet, reader);

            switch (dataSet.versionEnum) {
                case PatternV31:
                    EntityLoader<Node> node31Loader = getLoaderFor(new Header(reader), cacheMap.get(NodesCache),
                            dataSet, new NodeStreamFactoryV31());
                    dataSet.nodes = new StreamList<Node>(node31Loader);
                    break;
                case PatternV32:
                    EntityLoader<Node> node32Loader = getLoaderFor(new Header(reader), cacheMap.get(NodesCache),
                            dataSet, new NodeStreamFactoryV32());
                    dataSet.nodes = new StreamList<Node>(node32Loader);
                    break;
            }

            MemoryFixedList<Node> rootNodes = new MemoryFixedList<Node>(
                    dataSet, reader, new RootNodeFactory());
            dataSet.rootNodes = rootNodes;

            MemoryFixedList<ProfileOffset> profileOffsets =
                    new MemoryFixedList<ProfileOffset>(dataSet, reader,
                            new ProfileOffsetFactory());
            dataSet.profileOffsets = profileOffsets;

            //Read into memory all small lists which are frequently accessed.
            reader.setPos(components.header.getStartPosition());
            components.read(reader);
            reader.setPos(maps.header.getStartPosition());
            maps.read(reader);
            reader.setPos(properties.header.getStartPosition());
            properties.read(reader);
            reader.setPos(rootNodes.header.getStartPosition());
            rootNodes.read(reader);
            reader.setPos(profileOffsets.header.getStartPosition());
            profileOffsets.read(reader);

        } finally {
            if (reader != null) {
                dataSet.pool.release(reader);
            }
        }
    }

    /**
     * Build caches using the specified configuration.
     * The caches are returned in a map of CacheType to the cache instance
     * @param cacheConfiguration A dictionary mapping {@link CacheType} values to an object
     *                           implementing {@link ICacheOptions}.
     *                           This specifies the {@link ICacheBuilder} and size parameter to
     *                           use when constructing a cache of the associated type.
     * @return A map containing the created caches
     */
    private static java.util.Map<CacheType, ICache> buildCaches(
            java.util.Map<CacheType, ICacheOptions> cacheConfiguration) {
        java.util.Map<CacheType, ICache> caches = new EnumMap<CacheType, ICache>(CacheType.class);

        for (CacheType cacheType: cacheConfiguration.keySet()) {
            ICacheOptions options = cacheConfiguration.get(cacheType);
            if(options.getBuilder() != null) {
                caches.put(cacheType, options.getBuilder().build(options.getSize()));
            }
        }

        return caches;
    }
}
