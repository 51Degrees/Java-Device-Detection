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
import fiftyone.mobile.detection.TrieProvider;
import fiftyone.mobile.detection.factories.TrieFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * <!-- snippet -->
 * Example of creating a TrieProvider and using it to get a device property.
 * <p>
 * {@link #main} assumes it is being run with a working directory at root of 
 * project or of this module.
 * <!-- snippet -->
 */
public class TrieExample implements Closeable {

    // Trie provider object, created using the Trie factory.
    private final TrieProvider provider;

    public TrieExample() throws IOException {
        //Initialise provider through TrieFactory object.
        provider = TrieFactory.create(Shared.getLiteTrieV32());
    }

    /**
     * Illustrates the basic process of detecting a device and retrieving a 
     * property for Trie provider.
     * 
     * @param userAgent an HTTP User-Agent header value
     * @return ture if the device has been detected as mobile
     * @throws Exception
     */
    public boolean isItMobile(String userAgent) throws Exception {

        //Get device index based on HTTP User-Agent header value
        int index = provider.getDeviceIndex(userAgent);

        // get the value of the IsMobile property for this device index
        String result = provider.getPropertyValue(index, "IsMobile");

        // all properties return Strings
        return Boolean.valueOf(result);
    }

    @Override
    public void close() throws IOException {
        provider.close();
    }

    public static void main(String[] args) throws Exception {
        // set up
        TrieExample trieExample = new TrieExample();
        try {
            // loop over provided small sample of HTTP User-Agent header values
            for (String ua: Shared.USERAGENTS) {
                if (trieExample.isItMobile(ua)) {
                    System.out.println("It's mobile: " + ua);
                } else {
                    System.out.println("It isn't mobile: " + ua);
                }
            }
        } finally {
            trieExample.close();
        }
    }
}
