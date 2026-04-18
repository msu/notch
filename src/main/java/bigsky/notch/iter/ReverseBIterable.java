package bigsky.notch.iter;

public class ReverseBIterable<T> implements BetterIterable<T> {
    protected final BetterIterable<T> iterable;

    public ReverseBIterable(BetterIterable<T> iterable) {
        this.iterable = iterable;
    }

    @Override
    public BIterator<T> iterator() {
        return new ReverseBIterator<>(iterable.iterator());
    }

    @Override
    public BetterIterable<T> reverse() {
        return iterable;
    }
}
