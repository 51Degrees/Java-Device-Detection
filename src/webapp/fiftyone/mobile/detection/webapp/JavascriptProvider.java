package fiftyone.mobile.detection.webapp;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Property.PropertyValueType;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;

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

    private static final int DEFAULT_BUFFER_SIZE = 10240;

    static void sendJavaScript(
            HttpServletRequest request, HttpServletResponse response, 
            Dataset dataSet, StringBuilder javascript) throws IOException {
        
        response.reset();
        response.setContentType("application/x-javascript");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Vary", "User-Agent");
        response.setHeader("Cache-Control", "public");
        response.setHeader("Expires", dataSet.nextUpdate.toString());
        response.setHeader("Last-Modified", dataSet.published.toString());
        try {
            response.setHeader("ETag", eTagHash(dataSet, request));
        } catch (Exception ex) {
            // The response doesn't support eTags. Nothing we can do.
        }
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader("Content-Length",
                Integer.toString(javascript.length()));

        response.getOutputStream().println(javascript.toString());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }
    
    static void sendCoreJavaScript(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        StringBuilder javascript = new StringBuilder(
                "// Copyright 51 Degrees Mobile Experts Limited\r\n");

        
        String fodIo = ImageOptimizer.getJavascript(request);
        if(fodIo != null) {
            javascript.append(fodIo);
        }
        String fodBw = Bandwidth.getJavascript(request);
        if(fodBw != null) {
            javascript.append(fodBw);
        }
        String fodPo = ProfileOverride.getJavascript(request);
        if(fodPo != null) {
            javascript.append(fodPo);
        }
        
        sendJavaScript(
                request, 
                response, 
                WebProvider.getActiveProvider(
                    request.getServletContext()).dataSet,
                javascript);
    }

    /**
     * Responds with the JavaScript listing the featured properties and values.
     * @param request
     * @param response
     * @throws IOException 
     */
    static void sendFeatureJavaScript(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        StringBuilder javascript = new StringBuilder(
                "// Copyright 51 Degrees Mobile Experts Limited\r\n");
        Dataset dataSet = WebProvider.getActiveProvider(request.getServletContext()).dataSet;
        final Map<String, String[]> results = WebProvider.getResult(request);
        List<String> features = new ArrayList<String>();
        
        String query = request.getQueryString();
        if (query == null) {
            for(Property property : dataSet.properties) {
                if (property.valueType != PropertyValueType.JAVASCRIPT) {
                    getFeatureJavaScript(results, features, property);
                }
            }
        }
        else {
            Set<String> requestedProperties = 
                    new HashSet<String>(Arrays.asList(query.split("&")));
            for(Property property : dataSet.properties) {
                if (property.valueType != PropertyValueType.JAVASCRIPT) {
                    for(String name : requestedProperties) {
                        if (name.equalsIgnoreCase(property.getName())) {
                            getFeatureJavaScript(results, features, property);
                        }
                    }
                }
            }
        }
                
        javascript.append(String.format("var FODF={%s};", 
            stringJoin(",\r\n", features)));
        
        sendJavaScript(
            request, 
            response, 
            WebProvider.getActiveProvider(
                request.getServletContext()).dataSet,
            javascript);        
    }

    private static void getFeatureJavaScript(
            Map<String, String[]> results, List<String> features, Property property) throws IOException {
        String[] values = results.get(property.getName());
        if (values != null && values.length > 0) {
            String propName = property.getName().replace("/", "").replace("-", "");
            switch (property.valueType) {
                case BOOL:
                    try {
                        features.add(String.format(
                                "%s:%s",
                                propName,
                                Boolean.parseBoolean(values[0]) ? "true" : "false"));
                    } catch (NumberFormatException ex) {
                        // Ignore the property as there isn't a value that
                        // converts to a boolean.
                    }
                    break;
                case INT:
                    try {
                        features.add(String.format(
                                "%s:%i",
                                propName,
                                Double.parseDouble(values[0])));
                    } catch (NumberFormatException ex) {
                        // Ignore the property as there isn't a value that
                        // converts to a boolean.
                    }                 
                case DOUBLE:
                    try {
                        features.add(String.format(
                                "%s:%s",
                                propName,
                                Double.parseDouble(values[0])));
                    } catch (NumberFormatException ex) {
                        // Ignore the property as there isn't a value that
                        // converts to a boolean.
                    }
                    break;
                default:
                    features.add(String.format(
                            "%s:\"%s\"",
                            propName,
                            stringJoin(fiftyone.properties.DetectionConstants.VALUE_SEPARATOR, values)));
                    break;
            }
        }
    }
    
    /**
     * Returns a base 64 encoded version of the hash for the core JavaScript
     * being returned.
     * @param dataSet providing the JavaScript properties.
     * @param request
     * @return 
     */
    private static String eTagHash(Dataset dataSet, HttpServletRequest request) 
            throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeLong(dataSet.published.getTime());
        dos.writeChars(request.getHeader("User-Agent"));
        dos.writeChars(request.getQueryString());
        return Base64.encodeBase64String(
                MessageDigest.getInstance("MD5").digest(bos.toByteArray()));
    }
    
    /**
     * Joins the array of strings separated by the separator provided.
     * @param seperator
     * @param values
     * @return 
     */
    private static String stringJoin(String seperator, String[] values) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            stringBuilder.append(values[i]);
            if (i < values.length - 1) {
                stringBuilder.append(seperator);
            }
        }
        return stringBuilder.toString();
    }
    
    /**
     * Joins the list of strings separated by the separator provided.
     * @param seperator
     * @param values
     * @return 
     */
    private static String stringJoin(String seperator, List<String> values) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            stringBuilder.append(values.get(i));
            if (i < values.size() - 1) {
                stringBuilder.append(seperator);
            }
        }
        return stringBuilder.toString();
    }
}
