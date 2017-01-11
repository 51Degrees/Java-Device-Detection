package fiftyone.mobile.detection.factories;

import com.google.common.io.Files;
import fiftyone.mobile.Filename;
import fiftyone.mobile.StandardUnitTest;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.IndirectDataset;
import fiftyone.mobile.detection.helper.ViableProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static fiftyone.mobile.detection.helper.DatasetHelper.cacheTests;
import static fiftyone.mobile.detection.helper.DatasetHelper.compareDatasets;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Validate StreamFactory
 */
public class StreamFactoryTest extends StandardUnitTest {

    // create a 3.2 pattern provider using factory create method
    @Test
    public void testCreate32FromFilename () throws Exception {
        File temp = File.createTempFile("Test",".dat");
        File source = new File(Filename.LITE_PATTERN_V32);
        Files.copy(source, temp);

        Dataset dataset = StreamFactory.create(temp.getPath());
        ViableProvider.ensureViableProvider(new Provider(dataset));
        dataset.close();
        // temp must still exist after close
        assertTrue(temp.exists());

        // assess whether temporary file gets deleted
        dataset = StreamFactory.create(temp.getPath(), true);
        ViableProvider.ensureViableProvider(new Provider(dataset));
        dataset.close();
        // temp must NOT still exist after close
        assertFalse(temp.exists());

    }

    // create 3.1 pattern provider using factory create method
    @Test
    public void testCreate31FromFilename () throws Exception {
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V31);
        ViableProvider.ensureViableProvider(new Provider(dataset));
    }

    // tests to see if Stream and Memory load the same thing (default caches)
    @Test
    public void testMemoryStreamDatasetConsistentDefault () throws IOException {

        IndirectDataset indirectDataset =
                StreamFactory.create(Filename.LITE_PATTERN_V32);
        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);

        compareDatasets(indirectDataset, memoryDataset);
    }

    // see if the cache metrics etc work when using default cache.
    @Test
    public void testDefaultCache () throws Exception {
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32);
        Provider provider = new Provider(dataset, 20);

        cacheTests(provider);
    }
}