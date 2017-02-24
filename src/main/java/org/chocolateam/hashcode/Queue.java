package org.chocolateam.hashcode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Queue<T extends Comparable<T>> {

    private final List<T> queue = new ArrayList<>();


    public void add(T element) {
        queue.add(element);
    }

    public T poll() {
        return queue.remove(queue.size() - 1);
    }

    public T peek() {
        return queue.get(queue.size() - 1);
    }

    public T findEqualElement(T element) {
        int i = queue.indexOf(element);
        if (i < 0) {
            return null;
        }
        return queue.get(i);
    }

    public T remove(T element) {
        int i = queue.indexOf(element);
        if (i < 0) {
            return null;
        }
        return queue.remove(i);
    }

    public void sort() {
        queue.sort(Comparator.naturalOrder());
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void addSorted(T videoInThisCache) {

    }
}
