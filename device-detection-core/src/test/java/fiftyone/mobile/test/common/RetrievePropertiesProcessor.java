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

package fiftyone.mobile.test.common;

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Values;

import java.io.IOException;
import java.util.ArrayList;

public class RetrievePropertiesProcessor extends MatchProcessor.Default {

    private final ArrayList<Property> properties = new ArrayList<Property>();
    
    public RetrievePropertiesProcessor(Iterable<Property> properties) {
        if (properties != null) {
            for (Property property : properties) {
                this.properties.add(property);
            }
        }
    }
    
    @Override
    public void process(Match match, Results result) throws IOException {
        long checkSum = 0;
        for(Property property : this.properties) {
            Values values = match.getValues(property);
            if (values != null) {
                checkSum += match.getValues(property).toString().hashCode();
            }
        }
        result.checkSum.getAndAdd(checkSum);
    }
}