/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection;

/**
 *
 * @author tom
 */
public class AutoUpdateException extends Exception {
    private final String message;
    
    public AutoUpdateException(final String message){
        super();
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
