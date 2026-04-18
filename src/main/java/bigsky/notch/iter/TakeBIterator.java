package bigsky.notch.iter;

import java.util.Iterator;

public class TakeBIterator<T> implements BIterator<T> {
    protected final Iterator<T> iterator;
    protected final int n;

    public  TakeBIterator(Iterator<T> iterator, int n) {
        this.iterator = iterator;
        this.n = n;
    }

    protected int taken = 0;

    @Override
    public boolean hasNext() {
        if (taken >= n) return false;
        return iterator.hasNext();
    }

    @Override
    public T next() {
        T value = iterator.next();
        taken++;
        return value;
    }
}
