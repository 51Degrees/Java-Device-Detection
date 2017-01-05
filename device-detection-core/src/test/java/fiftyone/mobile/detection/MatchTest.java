package fiftyone.mobile.detection;

import fiftyone.mobile.Filename;
import fiftyone.mobile.StandardUnitTest;
import fiftyone.mobile.detection.factories.MemoryFactory;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * This is a test to see how much difference recycling the Match class compares to creating anew.
 *
 * @author jo
 */
public class MatchTest extends StandardUnitTest {
    private static final int NUMBER_OF_TESTS = 1000000;

    @Test
    public void testCreate() throws IOException, InterruptedException {
        long start;
        Provider provider = new Provider(MemoryFactory.create(new FileInputStream(Filename.LITE_PATTERN_V32)));

        List<Match> matches= new ArrayList<Match>(NUMBER_OF_TESTS);

        // warm up jvm
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.add(provider.createMatch());
        }
        create(provider, matches);

        // sleep and gather our breath.
        System.gc();
        Thread.sleep(2000);

        //measure how long it takes to add and create matches
        start = System.currentTimeMillis();
        create(provider, matches);
        System.out.printf("%,d millis to create%n", System.currentTimeMillis()-start);

        // warm up jvm
        newMatch(provider, matches);


        // sleep and gather our breath.
        System.gc();
        Thread.sleep(2000);


        // measure how long it takes to get from array and add new
        start = System.currentTimeMillis();
        newMatch(provider, matches);
        System.out.printf("%,d millis for new %n", System.currentTimeMillis()-start);


        // warm up
        reset(matches);

        System.gc();
        Thread.sleep(2000);

        // above again
        start = System.currentTimeMillis();
        reset(matches);
        System.out.printf("%,d millis to reset%n", System.currentTimeMillis()-start);

        System.gc();
        Thread.sleep(2000);

        // above again
        start = System.currentTimeMillis();
        reset(matches);
        System.out.printf("%,d millis to reset%n", System.currentTimeMillis()-start);

        System.gc();
        Thread.sleep(2000);

        // repeat test just for kix
        start = System.currentTimeMillis();
        newMatch(provider, matches);
        System.out.printf("%,d millis new%n", System.currentTimeMillis()-start);

        System.gc();
        Thread.sleep(2000);

        // repeat test just for kix
        start = System.currentTimeMillis();
        create(provider, matches);
        System.out.printf("%,d millis create%n", System.currentTimeMillis()-start);



    }

    private void reset(List<Match> matches) {
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.get(i).reset();
        }

        //use results
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }
    }

    private void newMatch(Provider provider, List<Match> matches) {
        // jvm warm up
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.get(i).setResult(new Match(provider).getResult());
        }
        // use results
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }
    }

    private void create(Provider provider, List<Match> matches) {
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.get(i).setResult(provider.createMatch().getResult());
        }
        // use the results so that the foregoing doesn't get optimised out
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }
    }
}
