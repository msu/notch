package bigsky.notch.util;

public interface SafeAutoClosable extends AutoCloseable {
    @Override
    void close();
}
