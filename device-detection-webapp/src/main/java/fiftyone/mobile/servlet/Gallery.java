package fiftyone.mobile.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fiftyone.mobile.detection.webapp.BaseServlet;

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
 * A simple Servlet example showing the image optimiser and bandwidth detection
 * functions. Needs Ultimate Device data.
 *
 */
@SuppressWarnings("serial")
@WebServlet(name = "Gallery", urlPatterns = {"/Gallery"})
public class Gallery extends BaseServlet {

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
        out.println("<script src=\"51D/core.js\" type=\"text/javascript\"></script>");
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
                "<h4 class=\"heading\">'%s' data published on '%tc' containing '%d' properties in use</h4>",
                super.getProvider(request).dataSet.getName(),
                super.getProvider(request).dataSet.published,
                super.getProvider(request).dataSet.getProperties().size());

        String mainImage = null;
        if (request.getQueryString() != null) {
            List<String> params = new ArrayList<String>();
            StringTokenizer ps = new StringTokenizer(request.getQueryString(), "&", false);
            while (ps.hasMoreTokens()) {
                params.add(ps.nextToken());
            }
            Map<String, String> map = new HashMap<String, String>();
            for (String param : params) {
                StringTokenizer st = new StringTokenizer(param, "=", false);
                map.put(st.nextToken(), st.nextToken());
            }
            mainImage = map.get("image");
        }

        if (mainImage != null) {
            out.printf("<div><img src=\"51D%s?w=600\"/><p>%s</P></div>", mainImage, mainImage);
        } else {
            ServletContext context = request.getServletContext();
            if (context != null) {
                Set<String> images = context.getResourcePaths("/images");
                for (String image : images) {
                    if (image.endsWith(".jpg")) {
                        out.printf(
                                "<div style=\"width: 200px;\"><a href=\"?image=%s\"><img src=\"51D/Empty.gif\" data-src=\"51D%s?w=auto\"/></a><p>%s</P></div>",
                                image, image, image);
                    }
                }
            }
        }

        // Display the name and published date of the dataset being used.
        out.println(dataInfo);
        
        // Add the call to the automatic image optimiser.
        out.println("<script type=\"text/javascript\">FODIO('w', 'h');</script>");

        out.println("</body>");
        
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
        return "An example image gallary using 51Degrees image optimisation";
    }
}
