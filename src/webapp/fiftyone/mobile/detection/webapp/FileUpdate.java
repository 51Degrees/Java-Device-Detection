/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.webapp;

import java.io.File;
import java.util.Date;
import java.util.TimerTask;

/**
 *
 * @author tom
 */
public class FileUpdate extends TimerTask {

    private Date lastModifiedDate = null;
    final private String binaryDataFilePath;
    final private FiftyOneDegreesListener listener;

    public FileUpdate(final FiftyOneDegreesListener listener, final String binaryDataFilePath) {
        super();

        this.listener = listener;
        this.binaryDataFilePath = binaryDataFilePath;
        lastModifiedDate = getDataFileDate();
    }

    @Override
    public void run() {
        final Date fileDate = getDataFileDate();
        if (lastModifiedDate == null && fileDate != null) {
            listener.refreshWebProvider();
        } else if (lastModifiedDate != null && fileDate != null && fileDate.after(lastModifiedDate)) {
            listener.refreshWebProvider();
        }
    }

    private Date getDataFileDate() {
        Date fileDate = null;
        final File currentFile = new File(binaryDataFilePath);
        if (currentFile.exists()) {
            fileDate = new Date(currentFile.lastModified());
        }
        return fileDate;
    }
}
