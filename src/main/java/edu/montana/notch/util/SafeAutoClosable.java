package edu.montana.notch.util;

public interface SafeAutoClosable extends AutoCloseable {
    @Override
    void close();
}
