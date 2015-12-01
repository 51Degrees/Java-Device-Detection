/*
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
 */

package fiftyone.device.example.illustration;

import fiftyone.device.example.Shared;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.mobile.detection.entities.Values;
import fiftyone.mobile.detection.factories.MemoryFactory;
import java.io.IOException;
import java.util.ArrayList;

/**
 * <!-- tutorial -->
 * Example of filtering signatures to obtain a subset based on one or more 
 * property : value condition. Example starts with a full set of signatures 
 * available in the data file and narrows them down to only leave signatures 
 * that correspond to mobile devices running Android OS that use Chrome browser.
 * Example then prints out a list of device IDs for the remaining signatures 
 * and the rank for each signature.
 * <p>
 * Example covers:
 * <ul>
 *  <li>Creating a dataset without provider
 *  <p><code>Dataset dataset = MemoryFactory.create(
 *      Shared.getLitePatternV32(), true);</code>
 *  <li>Converting an Iterable to ArrayList using:
 *  <p><code>iterableToArrayList(dataset.getSignatures())</code>
 *  <li>Using the <code>filterBy</code> method, that:
 *  <ol>
 *      <li>Performs null checks
 *      <li>Retrieves Property object based on provided property name.
 *      <li>For each signature <code>for (Signature sig : listToFilter) {</code>
 *      <br />Gets values for specified property: 
 *      <code>Values vals = sig.getValues(property);</code>
 *      <br />Checks if signature in question contains provided property values:
 *      <code>if (vals.get(propertyValue) != null)</code>, and if so, adds the 
 *      signature to the temporary list: <code>filterResults.add(sig);</code>
 *      <li>Temporary list is then returned.
 *  </ol>
 *  <li>Accessing deviceId and rank via signature object.
 * </ul>
 * <p>
 * Every invocation of the <code>filterBy()</code> function will narrow down the 
 * subset of signatures available based on the provided property name and value.
 * Eventually only a small subset will be left.
 * <p>
 * Dynamic filtering can be useful in a number of cases. For example: when 
 * creating an interdependent menu where one choice narrows down the options 
 * in a subsequent choice.
 * <!-- tutorial -->
 */
public class DynamicFilters {
    // Snippet Start
    // Dataset that holds device data information.
    protected Dataset dataset;
    // Full list of signatures.
    protected ArrayList<Signature> signatures;
    
    /**
     * Creates a new Dataset using memory factory which loads a 
     * memory-resident representation of data. Also loads a copy of 
     * signatures into an ArrayList.
     * 
     * @throws IOException if there was a problem reading from the data file.
     */
    public DynamicFilters() throws IOException {
        dataset = MemoryFactory.create(Shared.getLitePatternV32(), true);
        signatures = iterableToArrayList(dataset.getSignatures());
    }
    
    /**
     * Instantiates this class and launches the demo.
     * 
     * @param args 
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public static void main (String[] args) throws IOException {
        DynamicFilters dfe = new DynamicFilters();
        dfe.run();
    }
    
    /**
     * Repeatedly calls the filter function and prints results. The filtered 
     * subset of signatures represents mobile devices that run on Android OS 
     * and use Chrome Web browser.
     * 
     * @throws IOException if there was a problem accessing data file.
     */
    public void run() throws IOException {
        System.out.println("Total number of signatures in the data file: " + 
                signatures.size());
        
        ArrayList<Signature> subsetOfSignatures = 
                filterBy("IsMobile", "True", null);
        System.out.println("Signatures after removing non-mobile devices: " + 
                subsetOfSignatures.size());
        
        subsetOfSignatures = 
                filterBy("PlatformName", "Android", subsetOfSignatures);
        System.out.println("Signatures after remobing non Android devices: " + 
                subsetOfSignatures.size());
        
        subsetOfSignatures = 
                filterBy("BrowserName", "Chrome", subsetOfSignatures);
        System.out.println("Signatures after removing non Chrome browsers: " + 
                subsetOfSignatures.size());
        
        System.out.println("The following device IDs are for devices that are "
                + "mobile, run on Android OS and use Chrome browser:");
        for (Signature signature : subsetOfSignatures) {
            System.out.println(signature.getDeviceId() + " popularity: " + 
                    signature.getRank());
        }
    }
    
    /**
     * Filters the provided set of signatures to only return those, where the 
     * specified property is equal to the specified value. For example: calling 
     * this function with "IsMobile","True",null will return a subset of 
     * signatures where the IsMobile property evaluates to "True".
     * <p>
     * After checking for valid input method, iterates through the provided list 
     * of signatures to check if the required property of the current signature 
     * has the required value. If so, a signature is added to the temporary list 
     * that gets returned at the end.
     * <p>
     * If the signature list was not provided, a complete list of signatures 
     * available in the data file will be used.
     * 
     * @param propertyName String containing name of the property to check for, 
     * not null.
     * @param propertyValue String with value that the required property must 
     * evaluate to, not null.
     * @param listToFilter an ArrayList of signatures to perform filtering on. 
     * If null the entire set of signatures will be used.
     * @return Subset of signatures where property equals value.
     * @throws IOException if there was a problem accessing data file.
     */
    public ArrayList<Signature> filterBy(String propertyName, 
                                         String propertyValue, 
                                         ArrayList<Signature> listToFilter) 
                                                            throws IOException {
        if (propertyName.isEmpty() || propertyValue.isEmpty()) {
            throw new IllegalArgumentException("Property and Value can not be "
                    + "empty or null.");
        }
        if (listToFilter == null) {
            // Use complete list of signatures if no list was provided.
            listToFilter = signatures;
        }
        Property property = dataset.get(propertyName);
        if (property == null) {
            throw new IllegalArgumentException("Property you requested " +
                    propertyName + " does not appear to exist in the current "
                    + "data file.");
        }
        ArrayList<Signature> filterResults = new ArrayList<Signature>();
        for (Signature sig : listToFilter) {
            Values vals = sig.getValues(property);
            if (vals.get(propertyValue) != null) {
                filterResults.add(sig);
            }
        }
        return filterResults;
    }
    
    /**
     * Converts Iterable list of 51Degrees signatures to an ArrayList of 
     * signatures.
     * 
     * @param <E> 
     * @param iter Iterable to convert to ArrayList.
     * @return ArrayList of 51Degrees signatures.
     */
    public static <E> ArrayList<E> iterableToArrayList(Iterable<E> iter) {
        ArrayList<E> list = new ArrayList<E>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }
    
    /**
     * Closes the {@link fiftyone.mobile.detection.Dataset} by releasing data 
     * file readers and freeing the data file from locks. This method should 
     * only be used when the {@code Dataset} is no longer required, i.e. when 
     * device detection functionality is no longer required, or the data file 
     * needs to be freed.
     * 
     * @throws IOException if there is a problem accessing the data file.
     */
    public void close() throws IOException {
        dataset.close();
    }
    // Snippet End
}
