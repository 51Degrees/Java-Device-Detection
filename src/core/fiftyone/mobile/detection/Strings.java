/* *********************************************************************
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * Rather than store a copy of every string held in the data files a list of
 * strings is used and the index of the string is held in the data classes.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Strings {

    /**
     * Index containing the hashcode of the string as the index and either the
     * index in the _values list as the value or a list of values that match the
     * hashcode. It is possible for several different values to share the same
     * hashcode.
     */
    public final SortedMap<Integer, Object> _index = new TreeMap<Integer, Object>();
    /**
     * All the strings used in the 51Degrees.mobi file are held in this stack.
     */
    final List<String> _values = new ArrayList<String>(50631);

   /**
    *
    * Adds a new string value to the list of strings. If the value already
    * exists then it's index is returned. If it doesn't then a new entry is
    * added.
    *
    * @param value String value to add.
    * @return Index of the string in the _values list. Used the Get method to
    * retrieve the string value later.
    */
   @SuppressWarnings("unchecked")
   public int add(final String value) {
       final int hashcode = value.hashCode();
       int result = indexOf(value, hashcode);

       // If the string does not exist add to the index and values.
       if (result == -1) {
           // This hashcode does not exist so add a new entry to the list.
           result = addValue(value);
           _index.put(hashcode, result);
           return result;
       }

       // If this isn't the value we're looking for because another string
       // shares the same hashcode add it's position to the index after the
       // new string has been added to the values list.
       if (!_values.get(result).equals(value)) {
           // Create the new list for the indexes.
           ArrayList<Integer> newList = new ArrayList<Integer>();
           final Object obj = _index.get(hashcode);
           if (obj.getClass() == Integer.class) {
               newList.add((Integer) obj);
           } else if (obj.getClass() == Integer[].class) {
               newList.addAll(Arrays.asList((Integer[])obj));
           } else {
               //if its neither add anyway, it will be needed...
               newList = new ArrayList<Integer>();
           }
           // This is a new value for an existing hashcode. Add it to
           // the list of strings before updating the index.
           result = addValue(value);
           newList.add(result);
           Integer[] newArray = new Integer[newList.size()];
           newList.toArray(newArray);
           _index.put(hashcode, newArray);
       }
       return result;
   }

    /**
     *
     * Returns the string at the index position provided. If the index is
     * invalid then return null.
     *
     * @param index Index of string required.
     * @return String value at the specified index.
     */
    public String get(final int index) {
        if (index < 0) {
            return null;
        }
        return _values.get(index);
    }

    /**
     *
     * Adds a value to the list
     *
     * @param value Value to add
     * @return The list index of the value
     */
    private int addValue(final String value) {
        int result = _values.size();
        _values.add(value);
        return result;
    }

    /**
     *
     * Gets the index of the value and hashcode. The hashcode is provided to
     * avoid calculating when it already exists from the string.
     *
     * @param value The value who's index is required from the list.
     * @param hashcode The hashcode of the value.
     * @return The integer index of the string value in the list, otherwise -1.
     */
    private int indexOf(final String value, final int hashcode) {
        final Object obj = _index.get(hashcode);
        // Does the hashcode exist in the list.
        if (obj != null) {
            // If the object is an integer return the index.
            if (obj.getClass() == Integer.class) {
                return (Integer) obj;
            }

            // If it's an array of objects, which is very rare because the hashcodes
            // will have to match return the 1st item if only one exists, or one that
            // matches the string value passed into the method.
            final Integer[] list = (Integer[]) obj;
            if (list.length == 1) {
                return list[0];
            }

            // Find the matching index.
            for (int index : list) {
                if (_values.get(index).equals(value)) {
                    return index;
                }
            }
        }
        return -1;
    }
}
