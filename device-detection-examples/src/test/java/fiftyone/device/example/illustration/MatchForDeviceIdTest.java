/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.entities.Profile;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class MatchForDeviceIdTest {
    
    static MatchForDeviceId mfdi;
    
    @BeforeClass
    public static void setUp() throws IOException {
        mfdi = new MatchForDeviceId();
    }
    
    @AfterClass
    public static void tearDown() throws IOException {
        mfdi.close();
    }
    
    @Test
    public void matchFromDeviceId() throws IOException {
        // Obtain device Id as string, byte array and list from a User-Agent.
        Match original = mfdi.matchForUserAgent(mfdi.mobileUserAgent);
        String deviceIdString = original.getDeviceId();
        byte[] deviceIdArray = original.getDeviceIdAsByteArray();
        ArrayList<Integer> deviceIdList = new ArrayList<Integer>();
        for (Profile profile : original.getProfiles()) {
            deviceIdList.add(profile.profileId);
        }
        boolean isMobile = original.getValues("IsMobile").toBool();
        // For each Id obtain match.
        Match matchFromString = mfdi.matchForDeviceIdString(deviceIdString);
        Match matchFromArray = mfdi.matchForDeviceIdArray(deviceIdArray);
        Match matchFromList = mfdi.matchForDeviceIdList(deviceIdList);
        // Perform tests.
        assertTrue("Match objects are different: ", 
                (System.identityHashCode(matchFromString) != 
                    System.identityHashCode(matchFromArray)) &&
                (System.identityHashCode(matchFromString) != 
                    System.identityHashCode(matchFromList)) &&
                (System.identityHashCode(matchFromString) != 
                    System.identityHashCode(matchFromList)));
        assertTrue("Device IDs are the same: ",
                (matchFromString.getDeviceId().equals(
                        matchFromArray.getDeviceId())) && 
                (matchFromString.getDeviceId().equals(
                        matchFromList.getDeviceId())));
        assertTrue("Device IsMobile values are the same: ",
                (matchFromString.getValues("IsMobile").toBool() == isMobile) && 
                (matchFromArray.getValues("IsMobile").toBool() == isMobile) &&
                (matchFromList.getValues("IsMobile").toBool() == isMobile));
    }
    
}
