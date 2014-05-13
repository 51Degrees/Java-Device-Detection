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

package fiftyone.mobile.detection;

import fiftyone.mobile.detection.binary.Reader;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class Run by the Timer object in the Factory51D Object. Used to ensure that
 * all instances have the latest data available on the file system.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class FileAutoChecker extends java.util.TimerTask {
    
    /**
     * The factory class that created this timer task.
     */
    protected Factory _factory = null;
    
    /**
     * The last modified data of the data currently being used.
     */
    protected long _loadedFileModifiedDate = 0;
    
        /**
     * Constructs a new instance of the checker for the factory provided.
     * @param factory 
     */
    public FileAutoChecker(final Factory factory)
    {
        super();
        this._factory = factory;
        
        this._loadedFileModifiedDate = _factory.getBinaryFilePath().lastModified();
    }
    
    /**
     * Creates a logger for this class
     */
    private static final Logger _logger = LoggerFactory.getLogger(FileAutoChecker.class);
    
    /**
     * Runs the file checker.
     */
    @Override
    public synchronized void run() 
    {
        _logger.trace("Checking for newer data file on file system.");
        final File binaryFile = _factory.getBinaryFilePath();
        
        // first check if the data file is newer than the one loaded
        if (binaryFile != null && binaryFile.lastModified() > _loadedFileModifiedDate) 
        {
            _logger.info(String.format("Newer data file on file system found. New file last modified at '%d'.",
                    binaryFile.lastModified())
                );
            
            // create a new provider to fill
            final Provider provider = _factory.createProvider();
            
            if(Reader.read(provider, binaryFile.getAbsolutePath())) 
            {
                // provider was successfully filled with new data. now set as active provider
                _factory.setActiveProvider(provider);
                _logger.info(
                String.format("New provider set from file. Data publish date - '%s'.",
                        provider.getPublishedDate().toString())
                );
            }
            else
            {
                // error while loading data
                _logger.info("Provider could not be set from newer data on the file system. The file may be corrupt.");
            }
            _loadedFileModifiedDate = binaryFile.lastModified();
        }
    }
}
