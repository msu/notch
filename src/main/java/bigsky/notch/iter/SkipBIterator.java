package bigsky.notch.iter;

import java.util.Iterator;

public class SkipBIterator<T> implements BIterator<T> {
    protected final Iterator<T> iterator;
    protected final int n;

    public SkipBIterator(Iterator<T> iterator, int n) {
        this.iterator = iterator;
        this.n = n;
    }

    protected boolean skipped = false;

    @Override
    public boolean hasNext() {
        if (!skipped) {
            for (int i = 0; i < n; i++) {
                if (!iterator.hasNext()) return false;
                iterator.next();
            }
            skipped = true;
        }

        return iterator.hasNext();
    }

    @Override
    public T next() {
        return iterator.next();
    }
}
