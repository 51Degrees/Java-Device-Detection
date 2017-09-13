/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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
package fiftyone.mobile.detection.cache;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Many of the entities used by the detector data set are requested repeatedly. 
 * The cache improves memory usage and reduces strain on the garbage collector
 * by storing previously requested entities for a period of time to avoid the 
 * need to re-fetch them from the underlying storage mechanism.
 * <p>
 * A variation of a Least Recently Used (LRU) cache is used. LRU cache keeps
 * track of what was used when in order to discard the least recently used
 * items first. Every time a cache item is used the "age" of the item used
 * is updated.
 * <p>
 * This implementation supports concurrency by using multiple linked lists
 * in place of a single linked list in the original implementation.
 * The linked list to use is assigned at random and stored in the cached
 * item. This will generate an even set of results across the different
 * linked lists. The approach reduces the probability of the same linked
 * list being locked when used in a environments with a high degree of
 * concurrency. If the feature is not required then the constructor should be
 * provided with a concurrency value of 1.
 * <p>
 * For a vast majority of the real life environments a constant stream of unique 
 * User-Agents is a fairly rare event. Usually the same User-Agent can be 
 * encountered multiple times within a fairly short period of time as the user 
 * is making a subsequent request. Caching frequently occurring User-Agents 
 * improved detection speed considerably.
 * <p>
 * Some devices are also more popular than others and while the User-Agents for 
 * such devices may differ, the combination of components used would be very 
 * similar. Therefore internal caching is also used to take advantage of the 
 * more frequently occurring entities.
 * <p>
 * This class should not be called as it is part of the internal logic.
 * 
 * @param <K> Key for the cache items.
 * @param <V> Value for the cache items.
 */
public class LruCache<K, V>  implements ILoadingCache<K,V> {

    /**
     * An item stored in the cache along with references to the next and
     * previous items.
     */
    class CachedItem {

        /**
         * Key associated with the cached item.
         */
        final K key;

        /**
         * Value of the cached item.
         */
        final V value;

        /**
         * The next item in the linked list.
         */
        CachedItem next;

        /**
         * The previous item in the linked list.
         */
        CachedItem previous;

        /**
         * The linked list the item is part of.
         */
        final CacheLinkedList list;

        /**
         * Indicates that the item is valid and added to the linked list.
         * It is not in the process of being manipulated by another thread
         * either being added to the list or being removed.
         */
        boolean isValid;

        public CachedItem(CacheLinkedList list, K key, V value) {
            this.list = list;
            this.key = key;
            this.value = value;
        }
    }

    /**
     * A linked list used in the LruCache implementation.
     * This linked list implementation enables items to be moved
     * within the linked list.
     */
    class CacheLinkedList {

        /**
         * The cache that the list is part of.
         */
        LruCache cache = null;

        /**
         * The first item in the list.
         */
        CachedItem first = null;

        /**
         * The last item in the list.
         */
        CachedItem last = null;

        /**
         * Constructs a new instance of the CacheLinkedList.
         */
        public CacheLinkedList(LruCache cache) {
            this.cache = cache;
        }

        /**
         * Adds a new cache item to the linked list.
         */
        void addNew(CachedItem item)
        {
            boolean added = false;
            if (item != first)
            {
                synchronized(this)
                {
                    if (item != first)
                    {
                        if (first == null)
                        {
                            // First item to be added to the queue.
                            first = item;
                            last = item;
                        }
                        else
                        {
                            // Add this item to the head of the linked list.
                            item.next = first;
                            first.previous = item;
                            first = item;

                            // Set flag to indicate an item was added and if
                            // the cache is full an item should be removed.
                            added = true;
                        }

                        // Indicate the item is now ready for another thread
                        // to manipulate and is fully added to the linked list.
                        item.isValid = true;
                    }
                }
            }

            // Check if the linked list needs to be trimmed as the cache
            // size has been exceeded.
            if (added && cache.hashMap.size() > cache.cacheSize)
            {
                synchronized (this)
                {
                    if (cache.hashMap.size() > cache.cacheSize)
                    {
                        // Indicate that the last item is being removed from
                        // the linked list.
                        last.isValid = false;

                        // Remove the item from the dictionary before
                        // removing from the linked list.
                        cache.hashMap.remove(last.key);
                        last = last.previous;
                        last.next = null;
                    }
                }
            }
        }

        /**
         * Set the first item in the linked list to the item provided.
         */
        void moveFirst(CachedItem item)
        {
            if (item != first && item.isValid == true)
            {
                synchronized (this)
                {
                    if (item != first && item.isValid == true)
                    {
                        if (item == last)
                        {
                            // The item is the last one in the list so is
                            // easy to remove. A new last will need to be
                            // set.
                            last = item.previous;
                            last.next = null;
                        }
                        else
                        {
                            // The item was not at the end of the list.
                            // Remove it from it's current position ready
                            // to be added to the top of the list.
                            item.previous.next = item.next;
                            item.next.previous = item.previous;
                        }

                        // Add this item to the head of the linked list.
                        item.next = first;
                        item.previous = null;
                        first.previous = item;
                        first = item;
                    }
                }
            }
        }

