/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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

import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.webapp.BaseServlet;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <!-- tutorial -->
 * Illustrates how to use 51Degrees device detection individual properties and 
 * values.
 * <p>
 * Example covers:
 * <ul>
 *  <li>Extending 51Degrees BaseServlet
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      public class UsingDetection extends BaseServlet {
 *  </code>
 *  </pre>
 *  <li>Accessing the dataset of the provider to retrieve a list of properties 
 *  available in the current data file.
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      super.getProvider(request).dataSet.getProperties();
 *  </code>
 *  </pre>
 *  <li>Retrieving value for a specific property for the current request
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      String value = super.getProperty(request, propertyNameString);
 *  </code>
 *  </pre>
 * </ul>
 * This is a complete servlet example using 51Degrees device detection. 
 * Remember to place the 51Degrees data file into the 'WEB-INF/51Degrees.dat' 
 * directory or provide an alternative path to the data file in the 
 * BINARY_FILE_PATH setting of Web.xml.
 * <!-- tutorial -->
 * <p>
 * The output below will vary depending on the data file you use. Premium and 
 * Enterprise data files will provide considerably more properties than the 
 * Lite (free) data file.
 * For more information please see:
 * <a href="https://51degrees.com/compare-data-options">
 * Compare Data Options</a>
 */
// Snippet Start
public class UsingDetection extends BaseServlet {
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException if there was a problem accessing the data file.
     */
    protected void processRequest(HttpServletRequest request, 
                                  HttpServletResponse response)
                                  throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet UsingDetection</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>51Degrees Servlet Device Detection Example.</h1>");
            String name, value;
            // Get a list of all properties available in the data file.
            // For each property print corresponding value.
            for (Property property : 
                    super.getProvider(request).dataSet.getProperties()) {
                // Name of the current property.
                name = property.getName();
                // Value of the current property as a string.
                value = super.getProperty(request, name);
                // Only use value if it's not null.
                if (value != null) {
                    out.println("<li>" + name + ": " + value + "</li>");
                }
            }
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
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
     * Handles the HTTP <code>POST</code> method.
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
        return "Short description";
    }
}
// Snippet End
