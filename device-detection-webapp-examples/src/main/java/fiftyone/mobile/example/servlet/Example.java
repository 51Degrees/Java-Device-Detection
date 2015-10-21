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

import fiftyone.mobile.detection.Match;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Property.PropertyValueType;
import fiftyone.mobile.detection.webapp.BaseServlet;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * A simple Servlet example, using 51Degrees to detect incoming devices.
 *
 */
@SuppressWarnings("serial")
@WebServlet(name = "Example", urlPatterns = {"/Example"})
public class Example extends BaseServlet {
    
    /**
     * A pre-defined list of properties to display in the results list.
     */
    private final static String[] properties = {"BrowserName", 
                                                "BrowserVendor",
                                                "BrowserVersion", 
                                                "DeviceType", 
                                                "HardwareVendor", 
                                                "IsTablet", 
                                                "IsMobile", 
                                                "IsCrawler", 
                                                "ScreenInchesDiagonal",
                                                "ScreenPixelsWidth"};

	/**
	 * Orders the properties before display.
	 */
	private static final Comparator<Property> _propertyComparer = new Comparator<Property>() {
		@Override
		public int compare(Property arg0, Property arg1) {
			int difference = 0;
			try {
				String c0 = arg0.getCategory();
				String c1 = arg1.getCategory();
				difference = (c0 == null ? "" : c0).compareTo(c1 == null ? "" : c1);
				if (difference == 0)
					difference = arg0.getName().compareTo(arg1.getName());
			} catch (IOException e) {
				// Do nothing.
			}
			return difference;
		}
	};
	
