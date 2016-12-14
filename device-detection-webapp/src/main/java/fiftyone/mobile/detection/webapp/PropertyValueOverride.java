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
package fiftyone.mobile.detection.webapp;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Value;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

class PropertyValueOverride {
       
    /**
     * Returns the JavaScript snippet for property value override for the
     * requesting device.
     * 
     * @param request current HttpServletRequest.
     * @return JavaScript as string.
     */
    static String getJavascript(HttpServletRequest request) throws IOException {
        List<String> jsValues = getJavaScriptValues(request);
        if (jsValues.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("// Property Value Overrides - Start\r");
            for (String jsValue : jsValues) {
                sb.append(jsValue);
                sb.append("\r");
            }
            sb.append("// Property Value Overrides - End\r");
            return sb.toString();
        }
        return null;
    }
    
    /**
     * Returns the individual JavaScript lines for each of the properties that 
     * have values for the request.
     * 
     * @param request current HttpServletRequest.
     * @return JavaScript lines.
     * @throws IOException 
     */
    private static List<String> getJavaScriptValues(HttpServletRequest request) 
            throws IOException {
        List<String> javaScriptValues = new ArrayList<String>();
        Match match = WebProvider.getMatch(request);
        if (match != null) {
            Dataset ds = WebProvider.getActiveProvider(
                    request.getServletContext()).dataSet;
            for (Property property : ds.getPropertyValueOverrideProperties()) {
               for (Value value : match.getValues(property).getAll()) {
                   javaScriptValues.add(value.getName());
               }
            } 
        }
        return javaScriptValues;
    }
}
