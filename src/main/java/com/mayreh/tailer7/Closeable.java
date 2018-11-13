package com.mayreh.tailer7;

/**
 * Closeable interface without checked Exception
 */
public interface Closeable extends AutoCloseable {
    @Override
    void close();
}
