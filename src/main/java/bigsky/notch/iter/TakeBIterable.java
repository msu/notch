package bigsky.notch.iter;

public class TakeBIterable<T> implements BetterIterable<T> {
    protected final Iterable<T> iterable;
    protected final int n;

    public TakeBIterable(Iterable<T> iterable, int n) {
        this.iterable = iterable;
        this.n = n;
    }

    @Override
    public BIterator<T> iterator() {
        return new TakeBIterator<>(iterable.iterator(), n);
    }
}