    /**
     * Joins the array of strings separated by the separator provided.
     * @param seperator
     * @param values
     * @return 
     */
    private static String stringJoin(String seperator, String[] values) {
        String value;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            stringBuilder.append(values[i]);
            if (i < values.length - 1) {
                stringBuilder.append(seperator);
            }
        }
        value = stringBuilder.toString();
        return value;
    }        
        
    /**
     * Returns a list item string in HTML.
     * 
     * @param property property the values relate to.
     * @param values the values to be displayed.
     * @param title true if a balloon title should be displayed.
     * @throws IOException 
     */
    private String calculateItem(Property property, String[] values, boolean title) throws IOException {
        if (title == true) {
            // Display the final property and values.
            return String.format(
                "<tr class=\"item\"><td class=\"property\" title=\"%s\"><a href=\"http://51degrees.com/Resources/Property-Dictionary#%s\">%s</a></td><td>%s</td></tr>",
                property.getDescription(),
                property.getName(),
                property.getName(),
                stringJoin(", ", values));
            
        } else {
            return String.format(
                "<tr class=\"item\"><td class=\"property\">%s:</td><td class=\"value\">%s</td></tr>",
                property.getName(),
                stringJoin(", ", values));
        }
    }
    
    /**
     * If there are images of the device displays them in in order with 
     * captions. It's neat feature of 51Degrees.mobi Premium.
     * @param propertyName name of the property containing images.
     * @param values list of image values.
     * @return HTML string for display.
     */
    private String calculateImages(String propertyName, String[] values) {
        StringBuilder builder = new StringBuilder();
        builder.append("<tr class=\"item\">");
        builder.append(String.format(
            "<td class=\"property\">%s</td><td>",
            propertyName));
        for(String value : values) {
            String[] elements = value.split("\t");
            if (elements.length == 2) {
            builder.append(String.format(
                "<div class=\"image\"><img src=\"%s\"></img><div class=\"value\">%s</div></div>",
                elements[1],
                elements[0]));
            }
        }
        builder.append("</td></tr>");
        return builder.toString();
    }
    
    /**
     * Prints all the properties associated with the component provided.
     * 
     * @param out the output writer
     * @param r the result containing the property to be printed
     * @param component the type of component to print properties for
     * @throws IOException 
     */
    private void printComponentProperties(
            final PrintWriter out, 
            final HttpServletRequest request, 
            final Component component) throws ServletException, IOException, Exception {

        final Map<String, String[]> result = super.getResult(request);
        final boolean isMobile = "True".equals(super.getProperty(request, "IsMobile"));

        List<Property> properties = Arrays.asList(component.getProperties());
        Collections.sort(properties, _propertyComparer);
        String lastCategory = null;        
        
        if (properties.size() > 0) {
    		out.println(String.format(
    				"<tr><th colspan=\"2\">%s</th></tr>",
                    component.getName()));
	        for(Property property : properties) {
	        	if (property.valueType != PropertyValueType.JAVASCRIPT) {
		        	if (lastCategory != property.getCategory()) {
		        		lastCategory = property.getCategory();
		        		out.println(String.format(
		                        "<tr><td>%s</td></tr>",
		                        lastCategory));
		        	}
		        	String snippet = null;
		            if ("HardwareImages".equals(property.getName())) {
		            	snippet = calculateImages(
		                        property.getName(), 
		                        result.get(property.getName()));
		            } else {
		                final String[] values = result.get(
		                        property.getName());
		                if (values != null) {
		                	snippet = calculateItem(
		                            property, 
		                            values,
		                            isMobile == false);
		                }
		            }
		            
		            if (snippet != null) {
		            	out.println(snippet);
		            }
	        	}
	        }
        }
    }
    
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, Exception {
        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        // Start writing HTML code
        out.println("<html>");
        out.println("<head>");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\">");
        out.println("<title>51Degrees Example Servlet</title>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"https://51degrees.com/Demos/examples.css\" class=\"inline\"/>");
        
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"content\">");

        out.println("<img src=\"https://51degrees.com/DesktopModules/FiftyOne"
                + "/Distributor/Logo.ashx?utm_source=&utm_medium=repository"
                + "&utm_content=website-example-logo&utm_campaign=java-open"
                + "-source\">");
        out.println("<h1>Java Pattern - Device Detection Example</h1>");
        
        printDatasetInformation(out, request);
        
        printHeadersTable(out, request, null);
        
        printProperties(out, request);
        
        // Display the name and published date of the dataset being used.
        out.println("</div>");
        
        // For each of the components display the available properties.
        out.close();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

            try {
                processRequest(request, response);
            } catch (Exception ex) {
                
            }
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            try {
                processRequest(request, response);
            } catch (Exception ex) {
               
            }
    }
    
    private void printMatchResults(Match match) {
        //match.
    }
    
    /**
     * 
     * @param out
     * @param request
     * @param specificHeaders
     * @throws Exception 
     */
    private void printHeadersTable( final PrintWriter out, 
                                    final HttpServletRequest request, 
                                    String[] specificHeaders) 
                                    throws Exception {
        out.println("<table>");
        out.println("<tbody>");
        // Print all available headers.
        if (specificHeaders == null || specificHeaders.length == 0) {
            for (String header : super.getProvider(request).dataSet.getHttpHeaders()) {
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
                    out.println("<td>"+request.getHeader(filteredHeaders[i])+"</td>");
                } else {
                    out.println("<td>header not set</td>");
                }
                out.println("</tr>");
            }
        }
        out.println("<tbody>");
        out.println("<table>");
    }
    
    
    private void printProperties(   final PrintWriter out, 
                                    final HttpServletRequest request) 
                                    throws IOException {
        out.println("<table>");
        out.println("<tbody>");
        
        //Print the Match Metrics block.
        out.println("<tr>");
        out.println("<th colspan=\"3\">Match Metrics</th>");
        out.println("<th rowspan=\"5\">");
        out.println("<a class=\"button\" target=\"_blank\" "
                + "href=\"https://51degrees.com/support/documentation/pattern?"
                + "utm_source=&"
                + "utm_medium=repository&"
                + "utm_content=example-pattern-about-metrics&"
                + "utm_campaign=java-open-source\">About Metrics</a>");
        out.println("</th>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Id</td>");
        out.println("<td colspan=\"2\"> 12345-6789-1011-1213");
        //super.get
        out.println("</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Method</td>");
        out.println("<td colspan=\"2\"> Exact");
        //super.get
        out.println("</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Difference</td>");
        out.println("<td colspan=\"2\"> 0");
        //super.get
        out.println("</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Rank</td>");
        out.println("<td colspan=\"2\"> 12345");
        //super.get
        out.println("</td>");
        out.println("</tr>");
        /**/
        
        // Device Properties
        out.println("<tr>");
        out.println("<th colspan=\"3\">Device Properties</th>");
        out.println("<th rowspan=\""+(properties.length + 1)+"\">");
        out.println("<a class=\"button\" target=\"_blank\""
                + " href=\"https://51degrees.com/resources/property-"
                + "dictionary?utm_source=&utm_medium=repository&utm_content"
                + "=example-pattern-more-properties&utm_campaign=java-"
                + "open-source\">More Properties</a>");
        out.println("</th>");
        out.println("</tr>");
        for (String property : properties) {
            out.println("<tr>");
            out.println("<td>");
            out.println("<a href=\"https://51degrees.com/resources/"
                    + "property-dictionary#"+property+"\">"+property+"</a>");
            out.println("</td>");
            Property pr = super.getProvider(request).dataSet.get(property);
            if (pr != null) {
                if (pr.valueType != PropertyValueType.JAVASCRIPT) {
                    if (super.getResult(request).containsKey(pr.getName())) {
                        String[] values = super.getResult(request).get(pr.getName());
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
    
    private String[] filterRelevantHeaders(String[] importantHeaders, String[] headersToFilter) {
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
        out.println("<a class=\"button\" "
                + "href=\"https://51degrees.com/compare-data-options?utm_source"
                + "=&utm_medium=repository&utm_content=example-dataset-information"
                + "&utm_campaign=java-open-source\" target=\"_blank\">"
                + "Compare Data Options</a>");
        out.println("</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Dataset published: </td>");
        out.println("<td>"+super.getProvider(request).dataSet.published+"</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Dataset next update: </td>");
        out.println("<td>"+super.getProvider(request).dataSet.nextUpdate+"</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Dataset version: </td>");
        out.println("<td>"+super.getProvider(request).dataSet.version+"</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Dataset name: </td>");
        out.println("<td>"+super.getProvider(request).dataSet.getName()+"</td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td>Dataset device combinations: </td>");
        out.println("<td>"+super.getProvider(request).dataSet.deviceCombinations+"</td>");
        out.println("</tr>");
        
        out.println("</tbody>");
        out.println("</table>");
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "An example of 51degrees.mobi's Java device detection solution";
    }// </editor-fold>
}
