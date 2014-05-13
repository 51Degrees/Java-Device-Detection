/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.webapp;

import fiftyone.mobile.detection.AutoUpdateException;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tom
 */
public class AutoUpdate extends TimerTask {
    
    private final FiftyOneDegreesListener listener;
    private final String masterFilePath;
    private String[] licenseKeys;
    final private static Logger logger = LoggerFactory
            .getLogger(AutoUpdate.class);
    
    public AutoUpdate(final FiftyOneDegreesListener listener,
            final String masterFilePath,
            final List<String> licenseKeys) {
        super();
        this.listener = listener;
        this.masterFilePath = masterFilePath;
        this.licenseKeys = licenseKeys.toArray(new String[licenseKeys.size()]);
    }
    
    @Override
    public void run() {
        if (shouldUpdate()) {
            try {
                boolean success = fiftyone.mobile.detection.AutoUpdate.update(licenseKeys, masterFilePath);
                if (success) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("y-MMM-d");
                    final File masterFile = new File(masterFilePath);                    
                    Date fileDate = new Date(masterFile.lastModified());
                    String dateStr = dateFormat.format(fileDate);
                    logger.info(String.format("Automatically updated binary data file '%s' with version " +
                        "published on the '%s'.",
                        masterFile,
                        dateStr));
                    
                    listener.refreshWebProvider();
                }
            } catch (AutoUpdateException ex) {
                logger.debug(ex.getMessage());
            }
        }
    }
    
    private boolean shouldUpdate() {
        // check if file exists
        boolean shouldUpdate = true;
        final File masterFile = new File(masterFilePath);
        // If no file exists an update is definitely required.
        if (masterFile.exists()) {
            try {
                Dataset dataset = StreamFactory.create(masterFilePath);
                if (new Date().before(dataset.published)) {
                    shouldUpdate = false;
                }
            } catch (IOException ex) {
                // data file is probably corrupt, allow update
            }
        }
        
        return shouldUpdate;
    }
}
