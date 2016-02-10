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
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.factories.StreamFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class FindProfiles implements Closeable {
    // Snippet Start
    // Device detection provider which takes User-Agents and returns matches.
    protected final Provider provider;

    /**
     * Initialises the device detection Provider with the included Lite data
     * file. For more data see:
     * <a href="https://51degrees.com/compare-data-options">compare data options
     * </a>
     *
     * @throws IOException can be thrown if there is a problem reading from the
     * provided data file.
     */
    public FindProfiles() throws IOException {
        provider = new Provider(StreamFactory.create(Shared.getLitePatternV32(), false));
    }

    /**
     * Main entry point for this example. For the value "IsMobile is
     * True and False:
     * <ol>
     * <li>invokes
     * {@code Provider.DataSet.FindProfiles(java.lang.String, java.lang.String, List<Profile>)}.
     * Profiles are then stored in the {@link List<Profile>} list and can be
     * accessed using the {@code profiles.get(int index)} method.
     * <li>prints results.
     * </ol>
     *
     * @param args command line arguments, not used.
     * @throws java.io.IOException if there is a problem accessing the data file
     * that will be used for device detection.
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Starting FindProfiles example.");
        FindProfiles fp = new FindProfiles();
        try {
            // Retrieve all mobile profiles from the data set.
            List<Profile> profiles = fp.provider.dataSet.findProfiles("IsMobile", "True", null);
            System.out.println("There are "
                    + profiles.size()
                    + " mobile profiles in the "
                    + fp.provider.dataSet.getName()
                    + " data set.");
            // Find how many have a screen width of 1080 pixels.
            profiles = fp.provider.dataSet.findProfiles("ScreenPixelsWidth", "1080",
                    profiles);
            System.out.println(profiles.size()
            + " of them have a screen width of 1080 pixels.");

            // Retrieve all non-mobile profiles from the data set.
            profiles = fp.provider.dataSet.findProfiles("IsMobile", "False", null);
            System.out.println("There are "
                    + profiles.size()
                    + " mobile profiles in the "
                    + fp.provider.dataSet.getName()
                    + " data set.");
            // Find how many have a screen width of 1080 pixels.
            profiles = fp.provider.dataSet.findProfiles("ScreenPixelsWidth", "1080",
                    profiles);
            System.out.println(profiles.size()
                    + " of them have a screen width of 1080 pixels.");
        } finally {
            fp.close();
        }
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
    @Override
    public void close() throws IOException {
        provider.dataSet.close();
    }
    // Snippet End
}
