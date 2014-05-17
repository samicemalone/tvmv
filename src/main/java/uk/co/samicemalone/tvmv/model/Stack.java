/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.samicemalone.tvmv.model;

/**
 *
 * @author Sam Malone
 * @param <E>
 */
public interface Stack<E> {
    void push(E element);
    E pop();
    E peek();
}
