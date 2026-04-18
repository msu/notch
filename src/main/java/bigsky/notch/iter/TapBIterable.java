package bigsky.notch.iter;

import java.util.function.Consumer;

public class TapBIterable<T> implements BetterIterable<T> {
    protected final Iterable<T> iterable;
    protected final Consumer<T> consumer;

    public TapBIterable(Iterable<T> iterable, Consumer<T> consumer) {
        this.iterable = iterable;
        this.consumer = consumer;
    }

    @Override
    public BIterator<T> iterator() {
        return new InspectBIterator<>(iterable.iterator(), consumer);
    }
}
