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

import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Signature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class FindProfilesTest {

    static FindProfiles fp;

    /**
     * Runs the FindProfiles example, checks it runs and produces the correct
     * output.
     * @throws IOException
     */
    @Test
    public void FindProfilesExample() throws IOException {
        fp = new FindProfiles();
        List<Profile> profiles = fp.provider.dataSet.findProfiles("IsMobile", "True", null);
        for (Profile profile : profiles) {
            assertEquals(profile.getValues("IsMobile").toString(), "True");
        }
        profiles = fp.provider.dataSet.findProfiles("IsMobile", "False", null);
        for (Profile profile : profiles) {
            assertEquals(profile.getValues("IsMobile").toString(), "False");
        }
    }
}
