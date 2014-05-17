/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.samicemalone.tvmv.model;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author Sam Malone
 * @param <E>
 */
public class DequeStack<E> implements Stack<E>{
    
    private final Deque<E> stack;

    public DequeStack() {
        this.stack = new ArrayDeque<>();
    }

    public DequeStack(int numInitialElements) {
        this.stack = new ArrayDeque<>(numInitialElements);
    }

    @Override
    public void push(E element) {
        stack.addFirst(element);
    }

    @Override
    public E pop() {
        return stack.pollFirst();
    }

    @Override
    public E peek() {
        return stack.peekFirst();
    }
    
    public boolean isEmpty() {
        return stack.isEmpty();
    }
    
}
