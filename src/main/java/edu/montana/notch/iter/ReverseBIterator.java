package edu.montana.notch.iter;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ReverseBIterator<T> implements BIterator<T> {
    private final Iterator<T> iterator;

    public ReverseBIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    private ArrayDeque<T> deque;

    @Override
    public boolean hasNext() {
        if (deque == null) {
            deque = new ArrayDeque<>();
            while (iterator.hasNext()) {
                deque.add(iterator.next());
            }
        }

        return !deque.isEmpty();
    }

    @Override
    public T next() {
        if (deque == null) throw new IllegalStateException("hasNext() must be called before calling next()");
        if (deque.isEmpty()) throw new NoSuchElementException();
        return deque.removeLast();
    }
}
