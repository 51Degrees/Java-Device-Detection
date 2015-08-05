package Memory.Lite;

import Memory.ArrayBase;
import common.UserAgentGenerator;
import java.io.IOException;
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

public class V31Array extends ArrayBase {

    public V31Array() {
        super("../../data/51Degrees-LiteV3.1.dat");
    }

    @Test
    public void LiteV31Array_Memory_UniqueUserAgentsMulti() throws IOException {
        super.userAgentsMulti(UserAgentGenerator.getUniqueUserAgents(), 60);
    }

    @Test
    public void LiteV31Array_Memory_UniqueUserAgentsSingle() throws IOException {
        super.userAgentsSingle(UserAgentGenerator.getUniqueUserAgents(), 60);
    }

    @Test
    public void LiteV31Array_Memory_RandomUserAgentsMulti() throws IOException {
        super.userAgentsMulti(UserAgentGenerator.getRandomUserAgents(), 60);
    }

    @Test
    public void LiteV31Array_Memory_RandomUserAgentsSingle() throws IOException {
        super.userAgentsSingle(UserAgentGenerator.getRandomUserAgents(), 60);
    }

    @Test
    public void LiteV31Array_Memory_BadUserAgentsMulti() throws IOException {
        super.userAgentsMulti(UserAgentGenerator.getBadUserAgents(), 80);
    }

    @Test
    public void LiteV31Array_Memory_BadUserAgentsSingle() throws IOException {
        super.userAgentsSingle(UserAgentGenerator.getBadUserAgents(), 80);
    }
}
