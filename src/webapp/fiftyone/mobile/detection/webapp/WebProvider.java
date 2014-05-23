package fiftyone.mobile.detection.webapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Disposable;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;

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
public class WebProvider extends Provider implements Disposable {

    final Logger logger = LoggerFactory.getLogger(WebProvider.class);
    private static final String USER_AGENT = "user-agent";
    /**
     * Used to store the result for the current request in the
     * HttpServletRequest's attribute collection.
     */
    private static final String RESULT_ATTIBUTE = "51D_RESULT";
    private static final String SESSION_RESULT = "SESSION_RESULT";
    private final ShareUsage usage;
    private final Feature feature;

    public WebProvider() throws IOException {
        super(Constants.CACHE_SERVICE_INTERVAL);
        usage = new ShareUsage(Constants.URL, NewDeviceDetails.MAXIMUM);
        Thread usageThread = new Thread(usage, "51Degrees usage thread");
        usageThread.setDaemon(true);
        usageThread.setPriority(Thread.MIN_PRIORITY);

        feature = new Feature();

        // don't start threads in constructors, but in this case we want to
        // match the extended API
        usageThread.start();
    }

    public WebProvider(Dataset dataSet) {
        super(dataSet, Constants.CACHE_SERVICE_INTERVAL);
        usage = new ShareUsage(Constants.URL, NewDeviceDetails.MAXIMUM);
        Thread usageThread = new Thread(usage, "51Degrees usage thread");
        usageThread.setDaemon(true);
        usageThread.setPriority(Thread.MIN_PRIORITY);

        feature = new Feature();

        // don't start threads in constructors, but in this case we want to
        // match the extended API
        usageThread.start();
    }

    @Override
    public void dispose() {
        // let thread run to completion and then die
        usage.destroy();
    }

    public void record(HttpServletRequest request) {
        String shareUsage = request.getServletContext().
                getInitParameter(Constants.SHARE_USAGE);
        if (shareUsage == null
                || Boolean.parseBoolean(shareUsage)) {
            try {
                usage.recordNewDevice(request);
            } catch (Exception e) {
                logger.error("Could not record request", e);
            }
        }
    }

    public Match getResult(final HttpServletRequest request)
            throws ServletException, IOException {
        record(request);
        Object previousResult = request.getAttribute(RESULT_ATTIBUTE);
        if (previousResult instanceof Match == false) {
            try {
                Match result = match(parseRequest(request));
                mergeMatchWithSessionData(result, request.getSession());
                request.setAttribute(RESULT_ATTIBUTE, result);
                previousResult = result;
                request.getSession().setAttribute(SESSION_RESULT,
                        result.getResults());
            } catch (UnsupportedEncodingException e) {
                logger.error("Issues reading header info:", e);
            }
        }
        return (Match) previousResult;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String[]> getResults(HttpServletRequest request)
            throws IOException {
        Map<String, String[]> result = (Map<String, String[]>) request.getSession()
                .getAttribute(SESSION_RESULT);
        //TODO rationalise with above
        if (result == null) {
            Match match = match(parseRequest(request));
            mergeMatchWithSessionData(match, request.getSession());
            request.setAttribute(RESULT_ATTIBUTE, result);
            result = match.getResults();
            request.getSession().setAttribute(SESSION_RESULT,
                    match.getResults());
        }

        return result;
    }

    public Map<String, String> parseRequest(HttpServletRequest request) {
        final Enumeration<String> headerNames = request.getHeaderNames();
        final Map<String, String> headers = new HashMap<String, String>();
        while (headerNames.hasMoreElements()) {
            final String n = (String) headerNames.nextElement().toString()
                    .toLowerCase();
            headers.put(n, request.getHeader(n));
        }
        return headers;
    }

    @Override
    protected String getUserAgentString() {
        return USER_AGENT;
    }

    private void mergeMatchWithSessionData(Match result, HttpSession session) throws IOException {
        feature.merge(result, session, dataSet);
    }

    public Date getPublished() {
        return dataSet.published;
    }

    public Date getNextUpdate() {
        return dataSet.nextUpdate;
    }
}
