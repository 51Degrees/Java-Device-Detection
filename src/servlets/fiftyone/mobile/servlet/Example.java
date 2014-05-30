package fiftyone.mobile.servlet;

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
import java.util.Map;

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

/**
 *
 * A simple Servlet example, using 51Degrees to detect incoming devices.
 *
 */
@SuppressWarnings("serial")
@WebServlet(name = "Example", urlPatterns = {"/Example"})
public class Example extends BaseServlet {
    
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
            final Component component) throws ServletException, IOException {

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
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        // Start writing HTML code
        out.println("<html>");
        out.println("<head>");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\">");
        out.println("<title>51Degrees Example Servlet</title>");
        out.println("<style type=\"text/css\">");
        out.println("body {font-size: 12pt; font-family: Arial;}");
        out.println(".list {width:400px;}");
        out.println(".heading {font-weight: bold;}");
        out.println(".item {margin: 1em 0; color: #777777; text-decoration: none;}");
        out.println(".property {font-weight: bold;}");
        out.println(".value {font-weight: normal; color: #333333;}");
        out.println(".image .value {text-align: center;}");
        out.println(".image {display: inline-block; margin: 1em;}");
        out.println(".title {text-align: center;}");
        out.println("</style>");
        
        out.println("</head>");
        out.println("<body>");

        // Display the name and published date of the dataset being used.
        String dataInfo = String.format(
            "<h4 class=\"heading\">'%s' data published on '%tc' containing '%d' properties in use</h4>",
            super.getProvider(request).dataSet.getName(),
            super.getProvider(request).dataSet.published,
            super.getProvider(request).dataSet.getProperties().size());
        
        // Display all the properties available.
        out.println("<table class=\"list\">");
        for(Component component : super.getProvider(request).dataSet.components) {
        	printComponentProperties(out, request, component);
        }
        out.println("</table>");
        
        // Display the name and published date of the dataset being used.
        out.println(dataInfo);
        
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

        processRequest(request, response);
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
        processRequest(request, response);
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
