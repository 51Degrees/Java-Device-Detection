package API;

import common.Utils;
import fiftyone.mobile.detection.TrieProvider;
import fiftyone.mobile.detection.factories.TrieFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
/**
 *
 */
public class TrieBase {
    
    private TrieProvider provider;
    protected String dataFile;
    
    public TrieBase(String dataFile) {
        this.dataFile = dataFile;
    }
    
    /* Set up */
    
    @Before
    public void createDataSet() {
        Utils.checkFileExists(dataFile);
        try {
            this.provider = TrieFactory.create(dataFile, false);
        } catch (IOException ex) {
            Logger.getLogger(TrieBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /* Tests */
    
    @Test
    public void API_Trie_AllHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        for (String header : provider.getHttpHeaders()) {
            headers.put(header, common.UserAgentGenerator.getRandomUserAgent(0));
        }
        fetchAllProperties(provider.getDeviceIndexes(headers));
    }
    
    @Test
    public void API_Trie_AllHeadersNull() {
        Map<String, String> headers = new HashMap<String, String>();
        for (String header : provider.getHttpHeaders()) {
            headers.put(header, null);
        }
        fetchAllProperties(provider.getDeviceIndexes(headers));
    }
    
    @Test
    public void API_Trie_DuplicateHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        for (int i = 0; i < 5; i++) {
            for (String header : provider.getHttpHeaders()) {
                headers.put(header, common.UserAgentGenerator.getRandomUserAgent(0));
            }
        }
        fetchAllProperties(provider.getDeviceIndexes(headers));
    }
    
    @Test
    public void API_Trie_DuplicateHeadersNull() {
        Map<String, String> headers = new HashMap<String, String>();
        for (int i = 0; i < 5; i++) {
            for (String header : provider.getHttpHeaders()) {
                headers.put(header, null);
            }
        }
        fetchAllProperties(provider.getDeviceIndexes(headers));
    }
    
    @Test
    public void API_Trie_EmptyHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        fetchAllProperties(provider.getDeviceIndexes(headers));
    }
    
    @Test
    public void API_Trie_EmptyUserAgent() {
        String emptyUA = "";
        try {
            fetchAllProperties(provider.getDeviceIndex(emptyUA));
        } catch (Exception ex) {
            Logger.getLogger(TrieBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void API_Trie_LongUserAgent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(common.UserAgentGenerator.getRandomUserAgent(10));
        }
        try {
            fetchAllProperties(provider.getDeviceIndex(sb.toString()));
        } catch (Exception ex) {
            Logger.getLogger(TrieBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void API_Trie_NullHeaders() {
        Map<String, String> headers = null;
        fetchAllProperties(provider.getDeviceIndexes(headers));
    }
    
    @Test
    public void API_Trie_NullUserAgent() {
        String userAgent = null;
        try {
            fetchAllProperties(provider.getDeviceIndex(userAgent));
        } catch (Exception ex) {
            Logger.getLogger(TrieBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /* Clean up */
    
    @After
    public void dispose() {
        dispose(true);
        System.gc();
    }
    
    private void dispose(boolean disposing) {
        if (this.provider != null) {
            provider.dispose();
        }
    }
    
    /* Support methods */
    
    private void fetchAllProperties(Map<String, Integer> deviceIndexes) {
        int checkSum = 0;
        for (String propertyName : provider.propertyNames()) {
            String value = provider.getPropertyValue(deviceIndexes, propertyName);
            System.out.print(propertyName+": ");
            if (value != null) {
                System.out.println(value);
                checkSum += value.hashCode();
            }
        }
        System.out.println("Check sum: "+checkSum);
    }
    
    private void fetchAllProperties(int deviceIndex) {
        int checkSum = 0;
        for (String propertyName : provider.propertyNames()) {
            String value = provider.getPropertyValue(deviceIndex, propertyName);
            System.out.print(propertyName+": ");
            if (value != null) {
                System.out.println(value);
                checkSum += value.hashCode();
            }
        }
        System.out.println("Check sum: "+checkSum);
    }
}
