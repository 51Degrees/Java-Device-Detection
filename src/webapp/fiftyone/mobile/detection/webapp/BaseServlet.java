package fiftyone.mobile.detection.webapp;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 * Adds functionality to a base HttpServlet to provide properties associated
 * with the requesting device. The Listener must be included in the web
 * configuration.
 */
@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {

    /**
     * Returns the result set associated with the request provided. If this is
     * the first time the method is called the result will be stored in the
     * HttpServletRequest's attribute collection so that it does not need to be
     * fetched again in the future.
     *
     * @param request the request property results are for.
     * @return a set of results containing access to properties.
     * @throws IOException
     */
    protected Map<String, String[]> getResult(final HttpServletRequest request) 
            throws IOException {
        return WebProvider.getResult(request);
    }
    
    /**
     * 
     * @param request
     * @return the active provider for the system.
     */
    protected WebProvider getProvider(final HttpServletRequest request) {
        return WebProvider.getActiveProvider(request.getServletContext());
    }

    /**
     * Returns the value associated with the device property requested.
     *
     * @param request the request property results are for.
     * @param propertyName device property name required.
     * @return
     * @throws IOException
     */
    protected String getProperty(
            final HttpServletRequest request,
            final String propertyName) throws ServletException, IOException {
        final Map<String, String[]> result = getResult(request);
        if (result != null) {
            String[] values = result.get(propertyName);
            if (values != null) {
                return join(values);
            }
        }
        return null;
    }

    /**
     * Joins a list of strings into a comma separated string.
     *
     * @param values list of values to join
     * @return single string comma separated
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