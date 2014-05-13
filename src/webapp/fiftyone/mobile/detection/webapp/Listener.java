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
package fiftyone.mobile.detection.webapp;

import fiftyone.mobile.detection.Factory;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class Listener extends Factory implements ServletContextListener{

    /**
     * Creates a _logger for this class
     */
    final private static Logger _logger = LoggerFactory.getLogger(Listener.class);
    
    private ThreadPoolExecutor _threadPool;
    
    /**
     * Initialises the 51Degrees.mobi factory with a provider and auto update
     * settings to enable servlets to be able to access properties of the 
     * requesting device.
     * 
     * @param contextEvent 
     */
    @Override
    public void contextInitialized(final ServletContextEvent contextEvent) {
        try{    // first try to get premium data
            _threadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
            
        initialize(
            contextEvent.getServletContext().getRealPath(Constants.DEFAULT_DATA_FILE_PATH),
            (String)null // instead of null, put your licence key there.
            ,_threadPool
                );  
        }
        catch(Exception ex) // failed, now get lite data
        {
            initialize();
        }
        contextEvent.getServletContext().setAttribute(Constants.FACTORY_KEY, this);
        _logger.info("51Degrees.mobi Listener initialised");        
    }
    /**
     * Calls the base factory to stop the auto update timer and destroy the
     * current provider.
     * @param contextEvent 
     */
    @Override
    public void contextDestroyed(final ServletContextEvent contextEvent) {
        _logger.info("Destroying 51Degrees.mobi Listener");            
        try {
            _threadPool.shutdown();
            super.destroy();
        } catch (Throwable ex) {
            _logger.warn(
                    "Exception destroying factory",
                    ex);
        }
        _logger.info("Destroyed 51Degrees.mobi Listener");
    }
    /**
     * Creates a new version of the webapp provider with support for getting
     * results from HttpServletRequests.
     * @return 
     */
    @Override
    protected Provider createProvider() {
        return new Provider();
    }
    /**
     * Returns a provider of type webapp.Provider.
     * @return the active provider.
     */
    @Override
    public Provider getProvider() {
        return (Provider)super.getProvider();
    }
}