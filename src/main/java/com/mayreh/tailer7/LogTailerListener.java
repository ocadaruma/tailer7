package com.mayreh.tailer7;

/**
 * A callback when log sent
 */
public interface LogTailerListener {
    void onSent(LogLine logLine);
}
