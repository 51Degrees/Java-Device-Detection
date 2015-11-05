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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Many of the entities used by the detector data set are requested repeatedly. 
 * The cache improves memory usage and reduces strain on the garbage collector
 * by storing previously requested entities for a short period of time to avoid 
 * the need to re-fetch them from the underlying storage mechanism.
 * 
 * The cache works by maintaining two dictionaries of entities keyed on their 
 * offset or index. The inactive list contains all items requested since the 
 * cache was created or last serviced. The active list contains all the items 
 * currently in the cache. The inactive list is always updated when an item is 
 * requested.
 * 
 * When the cache is serviced the active list is destroyed and the inactive list
 * becomes the active list. i.e. all the items that were requested since the 
 * cache was last serviced are now in the cache. A new inactive list is created 
 * to store all items being requested since the cache was last serviced.
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

    class Node {
        final KeyValuePair item;
        Node next;
        Node previous;
        DoublyLinkedList list;

        public DoublyLinkedList getList() {
            return list;
        }

        public Node(KeyValuePair item) {
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
     * The number of items the cache lists should have capacity for.
     */
    private final int cacheSize;
    
    /**
     * The number of requests made to the cache.
     */
    private final AtomicLong requests = new AtomicLong(0);
    
    /**
     * The number of times an item was not available.
     */
    private final AtomicLong misses = new AtomicLong(0);

    /**
     * Constructs a new instance of the cache.
     * @param cacheSize The number of items to store in the cache.
     */
    public Cache(int cacheSize) {
        this(cacheSize, null);
    }
    
    /**
     * Constructs a new instance of the cache.
     * @param cacheSize The number of items to store in the cache.
     * @param loader used to fetch items not in the cache.
     */    
    public Cache(int cacheSize, ICacheLoader<K,V> loader) {
        this.cacheSize = cacheSize;
        this.loader = loader;
        this.hashMap = new ConcurrentHashMap<K,Node>(cacheSize);
        this.linkedList = new DoublyLinkedList();
    }

    /**
     * @return number of misses
     */
    public long getCacheMisses() {
        return misses.get();
    }
    
    /**
     * @return number of requests
     */
    public long getCacheRequests() {
        return requests.get();
    }
    
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
     * @param key or the item required
     * @return An instance of the value associated with the key
     * @throws java.io.IOException
     */    
    public V get(K key) throws IOException {
        return get(key, loader);
    }
    
    /**
     * Retrieves the value for key requested. If the key does not exist
     * in the cache then the Fetch method is used to retrieve the value
     * from another loader.
     * @param key or the item required
     * @param loader to fetch the items from
     * @return An instance of the value associated with the key
     * @throws java.io.IOException
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
        return node.item.value;
    }
    
    /**
     * Removes the last item in the cache if the cache size is reached.
     */
    private void removeLeastRecent() {
        if (hashMap.size() > cacheSize) {
            Node removedNode = linkedList.removeLast();
            assert hashMap.remove(removedNode.item.key) != null;
            assert hashMap.size() == cacheSize;
        }
    }

    /**
     * Resets the stats for the cache.
     */
    public void resetCache()
    {
        this.hashMap.clear();
        this.linkedList.clear();
        misses.set(0);
        requests.set(0);
    }
}
