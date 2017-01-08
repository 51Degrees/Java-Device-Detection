package fiftyone.mobile.detection;

import fiftyone.mobile.Filename;
import fiftyone.mobile.StandardUnitTest;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.factories.StreamFactoryTest;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static fiftyone.mobile.detection.helper.MatchHelper.matchEquals;

/**
 * minimal testing that Provider works. Some of this is covered in the Factory and DatasetBuilder tests
 *
 * @see StreamFactoryTest
 * @see DatasetBuilderTest
 */
public class ProviderTest extends StandardUnitTest {

    // check we get the same results whatever way we create a dataset
    @Test
    public void testMemoryAndStreamSame () throws IOException {
        StreamDataset cachedDataset = DatasetBuilder.stream()
                .addDefaultCaches()
                .build(Filename.LITE_PATTERN_V32);
        Provider cachedProvider = new Provider(cachedDataset);

        StreamDataset unCachedDataset = DatasetBuilder.stream()
                .build(Filename.LITE_PATTERN_V32);
        Provider unCachedProvider = new Provider(unCachedDataset);

        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);
        Provider memoryProvider = new Provider(memoryDataset);

        FileInputStream is = new FileInputStream(Filename.GOOD_USERAGENTS_FILE);
        BufferedReader source = new BufferedReader(new InputStreamReader(is));
        String line;
        int count = 0;
        while ((line = source.readLine()) != null) {
            Match cachedMatch = cachedProvider.match(line);
            Match unCachedMatch = unCachedProvider.match(line);
            Match memoryMatch = memoryProvider.match(line);
            matchEquals(memoryMatch,cachedMatch);
            matchEquals(memoryMatch,unCachedMatch);
            count++;
        }
        logger.info("{} tests done", count);
        source.close();
        cachedDataset.close();
        unCachedDataset.close();
        memoryDataset.close();
    }
}