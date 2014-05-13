package fiftyone.mobile.detection.webapp;

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Property.PropertyValueType;
import fiftyone.mobile.detection.entities.Value;
import fiftyone.mobile.detection.entities.Values;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
class JavascriptProvider {

    private static String stringJoin(String seperator, List<String> strings) {
        String value;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            stringBuilder.append(strings.get(i));
            if (i < strings.size() - 1) {
                stringBuilder.append(seperator);
            }
        }
        value = stringBuilder.toString();
        return value;
    }
    private ServletContext servletContext;
    private static final int DEFAULT_BUFFER_SIZE = 10240;

    public JavascriptProvider(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void provide(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        WebProvider provider = ((FiftyOneDegreesListener) servletContext
                .getAttribute(Constants.WEB_PROVIDER_KEY)).getProvider();

        StringBuilder javascript = new StringBuilder();

        Map<String, String[]> results = provider.getResults(request);
        String fODPO[] = results.get("JavascriptHardwareProfile");
        if (fODPO != null) {
            javascript
                    .append("function FODPO() {{ var profileIds = new Array(); ");
            javascript.append(fODPO[0]);
            javascript
                    .append(" document.cookie = \"51D_ProfileIds=\" + profileIds.join(\"|\"); }}");
            javascript.append("\r");
        }
        String fODBW[] = results.get("JavascriptBandwidth");
        String fODIO[] = results.get("JavascriptImageOptimiser");
        if (fODBW != null) {
            javascript.append(fODBW[0]);
            javascript.append("\r");
        }
        if (fODIO != null) {
            javascript.append(fODIO[0]);
        }

        response.reset();

        response.setContentType("application/x-javascript");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Vary", "User-Agent");
        response.setHeader("Cache-Control", "public");
        response.setHeader("Expires", provider.getNextUpdate().toString());
        response.setHeader("Last-Modified", provider.getPublished().toString());
        try {
            response.setHeader("ETag", eTagHash(provider, request));
        } catch (Exception ex) {
            // Nothing to do.
        }
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader("Content-Length",
                Integer.toString(javascript.length()));

        response.getOutputStream().println(javascript.toString());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    public void provideFeatures(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebProvider provider = ((FiftyOneDegreesListener) servletContext
                .getAttribute(Constants.WEB_PROVIDER_KEY)).getProvider();

        Set<String> requestedProperties = null;
        String query = request.getQueryString();
        if (query != null) {
            requestedProperties = new HashSet<String>(Arrays.asList(query.split("&")));
        }
        final Match match = provider.getResult(request);

        final List<String> pairs = new ArrayList<String>();
        for (Property property : provider.dataSet.properties) {
            if (property.valueType != PropertyValueType.JAVASCRIPT) {
                if (requestedProperties == null
                        || (requestedProperties != null && requestedProperties.contains(property.getName()))) {
                    Values values = match.getValues(property);
                    pairs.add(getJavascriptPair(property, values));
                }
            }
        }

        final String output = String.format("var FODF = {%s};", stringJoin(", ", pairs));

        response.reset();

        response.setContentType("application/x-javascript");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Vary", "User-Agent");
        response.setHeader("Cache-Control", "public");
        response.setHeader("Expires", provider.getNextUpdate().toString());
        response.setHeader("Last-Modified", provider.getPublished().toString());
        try {
            response.setHeader("ETag", eTagHash(provider, request));
        } catch (Exception ex) {
            // Nothing to do.
        }
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader("Content-Length",
                Integer.toString(output.length()));

        response.getOutputStream().println(output);
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    /// <summary>
    /// Adds the value for the property provided to the list of features.
    /// </summary>
    /// <param name="match"></param>
    /// <param name="features"></param>
    /// <param name="property"></param>
    private static String getJavascriptPair(Property property, Values values) throws IOException {
        List<String> valueStrs = new ArrayList<String>();
        for (Value value : values) {
            String valueStr;
            switch (property.valueType) {
                case BOOL:
                    valueStr = Boolean.toString(value.toBool());
                    break;
                case INT:
                case DOUBLE:
                    valueStr = Double.toString(value.toDouble());
                    break;
                default:
                    valueStr = String.format("\"%s\"", value.getName());
                    break;
            }
            valueStrs.add(valueStr);
        }
        String value;
        if (valueStrs.size() > 1) {
            value = String.format("[%s]", stringJoin(", ", valueStrs));
        } else {
            value = valueStrs.get(0);
        }
        String pair = String.format("%s:%s", property.getName(), value);
        return pair;
    }

    @SuppressWarnings("deprecation")
    private String eTagHash(WebProvider provider, HttpServletRequest request) {
        Date published = provider.getPublished();
        Integer year = published.getYear();
        Integer month = published.getMonth();
        Integer day = published.getDay();
        String userAgent = request.getHeader("User-Agent");
        String queryString = request.getQueryString();

        return Integer.toHexString(year.hashCode() + month.hashCode()
                + day.hashCode() + userAgent.hashCode()
                + queryString == null ? 0 : queryString.hashCode());
    }
}
