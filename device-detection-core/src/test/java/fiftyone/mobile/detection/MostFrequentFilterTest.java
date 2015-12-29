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

import fiftyone.mobile.detection.test.TestType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(TestType.TypeApi.class)
public class MostFrequentFilterTest extends DetectionTestSupport {
 
    private static final int NUMBER_OF_ARRAYS = 10;
    
    private static final int NUMBER_OF_ELEMENTS = 
            Short.MAX_VALUE / NUMBER_OF_ARRAYS;
    
    private List<Integer> createArray(int size, int firstValue, int increment) {
        List<Integer> localList = new ArrayList<Integer>();
        int lastValue = firstValue;
        for (int i = 0; i < size; i++) {
            localList.add(lastValue);
            lastValue += increment;
        }
        return localList;
    }
    
    @Test
    @Category(TestType.TypeUnit.class)
    public void allDuplicates() throws IOException {
        List<List<Integer>> arrays = new ArrayList<List<Integer>>();
        for (int i = 0; i < NUMBER_OF_ARRAYS; i++) {
            arrays.add(createArray(NUMBER_OF_ELEMENTS, 0, 1));
        }
        MostFrequentFilter filter =
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS);
        for (int i = 0; i < arrays.get(0).size(); i++) {
            assertTrue(filter.get(i).equals(arrays.get(0).get(i)));
        }
    }
    
    @Test
    public void noDuplicates() throws IOException {
        List<List<Integer>> arrays = new ArrayList<List<Integer>>();
        int startValue = 1;
        for (int i = 0; i < NUMBER_OF_ARRAYS; i++) {
            arrays.add(createArray(NUMBER_OF_ELEMENTS, startValue, 1));
            startValue += arrays.get(i).size();
        }
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == 
                NUMBER_OF_ELEMENTS * NUMBER_OF_ARRAYS);
        int lastValue = 0;
        for (int i = 0; i < filter.size(); i++) {
            assertTrue(lastValue < filter.get(i));
            lastValue = filter.get(i);
        }
    }

    @Test
    public void oneDuplicateArray() throws IOException {
        List<List<Integer>> arrays = new ArrayList<List<Integer>>();
        int startValue = 1;
        for (int i = 0; i < NUMBER_OF_ARRAYS; i++) {
            arrays.add(createArray(NUMBER_OF_ELEMENTS, startValue, 1));
            startValue += arrays.get(i).size();
        }
        arrays.set(arrays.size() - 1, arrays.get(0));
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS);
        for (int i = 0; i < arrays.size(); i++) {
            assertTrue(filter.get(i).equals(arrays.get(0).get(i)));
        }
    }

    @Test
    public void oneDuplicateValue() throws IOException {
        int DUPLICATE_ARRAY_INDEX = 1;
        List<List<Integer>> arrays = new ArrayList<List<Integer>>();
        int startValue = 1;
        for (int i = 0; i < NUMBER_OF_ARRAYS; i++) {
            arrays.add(createArray(NUMBER_OF_ELEMENTS, startValue, 1));
            startValue += arrays.get(i).size();
        }
        //arrays[arrays.Length - 1] = new int[] { arrays[0][NUMBER_OF_ELEMENTS / 2] };
        // Replace the last array of integers 
        //arrays.get(0).get(NUMBER_OF_ELEMENTS / 2)
        arrays.set((arrays.size() - 1), Arrays.asList(arrays.get(0).get(NUMBER_OF_ELEMENTS / 2)));
        
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == 1);
        assertTrue(filter.get(0).equals(arrays.get(arrays.size() - 1).get(0)));
        //assertTrue(filter.get(0) == arrays[arrays.length - 1][0]);
    }
    
    @Test
    public void multipleDuplicateValue() throws IOException {
        List<List<Integer>> arrays = new ArrayList<List<Integer>>();
        for (int i = 0; i < NUMBER_OF_ARRAYS; i++) {
            arrays.add(createArray(NUMBER_OF_ELEMENTS, 0, 1));
        }
        arrays.set(arrays.size() - 1, createArray(NUMBER_OF_ELEMENTS / 5, 0, 5));
        //arrays[arrays.length - 1] = createArray(NUMBER_OF_ELEMENTS / 5, 0, 5);
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS / 5);
        //arrays[arrays.length - 1].length
        for (int i = 0; i < arrays.get(arrays.size() - 1).size(); i++) {
            assertTrue(filter.get(i).equals(arrays.get(arrays.size() - 1).get(i)));
            //assertTrue(filter.get(i) == arrays[arrays.length - 1][i]);
        }
    }    

    @Test
    public void differentLengthDuplicateValue() throws IOException {
        List<List<Integer>> arrays = new ArrayList<List<Integer>>();
        for (int i = 0; i < NUMBER_OF_ARRAYS; i++) {
            arrays.add(createArray(NUMBER_OF_ELEMENTS * (NUMBER_OF_ARRAYS - i), 0, 1));
        }
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        System.out.println(filter.size());
        System.out.println(NUMBER_OF_ELEMENTS);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS);
        // arrays[arrays.length - 1].length
        for (int i = 0; i < arrays.get(arrays.size() - 1).size(); i++) {
            assertTrue(filter.get(i).equals(arrays.get(arrays.size() - 1).get(i)));
            //assertTrue(filter.get(i) == arrays[arrays.length - 1][i]);
        }
    }    

    @Test
    public void singleList() throws IOException {
        List<List<Integer>> arrays = new ArrayList<List<Integer>>();
        for (int i = 0; i < 1; i++) {
            arrays.add(createArray(NUMBER_OF_ELEMENTS, 0, 1));
        }
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS);
        for (int i = 0; i < arrays.get(0).size(); i++) {
            assertTrue(filter.get(i).equals(arrays.get(0).get(i)));
            // assertTrue(filter.get(i) == arrays[0][i]);
        }
    }
    
    /*
    @Test
    public void maxResults() {
        List<List<Integer>> arrays = new ArrayList<List<Integer>>(4);
        int startValue = 1;
        for (int i = 2; i >= 0; i -= 2) {
            arrays.add(i, createArray(NUMBER_OF_ELEMENTS, startValue, 1));
            arrays.add(i + 1, createArray(NUMBER_OF_ELEMENTS, startValue, 1));
            startValue += arrays.get(i).size();
        }
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, NUMBER_OF_ELEMENTS / 10);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS / 10);
        for (int i = 0; i < filter.size() - 1; i++) {
            assertTrue(filter.get(i) < filter.get(i + 1));
        }
        /*
        int[][] arrays = new int[4][];
        int startValue = 1;
        for (int i = 2; i >= 0; i -= 2) {
            arrays[i] = createArray(NUMBER_OF_ELEMENTS, startValue, 1);
            arrays[i + 1] = createArray(NUMBER_OF_ELEMENTS, startValue, 1);
            startValue += arrays[i].length;
        }
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, NUMBER_OF_ELEMENTS / 10);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS / 10);
        for (int i = 0; i < filter.size() - 1; i++) {
            assertTrue(filter.get(i) < filter.get(i + 1));
        }
                
    }
                */
}
