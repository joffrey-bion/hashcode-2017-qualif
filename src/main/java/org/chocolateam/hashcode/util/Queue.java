package org.chocolateam.hashcode.util;

import java.util.Comparator;

public class Queue<T> {

    private final PriorityQueue<T> queue;

    public Queue(Comparator<T> comparator) {
        queue = new PriorityQueue<>(comparator);
    }

    public void add(T element) {
        queue.add(element);
    }

    public T poll() {
        return queue.poll();
    }

    public T peek() {
        return queue.peek();
    }

    public T removeSimilar(T element) {
        return queue.removeSimilar(element);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

}
