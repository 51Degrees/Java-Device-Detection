package fiftyone.mobile.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Values;

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
public class Process implements Runnable {

    class Line {

        String value;
        int id;

        Line(int id, String value) {
            this.id = id;
            value = value.replaceAll("^\"", "");
            value = value.replaceAll("\"$", "");
            this.value = value;
        }
    }
    public int count;
    private final int limit;
    private final BufferedReader useragents;
    private final BufferedWriter writer;
    private final Provider provider;
    public final long start = System.currentTimeMillis();

    public Process(BufferedReader useragents, BufferedWriter writer, Provider provider, int limit) {
        this.useragents = useragents;
        this.provider = provider;
        this.limit = limit;
        this.writer = writer;
    }

    private synchronized Line read() {
        try {
            if (count < limit) {
                String line = useragents.readLine();
                if (line != null) {
                    count++;
                    Line record = new Line(count, line);
                    if (count % 100000 == 0) {
                        System.out.println(String.format("Read '%d' UserAgents", count));
                    }
                    return record;
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void run() {
        try {
            Line line = read();
            Match match = null;
            try {
                match = provider.createMatch();
            } catch (Exception ex) {
            }
            while (line != null) {
                try {
                    provider.match(line.value, match);
                } catch (Exception e) {
                }
                if (match != null) {
                    synchronized (writer) {
                        writer.write(line.value + "\t"
                                + getValue(match, "PlatformName") + "\t"
                                + getValue(match, "PlatformVersion") + "\t"
                                + getValue(match, "IsMobile") + "\t"
                                + getValue(match, "IsTablet") + "\t"
                                + getValue(match, "IsSmartPhone") + "\t"
                                + getValue(match, "IsSmallScreen") + "\t"
                                + getValue(match, "IsEmailBrowser") + "\t"
                                + getValue(match, "IsCrawler") + "\t"
                                + getValue(match, "BrowserName") + "\t"
                                + getValue(match, "BrowserVersion") + "\t"
                                + getValue(match, "ScreenMMHeight") + "\t"
                                + getValue(match, "ScreenMMWidth") + "\t"
                                + getValue(match, "HasTouchScreen") + "\t"
                                + getValue(match, "HasVirtualQwerty") + "\t"
                                + getValue(match, "HasQwertyPad") + "\r\n");
                    }
                }
                line = read();
            }
        } catch (IOException e) {
            return;
        }
    }

    private String getValue(Match match, String propertyName) {
        try {
            if (match != null) {
                Values value = match.getValues(propertyName);
                if (value != null) {
                    return value.toString();
                }
            }
        } catch (IOException e) {
            // Do nothing.
        }
        return "";
    }
}
