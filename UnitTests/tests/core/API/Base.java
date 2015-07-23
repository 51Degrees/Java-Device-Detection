/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package API;

import fiftyone.mobile.detection.Match;
import static fiftyone.mobile.detection.MatchMethods.EXACT;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mike
 */
public abstract class Base {
    
    protected static Provider provider;
    protected static Match match;
    
    public Base() throws IOException {
    }
    
    @Test
    public void providerInitTest() {
        assertTrue(provider != null);
    }
    
    @Test
    public void datasetnameTest() throws IOException {
        assertTrue(provider.dataSet.getName().equals("Lite"));
    }
    
    @Test
    public void numberOfPropertiesTest() {
        assertTrue(provider.dataSet.getProperties().size() > 50
        && provider.dataSet.getProperties().size() < 80);
    }
    
    @Test
    public void testKnownUserAgent() throws IOException {
        String userAgent = "Mozilla/5.0 (BlackBerry; U; BlackBerry 9900; en) "
                + "AppleWebKit/534.11+ (KHTML, like Gecko) Version/7.1.0.346 "
                + "Mobile Safari/534.11+";
        match = provider.match(userAgent);
        assertTrue(match.method == EXACT);
    }
    
    @Test
    public void testKnownUserAgentProperties() {
        
    }

    @Test
    public void testNullUserAgent() throws IOException {
        match = null;
        match = provider.match((String)null);
        assertTrue(match != null);
    }
    
}
