/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package API;

import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mike
 */
public class PatternV31 extends Base {
    
    public PatternV31() throws IOException {
        provider = provider = new Provider(StreamFactory.create("D:\\Workspace\\Java-Device-Detection\\data\\51Degrees-LiteV3.1.dat"));
    }
    
}
