/* *********************************************************************
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 * 
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.servlet;

import fiftyone.mobile.detection.Components;
import fiftyone.mobile.detection.Property;
import fiftyone.mobile.detection.Value;
import fiftyone.mobile.detection.matchers.Result;
import fiftyone.mobile.detection.webapp.FiftyOneServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * A simple Servlet example, using 51Degrees.mobi to detect incoming devices.
 *
 * @author 51Degrees.mobi
 * @version 2.1.16.1
 */
@WebServlet(name = "51D", urlPatterns = {"/"})
public class Servlet extends FiftyOneServlet {
    
    /**
     * Returns the value description.
     * @param property property being displayed.
     * @param value value being displayed.
     * @return 
     */
    private String calculateValueDescription(Property property, String value) {
        for(Value propertyValue : property.getValues()) {
            if (value.equals(propertyValue.getName())) {
                return propertyValue.getDescription();
            }
        }
        return null;
    }
    
    /**
     * Returns the HTML needed to display the values.
     * @param property the property the values relate to.
     * @param values list of values for the property.
     * @return HTML to display the values with descriptions if required.
     */
    private List<String> calculateValues(Property property, List<String> values) {
        ArrayList<String> list = new ArrayList<String>();
        for (String value : values) {
            String description = calculateValueDescription(property, value);
            if (description != null
                    && description.isEmpty() == false) {
                list.add(String.format(
                        "<span class=\"value title\" title=\"%s\">%s</span>",
                        description,
                        value));
            } else {
                list.add(String.format(
                        "<span class=\"value\">%s</span>",
                        value));
            }
        }
        return list;
    }
    
    /**
     * Returns a list item string in HTML.
     * 
     * @param property property the values relate to.
     * @param values the values to be displayed.
     * @param title true if a balloon title should be displayed.
     */
    private String calculateItem(Property property, List<String> values, boolean title) {
        if (title == true) {
            // Display the final property and values.
            return String.format(
                "<li class=\"item\"><span class=\"property title\" title=\"%s\"><a href=\"http://51degrees.mobi/Products/DeviceData/PropertyDictionary.aspx#%s\">%s</a></span>:&nbsp;%s</li>",
                property.getDescription(),
                property.getName(),
                property.getName(),
                join(calculateValues(property, values)));
            
        } else {
            return String.format(
                "<li class=\"item\"><span class=\"property\">%s:</span>&nbsp;<span class=\"value\">%s</span></li>",
                property.getName(),
                join(values));
        }
    }
    
    /**
     * If there are images of the device displays them in in order with 
     * captions. It's neat feature of 51Degrees.mobi Premium.
     * @param propertyName name of the property containing images.
     * @param values list of image values.
     * @return HTML string for display.
     */
    private String calculateImages(String propertyName, List<String> values) {
        StringBuilder builder = new StringBuilder();
        builder.append("<li class=\"item\">");
        builder.append(String.format(
            "<span class=\"property\">%s</span><br/>",
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
        builder.append("</li>");
        return builder.toString();
    }
    
    /**
     * Prints all the properties associated with the component provided.
     * 
     * @param out the output writer
     * @param r the result containing the property to be printed
     * @param component the type of component to print properties for
     */
    private void printComponentProperties(
            final PrintWriter out, 
            final HttpServletRequest request, 
            final Components component) throws ServletException {
        final TreeMap<String, String> list = new TreeMap<String, String>();
        final boolean isMobile = "True".equals(super.getProperty(request, "IsMobile"));
        for(Property property : super.getProvider().getProperties().values()) {
            if (property.getComponent() == component) {
                final Result result = super.getResult(request);
                if (result != null) {
                    if ("HardwareImages".equals(property.getName())) {
                        list.put(
                            property.getName(),
                            calculateImages(
                                property.getName(), 
                                result.getPropertyValues(property.getName())));
                    } else {
                        final List<String> values = result.getPropertyValues(
                                property.getName());
                        if (values != null) {
                            list.put(
                                property.getName(),
                                calculateItem(
                                    property, 
                                    values,
                                    isMobile == false));
                        }
                    }
                }
            }
        }
        if (list.size() > 0) {
            out.println(String.format(
                "<h4>%s</h4>",
                component.toString()));
            out.println("<ul class=\"list\">");
            for(String key : list.keySet()) {
                out.println(list.get(key));
            }
            out.println("</ul>");
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
        out.println(".heading {font-weight: bold;}");
        out.println(".list {list-style: none; padding: 0; margin: 0 0 0 1em;}");
        out.println(".list a {color: #777777}");
        out.println(".item {margin: 1em 0; color: #777777; text-decoration: none;}");
        out.println(".property {display: inline-block;}");
        out.println(".value {font-weight: bold; color: #333333}");
        out.println(".title {text-decoration: underline}");
        out.println(".image .value {text-align: center;}");
        out.println(".image {display: inline-block; margin: 1em;}");
        out.println("</style>");
        
        out.println("</head>");
        out.println("<body>");

        // Display the name and published date of the dataset being used.
        String dataInfo = String.format(
            "<h4 class=\"heading\">'%s' data published on '%tD' containing '%d' properties in use</h4>",
            super.getProvider().getDataSetName(),
            super.getProvider().getPublishedDate(),
            super.getProvider().getProperties().size());
        out.println(dataInfo);
        
        // Display all the properties available.
        printComponentProperties(out, request, Components.Hardware);
        printComponentProperties(out, request, Components.Software);
        printComponentProperties(out, request, Components.Browser);
        printComponentProperties(out, request, Components.Crawler);
        printComponentProperties(out, request, Components.Unknown);
      
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
