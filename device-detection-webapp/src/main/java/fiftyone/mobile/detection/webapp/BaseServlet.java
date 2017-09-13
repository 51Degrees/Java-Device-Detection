/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.entities.Values;
import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * Adds functionality to a base HttpServlet to provide properties associated
 * with the requesting device. The Listener must be included in the web
 * configuration.
 * <p>
 * If you wish to use the 51Degrees servlet all you need to do is extend this 
 * class in your servlet implementation:
 * <code>public class MyServlet extends BaseServlet</code>
 * <p>
 * Extending this class enables your servlet to access device detection 
 * functionality.
 */
@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {

    /**
     * Returns the match associated with the request provided.
     *
     * @param request details of the HTTP request
     * @return a match instance to access properties associated with the client
     * device.
     * @throws IOException if there was a problem accessing data file.
     */
    protected Match getMatch(final HttpServletRequest request) 
            throws IOException {
        return WebProvider.getMatch(request);
    }
    
    /**
     * Returns the result set associated with the request provided.
     * 
     * The method is deprecated in favour of getMatch which avoids creating a
     * Map for all properties and values.
     *
     * @param request details of the HTTP request 
     * @return a set of results containing access to properties.
     * @throws IOException if there was a problem accessing data file.
     */
    @Deprecated
    protected Map<String, String[]> getResult(final HttpServletRequest request) 
            throws IOException {
        return WebProvider.getResult(request);
    }
    
    /**
     * @param request current HttpServletRequest.
     * @return the active provider for the system.
     */
    protected WebProvider getProvider(final HttpServletRequest request) {
        return WebProvider.getActiveProvider(request.getServletContext());
    }

    /**
     * Returns the value associated with the device property requested.
     *
     * @param request the request property results are for.
     * @param propertyName name of the property required expressed as a string.
     * @return value of the requested property for current request as a string, 
     *         or null.
     * @throws javax.servlet.ServletException 
     * @throws IOException if there was a problem accessing data file.
     */
    protected String getProperty(final HttpServletRequest request,
                                 final String propertyName) 
                                 throws ServletException, IOException {
        final Match match = getMatch(request);
        if (match != null) {
            Values values = match.getValues(propertyName);
            if (values != null) {
                return values.toString();
            }
        }
        return null;
    }

    /**
     * Joins a list of strings into a comma separated string.
     *
     * @param values list of values to join.
     * @return single string comma separated.
     */
    protected String join(final String[] values) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]);
            if (i + 1 < values.length) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}