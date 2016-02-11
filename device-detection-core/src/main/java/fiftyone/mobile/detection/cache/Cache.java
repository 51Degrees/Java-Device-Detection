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
package fiftyone.mobile.detection.cache;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Many of the entities used by the detector data set are requested repeatedly. 
 * The cache improves memory usage and reduces strain on the garbage collector
 * by storing previously requested entities for a period of time to avoid the 
 * need to re-fetch them from the underlying storage mechanism.
 * <p>
 * The Least Recently Used (LRU) cache is used. LRU cache keeps track of what 
 * was used when in order to discard the least recently used items first.
 * Every time a cache item is used the "age" of the item used is updated.
 * <p>
 * Cache is implemented using the doubly linked list of Nodes where each Node 
 * tracks the next and previous Node and contains a value. Cache entries are 
 * stored as a Key : Value pair.
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
public class Cache<K, V> {
   
    /**
     * A key value pair for cached items.
     */
    class KeyValuePair {
        final K key;
        final V value;
        public KeyValuePair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    class Node<T> {
        final T item;
        Node next;
        Node previous;
        DoublyLinkedList list;

        public DoublyLinkedList getList() {
            return list;
        }

        public Node(T item) {
            this.item = item;
        }
    }
    
    class DoublyLinkedList {

        Node first = null;
        Node last = null;

        void clear() {
            first = null;
            last = null;
        }
        
        void addFirst(Node newNode) {
            newNode.list = linkedList;
            if (first == null) {
                newNode.next = null;
                newNode.previous = null;
                first = newNode;
                last = newNode;
            } else {
                first.previous = newNode;
                newNode.next = first;
                newNode.previous = null;
                first = newNode;
            }
        }

        void remove(Node node) {
            if (node.previous != null) {
                node.previous.next = node.next;
            }
            if (node.next != null) {
                node.next.previous = node.previous;
            }
            if (node == first) {
                first = first.next;
            }
            if (node == last) {
                last = last.previous;
            }
            node.list = null;
        }
        
        Node removeFirst() {
            Node result = first;
            if (first.next == null) {
                first = null;
                last = null;
            } else {
                first = first.next;
                first.previous = null;
            }
            result.list = null;
            return first;
        }

        Node removeLast() {
            Node result = last;
            if (first.next == null) {
                first = null;
                last = null;
            } else {
                last = last.previous;
                last.next = null;
            }
            result.list = null;
            return result;
        }
    }    

    /**
     * Used to synchronise access to the the dictionary and linked list in the
     * function of the cache.
     */
    private final Object writeLock = new Object();
    
    /**
     * Loader used to fetch items not in the cache.
     */
    private final ICacheLoader<K, V> loader;

    /**
     * Hash map of keys to item values.
     */
    private final ConcurrentHashMap<K, Node> hashMap;

    /**
     * A doubly linked list of nodes. Not marked private so that the unit
     * test can check the elements.
     */
    final DoublyLinkedList linkedList;
    
    /**
     * Constructs a new instance of the cache.
     * @param cacheSize The number of items to store in the cache.
     */
    public Cache(int cacheSize) {
        this(cacheSize, null);
    }

    /**
     * Constructs a new instance of the cache.
     * 
     * @param cacheSize The number of items to store in the cache.
     * @param loader used to fetch items not in the cache.
     */    
    public Cache(int cacheSize, ICacheLoader<K,V> loader) {
        this.cacheSize = new AtomicInteger(cacheSize);
        this.loader = loader;
        this.hashMap = new ConcurrentHashMap<K,Node>(cacheSize);
        this.linkedList = new DoublyLinkedList();
    }

    /**
     * Gets the size of the cache.
     *
     * @return size of the cache.
     */
    public int getCacheSize() { return cacheSize.get(); }

    /**
     * Sets the size of the cache. Used to improve performance when
     * more frequently requested items are likely to be required.
     * For example
     * {@link fiftyone.mobile.detection.entities.Property#findProfiles(String, List)}.
     * .
     *
     * @param size
     */
    public void setCacheSize(int size) { cacheSize.set(size); }
    private AtomicInteger cacheSize;

    /**
     * @return number of cache misses.
     */
    public long getCacheMisses() {
        return misses.get();
    }
    private final AtomicLong misses = new AtomicLong(0);
    
    /**
     * @return number of requests received by the cache.
     */
    public long getCacheRequests() {
        return requests.get();
    }
    private final AtomicLong requests = new AtomicLong(0);
    
    /**
     * @return the percentage of times cache request did not return a result.
     */
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
     * @throws java.io.IOException if there was a problem accessing data file.
     */    
    public V get(K key) throws IOException {
        return get(key, loader);
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
    public V get(K key, ICacheLoader<K,V> loader) throws IOException {
        boolean added = false;
        requests.incrementAndGet();
        Node node = hashMap.get(key);
        if (node == null) {
            // Get the item fresh from the loader before trying
            // to write the item to the cache.
            misses.incrementAndGet();
            V value = loader.fetch(key);

            synchronized(writeLock) {
                // If the node has already been added to the dictionary
                // then get it, otherise add the one just fetched.
                Node newItem = new Node(new KeyValuePair(key, value));
                node = hashMap.putIfAbsent(key, newItem);

                // If the node got from the dictionary is the new one
                // just feteched then it needs to be added to the linked
                // list. The value just added to the hash map needs to set
                // as the returned item.
                if (node == null)
                {
                    added = true;
                    node = newItem;
                    
                    // Add the key to the head of the linked list.
                    linkedList.addFirst(node);

                    // Check to see if the cache has grown and if so remove
                    // the last element.
                    removeLeastRecent();
                }
            }
        }
        if (added == false) {
            // The item is in the dictionary. Check it's still in the list
            // and if so them move the key to the head of the list.            
            synchronized(writeLock) {
                if (node.list != null) {
                    linkedList.remove(node);
                    linkedList.addFirst(node);
                }
            }
        }
        KeyValuePair kvp = (KeyValuePair)node.item;
        return kvp.value;
    }
    
    /**
     * Removes the last item in the cache if the cache size is reached.
     */
    private void removeLeastRecent() {
        if (hashMap.size() > cacheSize.get()) {
            Node removedNode = linkedList.removeLast();
            KeyValuePair kvp = (KeyValuePair)removedNode.item;
            assert hashMap.remove(kvp.key) != null;
            assert hashMap.size() == cacheSize.get();
        }
    }

    /**
     * Resets the 'stats' for the cache.
     */
    public void resetCache()
    {
        this.hashMap.clear();
        this.linkedList.clear();
        misses.set(0);
        requests.set(0);
    }
}
