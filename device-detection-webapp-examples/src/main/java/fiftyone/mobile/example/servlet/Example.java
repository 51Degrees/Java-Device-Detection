/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2015 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
package fiftyone.mobile.example.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Property.PropertyValueType;
import fiftyone.mobile.detection.webapp.BaseServlet;
import java.util.ArrayList;

/**
 * <!-- tutorial -->
 * Returns a page with predefined properties, match metrics information and 
 * a list of the relevant HTTP headers used for the detection.
 * <p>
 * Device detection functionality becomes available upon extending the 
 * 51Degrees BaseServlet from the WebApp module.
 * <code><pre class="prettyprint lang-java">
 *  public class Example extends BaseServlet {
 * </pre></code>
 * <p>
 * This example servlet outputs a similar page to that of the other 51Degrees 
 * APIs.
 * <p>
 * WebApp module provides access to the WebProvider which extends the Core 
 * Provider, hence allowing access to the same set of features you can use for 
 * offline device detection. To access property:
 * <code><pre class="prettyprint lang-java">
 *  Property pr = super.getProvider(request).dataSet.get("IsMobile");
 * </pre></code>
 * <p>
 * WebProvider's role is to manage the temporary data files as in the Servlet 
 * environment it is not always possible to stop detection to update the data 
 * file, hence using temporary files allows to keep the master file free of 
 * write locks.
 * <p>
 * Since version 3.2 the Lite data file is no longer embedded into the API, so 
 * please remember to specify the BINARY_FILE_PATH in the Web.xml and place 
 * the data file at that path.
 * <!-- tutorial -->
 */
@SuppressWarnings("serial")
@WebServlet(name = "Example", urlPatterns = {"/Example"})
// Snippet Start
public class Example extends BaseServlet {
    
