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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Used to find the most frequently occurring values in multiple ordered lists
 * of integers.
 */
class MostFrequentFilter extends ArrayList<Integer> {
    
    private class OrderedItems implements Comparable<OrderedItems> {
        
        private final int[] array;
        private int index = 0;
        int value;
        
        OrderedItems(int[] array) {
            this.array = array;
            this.value = array[index];
        }

        boolean moveNext() {
            index++;
            if (index < array.length) {
                value = array[index];
                return true;
            } 
            return false;
        }

        @Override
        public int compareTo(OrderedItems o) {
            return value - o.value;
        }
    }
    
    private int topCount = 0;
    private int lowestCount = 0;
    private int lowestValue;
    private final LinkedList<OrderedItems> lists = 
            new LinkedList<OrderedItems>();
    
    /**
     * Constructor used for unit testing.
     * @param lists array of T arrays generated for unit testing
     * @throws IOException 
     */
    MostFrequentFilter(int[][] lists) {
        for (int[] list : lists) {
            this.lists.add(new OrderedItems(list));
        }
        Init();
    }
    
    MostFrequentFilter(List<Node> nodes) throws IOException {
        for (Node node : nodes) {
            this.lists.add(new OrderedItems(node.getRankedSignatureIndexes()));
        }
        Init();
    }
    
    private void Init() {
        Collections.sort(lists);
        countLowest();
        while (lists.size() >= topCount) {
            if (lowestCount > topCount) {
                super.clear();
                topCount = lowestCount;
            }
            if (lowestCount == topCount)
            {
                super.add(lists.getFirst().value);
            }
            moveNext();
        }
        Collections.sort(this);
    }
    
    private void moveNext() {
        if (lists.size() == 1) {
            // Only one node in the list left so just
            // move this one on and set the count at one.
            if (lists.getFirst().moveNext() == false) {
                lists.removeFirst();
                lowestCount = 0; 
            }
            else {
                lowestCount = 1;
            }
        }
        else {
            // Move down the list those nodes with higher values
            // once incremented.
            moveLowest();

            // Get the number of identical low values.
            countLowest();
        }
    }

    /**
     * Move all the nodes that are at the lowest value on by one ensuring they 
     * continue to be represented in ascending order of lowest value.
     */
    private void moveLowest() {
        OrderedItems node = lists.getFirst();
        while (node != null && node.value == lowestValue) {
            if (node.moveNext() == false) {
                lists.remove(node);
            }
            else {
                sortLowest();
            }
            node = lists.peekFirst();
        }
    }

    /**
     * Count the number of nodes with the same lowest value.
     */
    private void countLowest() {
        lowestCount = 0;
        ListIterator<OrderedItems> iterator = lists.listIterator();
        OrderedItems node = iterator.hasNext() ? iterator.next() : null;
        if (node != null) {
            lowestValue = node.value;
            do {
                lowestCount++;
                node = iterator.hasNext() ? iterator.next() : null;
            } while (node != null &&
                node.value == lowestValue);
        }
    }

    /**
     * Moves the first node down the list to maintain ordering based on the 
     * value of the current value for each node.
     */
    private void sortLowest() {
        ListIterator<OrderedItems> iterator = lists.listIterator();
        OrderedItems firstNode = iterator.next();
        OrderedItems node = iterator.hasNext() ? iterator.next() : null;
        while(node != null && firstNode.value > node.value) {
            node = iterator.hasNext() ? iterator.next() : null;
        }
        if (node == null || iterator.previous() != firstNode) {
            iterator.add(firstNode);
            lists.removeFirst();
        }
    }    
}
