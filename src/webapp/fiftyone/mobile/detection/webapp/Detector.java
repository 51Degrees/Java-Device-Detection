package fiftyone.mobile.detection.webapp;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
class Detector {

    /**
     * The factory class used to access the current provider used across all
     * servlets.
     */
    private FiftyOneDegreesListener factory = null;
    /**
     * Creates a logger for this class
     */
    final Logger logger = LoggerFactory.getLogger(Detector.class);

    /**
     * Gets the factory being used by this servlet context.
     *
     * @param sc
     * @throws ServletException
     */
    public void init(final ServletConfig sc) throws ServletException {
        factory = (FiftyOneDegreesListener) sc.getServletContext().getAttribute(
                Constants.WEB_PROVIDER_KEY);
        if (factory != null) {
            logger.info("51Degrees.mobi Servlet Initialised");
        } else {
            throw new ServletException(
                    "51Degrees.mobi listener is not available. "
                    + "Check the class fiftyone.mobile.detection.webapp.Listener "
                    + "is registered in the web.xml file.");
        }
    }
}
