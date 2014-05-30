package fiftyone.mobile.detection.webapp;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
class Bandwidth {

    final private static Logger logger = LoggerFactory
            .getLogger(Bandwidth.class);
    
    private static final String COOKIE_NAME = "51D_Bandwidth";
    private static final String SESSION_KEY = "51D_Stats";
    private static final String COOKIE_DELIM = "|";

    static void process(HttpServletRequest req, HttpServletResponse resp,
            HttpSession session, Cookie[] cookies) {
        Stats stats = (Stats) session.getAttribute(SESSION_KEY);
        if (stats == null) {
            stats = new Stats();
        }
        long browserTimeSent = 0;
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                @SuppressWarnings("deprecation")
                StringTokenizer st = new StringTokenizer(
                        URLDecoder.decode(cookie.getValue()), COOKIE_DELIM,
                        false);
                try {
                    long id = Long.parseLong(st.nextToken());
                    long browserTimeRecieved = Long.parseLong(st.nextToken());
                    browserTimeSent = Long.parseLong(st.nextToken());
                    long browserTimeCompleted = Long.parseLong(st.nextToken());
                    int responseLength = Integer.parseInt(st.nextToken());

                    try {
                        Stat stat = stats.fetch(id);
                        if (stat == null) {
                            stats.clear();
                        } else if (!stat.isComplete()) {
                            stat.browserTimeRecieved = browserTimeRecieved;
                            stat.browserTimeCompleted = browserTimeCompleted;
                            stat.responseLength = responseLength;
                        }

                        stats.removeOld();

                        req.setAttribute("51D_LastResponseTime",
                                stats.getLastResponseTime());
                        req.setAttribute("51D_LastCompletionTime",
                                stats.getLastCompletionTime());
                        req.setAttribute("51D_AverageResponseTime",
                                stats.getAverageResponseTime());
                        req.setAttribute("51D_AverageCompletionTime",
                                stats.getAverageCompletionTime());
                        req.setAttribute("51D_AverageBandwidth",
                                stats.getAverageBandwidth());
                        break;

                    } catch (NumberFormatException e) {
                        logger.error("Error parsing 51D_Bandwidth cookie", e);
                    }
                } catch (NoSuchElementException e) {
                    // do nothing
                    System.out.println(e.toString());
                }
            }
        }
        Stat newStat = new Stat();
        newStat.serverTimeRecieved = System.currentTimeMillis();
        newStat.requestLength = req.getContentLength();
        newStat.browserTimeSent = browserTimeSent;

        stats.add(newStat);

        Cookie newCookie = new Cookie(COOKIE_NAME,
                Long.toString(newStat.id));
        newCookie.setPath("/");
        resp.addCookie(newCookie);
        stats.lastId = newStat.id;
        newStat.serverTimeSent = System.currentTimeMillis();
        session.setAttribute(SESSION_KEY, stats);
    }
    
    
    /**
     * Returns the bandwidth monitoring JavaScript for the current request.
     * @param request
     * @return
     * @throws IOException 
     */
    static String getJavascript(HttpServletRequest request) throws IOException {
        Map<String, String[]> results = WebProvider.getResult(request);
        if (results != null) {
            String[] values = results.get("JavascriptBandwidth");
            if (values != null &&
                values.length == 1) {
                return values[0];
            }
        }
        return null;
    }    
}
