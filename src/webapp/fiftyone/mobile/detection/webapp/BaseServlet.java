package fiftyone.mobile.detection.webapp;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fiftyone.mobile.detection.Match;
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

/**
 * Adds functionality to a base HttpServlet to provide properties associated
 * with the requesting device. The Listener must be included in the web 
 * configuration.
 */
@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet{
    /**
     * Used to store the result for the current request in the 
     * HttpServletRequest's attribute collection.
     */
    private static final String RESULT_ATTIBUTE = "51D_RESULT";
    /**
     * The factory class used to access the current provider used across
     * all servlets.
     */
    private FiftyOneDegreesListener listener = null;
    
    /**
     * Creates a logger for this class
     */
    private final Logger logger = LoggerFactory.getLogger(BaseServlet.class);
    
    /**
     * Gets the factory being used by this servlet context.
     * @param sc
     * @throws ServletException 
     */
    @Override
    public void init(final ServletConfig sc) throws ServletException {
        super.init();
        this.listener = (FiftyOneDegreesListener)sc.getServletContext().
                getAttribute(Constants.WEB_PROVIDER_KEY);
        if (this.listener != null) {
            logger.info("51Degrees.mobi Servlet Initialised");
        } else {
            throw new ServletException(
                "51Degrees.mobi listener is not available. " +
                "Check the class fiftyone.mobile.detection.webapp.FiftyOneDegreesListener " +
                "is registered in the web.xml file.");
        }
    }
       
    /**
     * Returns the result set associated with the request provided. If this is 
     * the first time the method is called the result will be stored in the 
     * HttpServletRequest's attribute collection so that it does not need to 
     * be fetched again in the future.
     * @param request the request property results are for.
     * @return a set of results containing access to properties.
     * @throws IOException 
     */
    protected Match getResult(final HttpServletRequest request) throws ServletException, IOException {
        // Check to see if the result has already been fetched.
        Object previousResult = request.getAttribute(RESULT_ATTIBUTE);
        if (previousResult instanceof Match == false) {
            previousResult = listener.getProvider().getResult(request);
            request.setAttribute(RESULT_ATTIBUTE, previousResult);
        }
        return (Match)previousResult;
    }
    
    /**
     * Returns the value associated with the device property requested.
     * @param request the request property results are for.
     * @param propertyName device property name required.
     * @return 
     * @throws IOException 
     */
    protected String getProperty(
            final HttpServletRequest request, 
            final String propertyName) throws ServletException, IOException {
        final Match result = getResult(request);
        if (result != null) {
            Values values = result.getValues(propertyName);
            if (values != null) {
                return values.toString();
            }
        }
        return null;
    }
    
    /**
     * Joins a list of strings into a comma separated string.
     * @param values list of values to join
     * @return single string comma separated
     */
    protected String join (final List<String> values){
        final StringBuilder builder = new StringBuilder();
        for(int i = 0; i < values.size(); i++) {
            builder.append(values.get(i));
            if (i + 1 < values.size()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

	public WebProvider getProvider() {
		return listener.getProvider();
	}    
}