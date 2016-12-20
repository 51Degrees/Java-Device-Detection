package fiftyone.mobile.detection;

import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.MatchState;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.factories.MemoryFactory;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author jo
 */
public class MatchTest {
    private static final int testnum = 1000000;

    @Test
    public void testCreate() throws IOException, InterruptedException {
        long start;
        Provider provider = new Provider(MemoryFactory.create(new FileInputStream("../data/51Degrees-LiteV3.2.dat")));
        List<Match> matches= new ArrayList<Match>(testnum);
        for (int i=0; i< testnum; i++) {
            matches.add(provider.createMatch());
        }
        System.gc();
        Thread.sleep(2000);
        matches.clear();
        start = System.currentTimeMillis();
        for (int i=0; i< testnum; i++) {
            matches.add(provider.createMatch());
        }
        System.out.printf("%,d millis to create%n", System.currentTimeMillis()-start);

        for (int i = 0; i< testnum; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }

        for (int i=0; i< testnum; i++) {
            matches.get(i).setResult(new MatchResult());
        }


        for (int i = 0; i< testnum; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }

        System.gc();
        Thread.sleep(2000);


        start = System.currentTimeMillis();
        for (int i=0; i< testnum; i++) {
            matches.get(i).setResult(new MatchResult());
        }

        System.out.printf("%,d millis to create%n", System.currentTimeMillis()-start);
        for (int i = 0; i< testnum; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }


        for (int i=0; i< testnum; i++) {
            matches.get(i).reset();
        }
        for (int i=0; i< testnum; i++) {
            matches.add(provider.createMatch());
        }
        start = System.currentTimeMillis();
        for (int i=0; i< testnum; i++) {
            matches.get(i).reset();
        }
        System.out.printf("%,d millis to reset%n", System.currentTimeMillis()-start);

        for (int i = 0; i< testnum; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }

        System.gc();
        Thread.sleep(2000);

        start = System.currentTimeMillis();
        for (int i=0; i< testnum; i++) {
            matches.get(i).reset();
        }
        System.out.printf("%,d millis to reset%n", System.currentTimeMillis()-start);

        for (int i = 0; i< testnum; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }

        System.gc();
        Thread.sleep(2000);


        start = System.currentTimeMillis();
        for (int i=0; i< testnum; i++) {
            matches.get(i).setResult(new MatchResult());
        }

        System.out.printf("%,d millis to create%n", System.currentTimeMillis()-start);
        for (int i = 0; i< testnum; i++) {
            if (matches.get(i).cookie!=null) {
                System.out.println("po");
            }
        }

    }
}
