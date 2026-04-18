package bigsky.notch.iter;

public class JavaBIterable<T> implements BetterIterable<T> {
    protected Iterable<T> inner;

    public JavaBIterable(Iterable<T> inner) {
        this.inner = inner;
    }

    @Override
    public BIterator<T> iterator() {
        return new JavaBIterator<>(inner.iterator());
    }
}
