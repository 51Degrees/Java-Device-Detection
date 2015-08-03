/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection;

import java.util.Iterator;

/**
 * Interface used for iterators that have resources which need to be freed
 * when the iteration has completed.
 * @author 51Degrees-Products
 * @param <T> type of entity to iterate.
 */
public interface IDisposableIterator<T> extends Iterator<T> {
    /**
     * Ensures any resources used by the iterator are explicitly released.
     */
    public void dispose();
}
