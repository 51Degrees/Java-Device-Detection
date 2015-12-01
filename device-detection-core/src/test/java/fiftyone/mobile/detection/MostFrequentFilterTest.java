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
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(TestType.TypeApi.class)
public class MostFrequentFilterTest extends DetectionTestSupport {
 
    private static final int NUMBER_OF_ARRAYS = 10;
    
    private static final int NUMBER_OF_ELEMENTS = 
            Short.MAX_VALUE / NUMBER_OF_ARRAYS;
    
    private int[] createArray(int size, int firstValue, int increment) {
        int[] array = new int[size];
        int lastValue = firstValue;
        for (int i = 0; i < size; i++) {
            array[i] = lastValue;
            lastValue += increment;
        }
        return array;
    }
    
    @Test
    @Category(TestType.TypeUnit.class)
    public void allDuplicates() throws IOException {
        int[][] arrays = new int[NUMBER_OF_ARRAYS][];
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = createArray(NUMBER_OF_ELEMENTS, 0, 1);
        }
        MostFrequentFilter filter =
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS);
        for (int i = 0; i < arrays[0].length; i++) {
            assertTrue(filter.get(i).equals(arrays[0][i]));
        }
    }
    
    @Test
    public void noDuplicates() throws IOException {
        int[][] arrays = new int[NUMBER_OF_ARRAYS][];
        int startValue = 1;
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = createArray(NUMBER_OF_ELEMENTS, startValue, 1);
            startValue += arrays[i].length;
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
        int[][] arrays = new int[NUMBER_OF_ARRAYS][];
        int startValue = 1;
        for (int i = 0; i < arrays.length - 1; i++) {
            arrays[i] = createArray(NUMBER_OF_ELEMENTS, startValue, 1);
            startValue += arrays[i].length;
        }
        arrays[arrays.length - 1] = arrays[0];
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS);
        for (int i = 0; i < arrays[0].length; i++) {
            assertTrue(filter.get(i) == arrays[0][i]);
        }
    }

    @Test
    public void oneDuplicateValue() throws IOException {
        int[][] arrays = new int[NUMBER_OF_ARRAYS][];
        int startValue = 1;
        for (int i = 0; i < arrays.length - 1; i++) {
            arrays[i] = createArray(NUMBER_OF_ELEMENTS, startValue, 1);
            startValue += arrays[i].length;
        }
        arrays[arrays.length - 1] = new int[] { 
            arrays[0][NUMBER_OF_ELEMENTS / 2] };
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == 1);
        assertTrue(filter.get(0) == arrays[arrays.length - 1][0]);
    }
    
    @Test
    public void multipleDuplicateValue() throws IOException {
        int[][] arrays = new int[NUMBER_OF_ARRAYS][];
        for (int i = 0; i < arrays.length - 1; i++) {
            arrays[i] = createArray(NUMBER_OF_ELEMENTS, 0, 1);
        }
        arrays[arrays.length - 1] = createArray(NUMBER_OF_ELEMENTS / 5, 0, 5);
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS / 5);
        for (int i = 0; i < arrays[arrays.length - 1].length; i++) {
            assertTrue(filter.get(i) == arrays[arrays.length - 1][i]);
        }
    }    

    @Test
    public void differentLengthDuplicateValue() throws IOException {
        int[][] arrays = new int[NUMBER_OF_ARRAYS][];
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = createArray(NUMBER_OF_ELEMENTS * (arrays.length - i), 0, 1);
        }
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS);
        for (int i = 0; i < arrays[arrays.length - 1].length; i++) {
            assertTrue(filter.get(i) == arrays[arrays.length - 1][i]);
        }
    }    

    @Test
    public void singleList() throws IOException {
        int[][] arrays = new int[1][];
        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = createArray(NUMBER_OF_ELEMENTS, 0, 1);
        }
        MostFrequentFilter filter = 
                new MostFrequentFilter(arrays, Integer.MAX_VALUE);
        assertTrue(filter.size() == NUMBER_OF_ELEMENTS);
        for (int i = 0; i < arrays[0].length; i++) {
            assertTrue(filter.get(i) == arrays[0][i]);
        }
    }
    
    @Test
    public void maxResults() {
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
}
