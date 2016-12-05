package fiftyone.device.proto;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.factories.StreamFactory;

import java.io.IOException;
import java.util.Date;

/**
 * Builds a dataset for memory use
 * @author jo
 */
public class MemoryDatasetBuilder {
    private Date lastModified;
    private boolean isInit;

    public  MemoryDatasetBuilder init() {
        isInit = true;
        return this;
    }

    MemoryDatasetBuilder lastModified(Date date) {
        lastModified = date;
        return this;
    }

    public Dataset build(byte[] buffer) throws IOException {
        return StreamFactory.create(buffer);
    }

    public Dataset build(String filename) throws IOException {
        return StreamFactory.create(filename, lastModified, isInit);
    }
}