    /**
     * A pre-defined list of properties to display in the results list.
     */
    private final static String[] PROPERTIES = {"BrowserName", "BrowserVendor", 
        "BrowserVersion", "DeviceType", "HardwareVendor", "IsTablet", 
        "IsMobile", "IsCrawler", "ScreenInchesDiagonal","ScreenPixelsWidth"};
    /**
     * Location of the CSS.
     */
    private static final String CSS_INCLUDE = 
            "https://51degrees.com/Demos/examples.css";
    /**
     * Logger to log exceptions.
     */
    private final static Logger logger = 
            LoggerFactory.getLogger(Example.class);
    
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request.
     * @param response servlet response.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException if an I/O error occurs.
     */
    protected void processRequest(  HttpServletRequest request, 
                                    HttpServletResponse response)
                            throws ServletException, IOException, Exception {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        // Start writing HTML code
        out.println("<html>");
        out.println("<head>");
        out.println("<meta name=\"viewport\" content=\"width=device-width, "
                + "initial-scale=1, maximum-scale=1\">");
        out.println("<title>51Degrees Pattern Example Servlet</title>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" "
                + "href=\""+CSS_INCLUDE+"\" class=\"inline\"/>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"content\">");
        out.println("<img src=\""
                + getURL("image")
                + "\" alt=\"51Degrees logo\">");  
        out.println("<h1>Java Pattern - Device Detection Example</h1>");
        // Print dataset information like published date or device combinations.
        printDatasetInformation(out, request);
        // Print a table of HTTP headers and values.
        printHeadersTable(out, request, null);
        // Print a table with match metrics and a list of properties.
        printProperties(out, request); 
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    /**
     * Method prints HTML table with the list of HTTP headers that are 
     * important for device detection and values for HTTP headers that had a 
     * value set for the current request.
     * 
     * @param out PrintWriter object to use for printing the output.
     * @param request HttpServletRequest to get HTTP headers from.
     * @param specificHeaders if not null will limit the output only to HTTP 
     * header names provided in this array. Only the relevant HTTP headers will 
     * be considered.
     * @throws Exception can occur when accessing the Provider object of the 
     * request.
     */
    private void printHeadersTable( final PrintWriter out, 
                                    final HttpServletRequest request, 
                                    String[] specificHeaders) 
                                    throws Exception {
        out.println("<table>");
        out.println("<tbody>");
        out.println("<th colspan=\"2\">");
        out.println("Relevant HTTP Headers Received");
        out.println("</th>");
        // Print all available headers.
        if (specificHeaders == null || specificHeaders.length == 0) {
            for (String header : super.getProvider(request)
                    .dataSet.getHttpHeaders()) {
                out.println("<tr>");
                out.println("<td>"+header+"</td>");
                if (request.getHeader(header) != null) {
                    out.println("<td>"+request.getHeader(header)+"</td>");
                } else {
                    out.println("<td>header not set</td>");
                }
                out.println("</tr>");
            }
        } else {
            // Filter out the supplied headers list and only use the 
            // relevant ones.
            String[] filteredHeaders = filterRelevantHeaders(
                    super.getProvider(request).dataSet.getHttpHeaders(), 
                    specificHeaders);
            for (int i = 0; i < filteredHeaders.length; i++) {
                out.println("<tr>");
                out.println("<td>"+filteredHeaders[i]+"</td>");
                if (request.getHeader(filteredHeaders[i]) != null) {
                    out.println("<td>" + 
                            request.getHeader(filteredHeaders[i]) + 
                            "</td>");
                } else {
                    out.println("<td>header not set</td>");
                }
                out.println("</tr>");
            }
        }
        out.println("<tbody>");
        out.println("<table>");
    }
    
    /**
     * Prints the table that contains Match Metrics and Device Properties.
     * 
     * @param out PrintWriter to use for outputting data.
     * @param request HttpServletRequest to print properties for.
     * @throws IOException could be thrown when accessing the provider object 
     * for the request.
     */
    private void printProperties(   final PrintWriter out, 
                                    final HttpServletRequest request) 
                                    throws IOException {
        out.println("<table>");
        out.println("<tbody>");
        
        //Print the Match Metrics block.
        out.println("<tr>");
        out.println("<th colspan=\"3\">Match Metrics</th>");
        out.println("<th rowspan=\"5\">");
        out.println("<a class=\"button\" target=\"_blank\" href=\""
                + getURL("metrics")
                + "\">About Metrics</a>");
        out.println("</th>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Id</td>");
        
        out.println("<td colspan=\"2\">");
        out.println(super.getProvider(request).match(request).getDeviceId());
        out.println("</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Method</td>");
        out.println("<td colspan=\"2\">");
        out.println(super.getProvider(request).match(request).getMethod());
        out.println("</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Difference</td>");
        out.println("<td colspan=\"2\">");
        out.println(super.getProvider(request).match(request).getDifference());
        out.println("</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Rank</td>");
        out.println("<td colspan=\"2\">");
        out.println(super.getProvider(request).match(request)
                .getSignature().getRank());
        out.println("</td>");
        out.println("</tr>");
       
        // Device Properties
        out.println("<tr>");
        out.println("<th colspan=\"3\">Device Properties</th>");
        out.println("<th rowspan=\""+(PROPERTIES.length + 1)+"\">");
        out.println("<a class=\"button\" target=\"_blank\" href=\""
                + getURL("properties")
                + "\">More Properties</a>");
        out.println("</th>");
        out.println("</tr>");
        for (String property : PROPERTIES) {
            out.println("<tr>");
            out.println("<td>");
            out.println("<a href=\"https://51degrees.com/resources/"
                    + "property-dictionary#"+property+"\">"+property+"</a>");
            out.println("</td>");
            Property pr = super.getProvider(request).dataSet.get(property);
            if (pr != null) {
                if (pr.valueType != PropertyValueType.JAVASCRIPT) {
                    if (super.getResult(request).containsKey(pr.getName())) {
                        String[] values = 
                                super.getResult(request).get(pr.getName());
                        int current = 0;
                        out.println("<td>");
                        for (String value : values) {
                            out.println(value);
                            if (current < values.length - 1) {
                                out.println(", ");
                            }
                            current++;
                        }
                        out.println("</td>");
                    }
                }
                out.println("<td>"+pr.valueType+"</td>");
            } else {
                out.println("<td>Switch Data Set</td><td>NULL</td>");
            }
            out.println("</tr>");
        }
        out.println("</tbody>");
        out.println("</table>");
    }
    
    /**
     * Method goes through the provided list of headers and the list of 
     * 51Degrees important HTTP headers and returns intersection of both lists.
     * 
     * Used primarily to prevent the display of any HTTP headers that are not 
     * relevant to device detection.
     * 
     * @param importantHeaders array of 51Degrees HTTP header strings.
     * @param headersToFilter array of HTTP header strings to filter.
     * @return 
     */
    private String[] filterRelevantHeaders( String[] importantHeaders, 
                                            String[] headersToFilter    ) {
        int importantHeadersLength = importantHeaders.length;
        int resultLength = headersToFilter.length;
        ArrayList<String> headers = new ArrayList<String>();
        // If no headers in to check, return empty list.
        if (resultLength <= 0) {
            return new String[0];
        }
        for (int i = 0; i < resultLength; i++) {
            for (int j = 0; j < importantHeadersLength; j++) {
                if (headersToFilter[i].equals(importantHeaders[j])) {
                    headers.add(importantHeaders[j]);
                    break;
                }
            }
        }
        return headers.toArray(new String[headers.size()]);
    }
    
    /**
     * Method prints information about the data set such as the published date 
     * and the next update date.
     * 
     * @param out
     * @param request servlet request.
     * @throws Exception 
     */
    private void printDatasetInformation(   final PrintWriter out, 
                                            final HttpServletRequest request) 
                                            throws Exception {
        out.println("<table>");
        out.println("<tbody>");
        out.println("<tr><th colspan=\"3\">Data Set Information</th></tr>");
        
        out.println("<tr>");
        out.println("<td>Dataset Initialisation Status: </td>");
        if (super.getProvider(request) != null) {
            out.println("<td>SUCCESS</td>");
        } else {
            out.println("<td>FAILED</td>");
        }
        // Compare Data Options button.
        out.println("<td rowspan=\"6\">");
        out.println("<a class=\"button\" href=\""
                + getURL("compare")
                +"\" target=\"_blank\">Compare Data Options</a>");
        out.println("</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Dataset published: </td>");
        out.println("<td>" + 
                super.getProvider(request).dataSet.published + 
                "</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Dataset next update: </td>");
        out.println("<td>" + 
                super.getProvider(request).dataSet.nextUpdate + 
                "</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Dataset version: </td>");
        out.println("<td>" + 
                super.getProvider(request).dataSet.version + 
                "</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Dataset name: </td>");
        out.println("<td>" + 
                super.getProvider(request).dataSet.getName() + 
                "</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Dataset device combinations: </td>");
        out.println("<td>" + 
                super.getProvider(request).dataSet.deviceCombinations + 
                "</td>");
        out.println("</tr>");
        
        out.println("</tbody>");
        out.println("</table>");
    }

    /**
     * Method generates a URL string based on the purpose of this URL.
     * @param purpose String indicating where the URL should point to.
     * @return A URL represented as string.
     */
    private String getURL(String purpose) {
        String url = "https://51degrees.com";
        String utm_source = "utm_source=github";
        String utm_medium = "utm_medium=repository";
        String utm_content = "utm_content=example-pattern";
        String utm_campaign = "utm_campaign=java-open-source";
        
        if (purpose.equals("image")) {
            // 51Degrees logo.
            url = "https://51degrees.com/DesktopModules/FiftyOne/Distributor/Logo.ashx";
        } else if (purpose.equals("metrics")) {
            // 'About Metrics' button links.
            url = "https://51degrees.com/support/documentation/pattern";
        } else if (purpose.equals("compare")) {
            // 'Compare Data Options' link in the Data Set Information section.
            url = "https://51degrees.com/compare-data-options";
        } else if (purpose.equals("properties")) {
            // 'More properties' button links.
            url = "https://51degrees.com/resources/property-dictionary";
        }
        // Build the URL
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        sb.append("?");
        sb.append(utm_source);
        sb.append("&");
        sb.append(utm_medium);
        sb.append("&");
        sb.append(utm_content);
        sb.append("&");
        sb.append(utm_campaign);
        return sb.toString();
    }
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "51Degrees API extending BaseServlet example.";
    }
    
    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(  HttpServletRequest request, 
                            HttpServletResponse response)
                            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }

    }
    
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response)
            throws ServletException, IOException {

            try {
                processRequest(request, response);
            } catch (Exception ex) {
                
            }
    }
}
// Snippet End
