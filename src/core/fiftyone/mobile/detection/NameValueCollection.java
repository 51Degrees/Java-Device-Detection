package fiftyone.mobile.detection;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NameValueCollection {
    private final List<NameValue> collection;
    
    public final List<String> keys;
    
    public NameValueCollection() {
        collection = new ArrayList<NameValue>();
        
        keys = new ArrayList<String>();
    }
    
    public NameValue get(String key) {
        for (NameValue nv : collection) {
            if (nv.getKey().equals(key))
                return nv;
        }
        return null;
    }
}
