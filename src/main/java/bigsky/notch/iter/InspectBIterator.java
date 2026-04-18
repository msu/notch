package bigsky.notch.iter;

import java.util.Iterator;
import java.util.function.Consumer;

public class InspectBIterator<T> implements BIterator<T> {
    protected final Iterator<T> iterator;
    protected final Consumer<T> inspector;

    public InspectBIterator(Iterator<T> iterator, Consumer<T> inspector) {
        this.iterator = iterator;
        this.inspector = inspector;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        T value =  iterator.next();
        inspector.accept(value);
        return value;
    }
}
