package HttpHeaders;

import java.io.IOException;

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

public class Combinations extends Base{

    public Combinations(String dataFile) {
        super(dataFile);
    }
    
    protected void OperaMini_Samsung() throws IOException
    {
        Validation validation = new Validation(super.dataSet);
        validation.put("BrowserName", "Opera(.*)");
        validation.put("HardwareVendor", "Samsung");
        super.process(
            "(.*)Opera Mini(.*)",
            "(.*)SAMSUNG GT-I(.*)", 
            validation);
    }

    protected void OperaMini_iPhone() throws IOException
    {
        Validation validation = new Validation(super.dataSet);
        validation.put("BrowserName", "Opera(.*)");
        validation.put("HardwareVendor", "Apple");
        super.process(
            "(.*)Opera Mini(.*)",
            "^Mozilla/5\\.0 \\(iPhone; CPU iPhone OS (.*)",
            validation);
    }

    protected void OperaMini_HTC() throws IOException
    {
        Validation validation = new Validation(super.dataSet);
        validation.put("BrowserName", "Opera(.*)");
        validation.put("HardwareVendor", "HTC");
        super.process(
            "(.*)Opera Mini(.*)",
            "(.*) HTC (.*)",
            validation);
    }    
}
