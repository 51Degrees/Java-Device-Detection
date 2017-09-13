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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intercepts HTTP requests of the parent Servlet to check the request path for 
 * 51Degrees functionality such as image optimiser, core and feature JavaScript 
 * functionality.
 * <p>
 * The functionality of this class will only be available when extending the 
 * BaseServlet as:
 * <code>public class MyServlet extends BaseServlet</code>
 */
@SuppressWarnings("serial")
public final class FiftyOneDegreesServlet extends HttpServlet {

    private final static Logger logger = LoggerFactory
            .getLogger(FiftyOneDegreesServlet.class);
    private static final String JAVASCRIPT_CORE = "/core.js";
    private static final String JAVASCRIPT_FEATURES = "/features.js";

    /**
     * Intercepts HTTP request to extend response with 51Degrees functionality 
     * like the image resizing, core and feature JavaScript.
     * 
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException 
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {

        // Check the path information to find the resource being requested.
        String pathInfo = request.getPathInfo();
        if (JAVASCRIPT_CORE.equals(pathInfo)) {
            JavascriptProvider.sendCoreJavaScript(request, response);
        } else if (JAVASCRIPT_FEATURES.equals(pathInfo)) {
            JavascriptProvider.sendFeatureJavaScript(request, response);
        } else if (
                pathInfo.toLowerCase().endsWith("jpg") ||
                pathInfo.toLowerCase().endsWith("png") ||
                pathInfo.toLowerCase().endsWith("gif")) {
            ImageOptimizer.sendImage(request, response);
        }
    }
}