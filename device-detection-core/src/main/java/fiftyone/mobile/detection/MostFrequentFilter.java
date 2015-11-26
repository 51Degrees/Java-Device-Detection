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

import fiftyone.mobile.detection.entities.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Used to filter multiple lists of ordered ranked signature indexes so that 
 * signatures that appear the most times are set in the top indexes list.
 * <p>
 * This class should not be called as it is part of the internal logic.
 */
class MostFrequentFilter extends ArrayList<Integer> {
    
    /**
     * Constructor used for unit testing.
     * 
     * @param lists array of T arrays generated for unit testing.
     * @param maxResults the maximum number of results to return.
     * @throws IOException if there was a problem accessing data file.
     */
    MostFrequentFilter(int[][] lists, int maxResults) {
        Arrays.sort(lists, new ArrayLengthComparator());
        ArrayList<OrderedList> localLists = new ArrayList<OrderedList>();
        for (int[] l : lists) {
            localLists.add(new OrderedList(l));
        }
        Init(localLists.toArray(new OrderedList[localLists.size()]), 
                maxResults);
    }
    
    /**
     * Constructs a new instance of MostFrequentFilter.
     * <p>
     * The nodes are always ordered based on the ascending lowest value in 
     * each list that is current.
     * 
     * @param state current state of the match process.
     * @throws IOException if there was a problem accessing data file.
     */
    MostFrequentFilter(MatchState state) throws IOException {
        ArrayList<OrderedList> localLists = new ArrayList<OrderedList>();
        Node[] nds = state.getNodes().clone();
        Arrays.sort(nds, new NodeLengthComparator());
        for (Node node : nds) {
            localLists.add(new OrderedList(node.getRankedSignatureIndexes()));
        }
        Init(localLists.toArray(new OrderedList[localLists.size()]), 
                state.getDataSet().maxSignatures);
    }
    
    /**
     * Keep adding integers to the list until there are insufficient lists 
     * remaining to make a difference or we've reached the maximum number of 
     * results to return.
     * 
     * @param lists array of OrderedList to check.
     * @param maxResults upper limit.
     */
    private void Init(OrderedList[] lists, int maxResults) {
        int topCount = 0;
        if (lists.length == 1) {
            for (int i = 0; i < maxResults; i++) {
                add(lists[0].items[i]);
            }
        } else if (lists.length > 1) {
            for (int listIndex = 0; 
                    listIndex < lists.length && 
                    (lists.length - listIndex) >= topCount; 
                    listIndex++) {
                for (OrderedList ol : lists) {
                    ol.reset();
                }
                while (lists[listIndex].moveNext()) {
                    if (getHasProcessed(lists, listIndex) == false) {
                        int count = getCount(lists, listIndex, topCount);
                        if (count > topCount) {
                            topCount = count;
                            clear();
                        }
                        if (count == topCount) {
                            add(lists[listIndex].current());
                        }
                    }
                }
            }
        }
        Collections.sort(this);
        if (size() > maxResults) {
            removeRange(maxResults, size());
        }
    }
    
    /**
     * If the value of the target node has already been processed because
     * it's contained in a previous list then return true. If not and
     * it still needs to be checked return false.
     * 
     * @param lists OrderedList array being filtered.
     * @param index Index of the list whose current value should be checked in 
     * prior lists.
     * @return True if the value has been processed, otherwise false.
     */
    private boolean getHasProcessed(OrderedList[] lists, int index) {
        for (int i = (index - 1); i >= 0; i--) {
            if (lists[i].contains(lists[index].current())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the number of lists the target value is contained in.
     * 
     * @param lists being filtered.
     * @param index of the list whose current value should be counted.
     * @param topCount highest count so far.
     * @return Number of lists that contain the value held by the list at the 
     * index.
     */
    private int getCount(OrderedList[] lists, int index, int topCount) {
        int count = 1;
        for (int i = index + 1; 
                i < lists.length && 
                (lists.length - index + count) > topCount; 
                i++) {
            if (lists[i].contains(lists[index].current())) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Fronts an array of integers. Used to identify duplicate items in the 
     * lists that are being filtered.
     */
    private class OrderedList {
        private final int[] items;
        private int nextStartIndex;
        private int currentIndex;
        
        /**
         * Constructs a new instance of the OrderedList.
         * 
         * @param items Array of integers to include in the list.
         */
        OrderedList(int[] items) {
            this.items = items;
            this.nextStartIndex = 0;
            this.currentIndex = -1;
        }
        
        /**
         * Determines if the ordered list contains the value requested.
         * Updates the start index as we know the next request to this method 
         * will always be for a value larger than the one just passed.
         * 
         * @param value integer to be checked in the list.
         * @return True if the list contains the value, otherwise false.
         */
        boolean contains(int value) {
            int itemIndex = Arrays.binarySearch(
                    this.items, 
                    this.nextStartIndex, 
                    this.items.length - 1, 
                    value);
            this.nextStartIndex = itemIndex +1;
            if (itemIndex < 0) {
                this.nextStartIndex = ~itemIndex;
            }
            return itemIndex >= 0;
        }
        
        /**
         * @return item at current index.
         */
        int current() {
            return this.items[this.currentIndex];
        }
        
        /**
         * Increments index and returns True if the new index is still within 
         * array range, False otherwise. Invoking this method WILL INCREASE the 
         * index.
         * 
         * @return True if new index is within bounds of the current array, 
         * False otherwise.
         */
        boolean moveNext() {
            this.currentIndex++;
            return this.currentIndex < this.items.length;
        }
        
        /**
         * Resets the current and start indexes.
         */
        void reset() {
            this.currentIndex = -1;
            this.nextStartIndex = 0;
        }
    }
    
    /**
     * Comparator for two Nodes. The number of ranked signature indexes used to 
     * determine the larger/smaller Node.
     */
    private class NodeLengthComparator implements Comparator<Node> {
        @Override
        public int compare(Node n1, Node n2) {
            try {
                if (n1.getRankedSignatureIndexes().length > 
                        n2.getRankedSignatureIndexes().length) {
                    return 1;
                }
            } catch (IOException ex) {
                System.err.println("ERROR");
            }
            
            return -1;
        }
    }
    
    /**
     * Comparator for two integer arrays.
     */
    private class ArrayLengthComparator implements Comparator<int[]> {
        @Override
        public int compare(int[] t, int[] t1) {
                if (t.length > t1.length) {
                        return 1;
                }
                return -1;
        }
    }
}
