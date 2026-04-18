package bigsky.notch.iter;

import java.util.Iterator;

public class JavaBIterator<T> implements BIterator<T> {
    protected Iterator<T> iterator;

    public JavaBIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        return iterator.next();
    }
}
