package fiftyone.mobile.detection;

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
public class MatchTest {
    private static final int NUMBER_OF_TESTS = 1000000;

    @Test
    public void testCreate() throws IOException, InterruptedException {
        long start;
        Provider provider = new Provider(MemoryFactory.create(new FileInputStream("../data/51Degrees-LiteV3.2.dat")));

        // warm up jvm
        List<Match> matches= new ArrayList<Match>(NUMBER_OF_TESTS);
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.add(provider.createMatch());
        }
        // sleep and gather our breath.
        System.gc();
        Thread.sleep(2000);

        //measure how long it takes to add and create matches
        matches.clear();
        start = System.currentTimeMillis();
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.add(provider.createMatch());
        }
        System.out.printf("%,d millis to create%n", System.currentTimeMillis()-start);

        // use the results so that the foregoing doesn't get optimised out
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }

        // jvm warm up
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.get(i).setResult(new MatchResult());
        }


        // use results
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }

        // sleep and gather our breath.
        System.gc();
        Thread.sleep(2000);


        // measure how long it takes to get from array and add new
        start = System.currentTimeMillis();
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.get(i).setResult(new MatchResult());
        }
        System.out.printf("%,d millis to create%n", System.currentTimeMillis()-start);

        // always use the results
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }


        // warm up
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.get(i).reset();
        }

        start = System.currentTimeMillis();
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.get(i).reset();
        }
        System.out.printf("%,d millis to reset%n", System.currentTimeMillis()-start);

        //use results
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }

        System.gc();
        Thread.sleep(2000);

        // above again
        start = System.currentTimeMillis();
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.get(i).reset();
        }
        System.out.printf("%,d millis to reset%n", System.currentTimeMillis()-start);

        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }

        System.gc();
        Thread.sleep(2000);


        // repeat test just for kix
        start = System.currentTimeMillis();
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            matches.get(i).setResult(new MatchResult());
        }

        System.out.printf("%,d millis to create%n", System.currentTimeMillis()-start);
        for (int i = 0; i< NUMBER_OF_TESTS; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }

    }
}