        /**
         * Clears all items from the linked list.
         */
        void clear()
        {
            first = null;
            last = null;
        }
    }    

    /**
     * Loader used to fetch items not in the cache.
     */
    public void setCacheLoader(IValueLoader<K, V> loader) {
       this.loader = loader;
    }
    private IValueLoader<K, V> loader;

    /**
     * Hash map of keys to item values.
     */
    private final ConcurrentHashMap<K, CachedItem> hashMap;

    /**
     * A array of doubly linked lists. Not marked private so that the unit
     * test can check the elements.
     */
    final CacheLinkedList[] linkedLists;

    /**
     * Random number generator used to select the linked list to use with
     * the new item being added to the cache.
     */
    final Random random = new Random();
    
    /**
     * Constructs a new instance of the cache.
     * @param cacheSize The number of items to store in the cache.
     */
    public LruCache(int cacheSize) {
        this(cacheSize, null);
    }

    /**
     * Constructs a new instance of the cache.
     * 
     * @param cacheSize The number of items to store in the cache.
     * @param loader used to fetch items not in the cache.
     */    
    public LruCache(int cacheSize, IValueLoader<K,V> loader) {
        this(cacheSize, Runtime.getRuntime().availableProcessors(), loader);
    }

    /**
     * Constructs a new instance of the cache.
     *
     * @param cacheSize The number of items to store in the cache.
     * @param loader used to fetch items not in the cache.
     */
    public LruCache(int cacheSize, int concurrency, IValueLoader<K,V> loader) {
        if (concurrency <= 0)
        {
            throw new IllegalArgumentException(
                    "Concurrency must be a positive integer greater than 0.");
        }
        this.cacheSize = cacheSize;
        this.loader = loader;
        this.hashMap = new ConcurrentHashMap<K,CachedItem>(cacheSize);
        linkedLists = (CacheLinkedList[]) Array.newInstance(
                CacheLinkedList.class, concurrency);
        for(int i = 0; i < linkedLists.length; i++){
            linkedLists[i] = new CacheLinkedList(this);
        }
    }

    /**
     * The number of items the cache lists should have capacity for.
     *
     * @return capacity of the cache.
     */
    @Override
    public long getCacheSize() { return cacheSize; }
    private int cacheSize;

    /**
     * @return number of cache misses.
     */
    @Override
    public long getCacheMisses() {
        return misses.get();
    }
    private final AtomicLong misses = new AtomicLong(0);
    
    /**
     * @return number of requests received by the cache.
     */
    @Override
    public long getCacheRequests() {
        return requests.get();
    }
    private final AtomicLong requests = new AtomicLong(0);
    
    /**
     * @return the percentage of times cache request did not return a result.
     */
    @Override
    public double getPercentageMisses() {
        return misses.doubleValue()/ requests.doubleValue();
    }
    
    /**
     * Retrieves the value for key requested. If the key does not exist
     * in the cache then the Fetch method of the cache's loader is used to
     * retrieve the value.
     * 
     * @param key or the item required.
     * @return An instance of the value associated with the key.
     * @throws java.lang.IllegalStateException if there was a problem accessing data file.
     */    
    @Override
    public V get(K key) {
        try {
            return get(key, loader);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Retrieves the value for key requested. If the key does not exist
     * in the cache then the Fetch method is used to retrieve the value
     * from another loader.
     * 
     * @param key or the item required
     * @param loader to fetch the items from
     * @return An instance of the value associated with the key
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public V get(K key, IValueLoader<K, V> loader) throws IOException {
        boolean added = false;
        requests.incrementAndGet();
        // First, try to get the item from the hashMap
        CachedItem node = hashMap.get(key);
        if (node == null) {
            misses.incrementAndGet();
            // The item was not in the cache so need to load it
            // before trying to add it in.
            // Also get a randomly selected linked list to add
            // the item to.
            CachedItem newNode = new CachedItem(
                    GetRandomLinkedList(),
                    key,
                    loader.load(key));

            // If the node has already been added to the dictionary
            // then get it, otherwise add the one just fetched.
            node = hashMap.putIfAbsent(key, newNode);

            // If the node was absent and was added to the dictionary (node == null)
            // then it needs to be added to the linked list.
            if (node == null) {
                added = true;
                newNode.list.addNew(newNode);
                node = newNode;
            }
        }
        if (added == false) {
            // The item is in the dictionary.
            // Move the item to the head of it's LRU list.
            node.list.moveFirst(node);
        }

        return node.value;
    }

    /**
     * Resets the 'stats' for the cache.
     */
    @Override
    public void resetCache()
    {
        this.hashMap.clear();
        misses.set(0);
        requests.set(0);
        for(int i = 0; i < linkedLists.length; i++){
            linkedLists[i].clear();
        }
    }

    /**
     * Returns a random linked list.
     */
    private CacheLinkedList GetRandomLinkedList()
    {
        return linkedLists[random.nextInt(linkedLists.length)];
    }

    /**
     * Return builder for an LRU cache
     */
    public static ICacheBuilder builder() {
        return new LruBuilder();
    }

    public static class LruBuilder implements ICacheBuilder {
        public ICache build(int size) {
            return new LruCache(size);
        }
    }
}