/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.entities;

import java.util.Iterator;

/**
 *
 * @author mike
 * @param <T>
 */
public interface IEnumerable<T extends BaseEntity> extends Iterator<Integer>, Iterable<Integer> {
    
    public abstract IEnumerable<T> getRange(int from, int to);
    
}
