package fiftyone.mobile.detection.factories;

import fiftyone.mobile.Filename;
import fiftyone.mobile.StandardUnitTest;
import fiftyone.mobile.detection.TrieProvider;
import org.junit.Test;

import static fiftyone.mobile.detection.helper.ViableProvider.ensureViableProvider;

/**
 * Minimal test for TrieFactory
 */
public class TrieFactoryTest extends StandardUnitTest {

    @Test
    public void testCreateFromFilename() throws Exception {
        TrieProvider p = TrieFactory.create(Filename.LITE_TRIE_V32) ;
        ensureViableProvider(p);

    }
}
