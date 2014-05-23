package fiftyone.mobile.detection.webapp;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Match;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
class Feature {

    final private static Logger logger = LoggerFactory.getLogger(Feature.class);
    private static final String COOKIE_NAME = "51D_ProfileIds";
    private static final String SESSION_KEY = "51D_Feature";
    private static final String FEATURE_DONE = "51D_Feature_Done";

    void process(HttpServletRequest req, HttpSession session, Cookie[] cookies)
            throws IOException, ServletException {
        Integer id = (Integer) session.getAttribute(SESSION_KEY);
        if (id == null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    try {
                        session.setAttribute(SESSION_KEY,
                                Integer.parseInt(cookie.getValue()));
                    } catch (NumberFormatException e) {
                        logger.error("Error parsing 51D_Feature cookie", e);
                    }
                }
            }
        }
    }

    void merge(Match match, HttpSession session, Dataset dataset) throws IOException {
        boolean done = session.getAttribute(FEATURE_DONE) == null;
        Integer id = (Integer) session.getAttribute(SESSION_KEY);
        if (id != null && !done) {
            match.updateProfile(id);
            session.setAttribute(FEATURE_DONE, new Boolean(true));
        }
    }
}
