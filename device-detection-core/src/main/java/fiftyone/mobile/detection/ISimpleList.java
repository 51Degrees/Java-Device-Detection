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
 * This Source Code Form is “Incompatible With Secondary Licenses”, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */

package fiftyone.mobile.detection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Provides the ability to efficiently retrieve the items from the list using 
 * a ranged enumerable. This list can be used with types that are returned from 
 * the BinaryReader implementation where a factory is not required to construct 
 * the entity.
 */
public interface ISimpleList {
    
    /**
     * Returns the values in the list starting at the index provided.
     * 
     * @param index first index of the range required.
     * @param count number of elements to return.
     * @return A list of the items in the range requested.
     */
    public List<Integer> getRange(int index, int count);
    
    /**
     * Returns the value in the list at the index provided.
     * 
     * @param index of the value required.
     * @return Value at the index requested.
     */
    public int get(int index);
    
    /**
     * @return the number of items in the list.
     */
    public int size();
    
    /**
     * Rather then making a copy of a section of an array in memory to a new 
     * array containing a subset of elements this class is used to create an 
     * IList that will return the subset of the underlying larger array.
     * <p>
     * This class should not be referenced directly as it is part of the 
     * internal logic.
     * 
     * @param <T> type of data to hold in the array.
     */
    class ArrayRange<T> implements List {

        private final int startIndex;
        private final int length;
        private final T[] array;
        
        /**
         * Create a new array range for the provided array, starting from the 
         * startIndex and spanning the number of elements provided by length.
         * 
         * @param startIndex start from element.
         * @param length how many elements to consider.
         * @param array list to consider.
         */
        public ArrayRange(int startIndex, int length, T[] array) {
            if (length > array.length) {
                throw new ArrayIndexOutOfBoundsException("The provided range "
                        + "is invalid.");
            }
            this.startIndex = startIndex;
            this.length = length;
            this.array = array;
        }
        
        @Override
        public T get(int i) {
            return this.array[this.startIndex + i];
        }
        
        @Override
        public int size() {
            return this.length;
        }
        
        @Override
        public Iterator iterator() {
            return new ISimpleListIterator(length, startIndex, this);
        }
        
        public T[] getArray() {
            return this.array;
        }

        // <editor-fold defaultstate="collapsed" desc="Unsupported methods">
        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object[] toArray(Object[] ts) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean add(Object e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean containsAll(Collection clctn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addAll(Collection clctn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addAll(int i, Collection clctn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeAll(Collection clctn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean retainAll(Collection clctn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object set(int i, Object e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void add(int i, Object e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object remove(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int indexOf(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int lastIndexOf(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ListIterator listIterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ListIterator listIterator(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List subList(int i, int i1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        // </editor-fold>
    }
}
