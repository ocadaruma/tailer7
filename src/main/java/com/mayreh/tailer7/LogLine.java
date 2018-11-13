package com.mayreh.tailer7;

import lombok.Data;
import lombok.Value;

/**
 * Represents a line in a log file
 */
@Value
public class LogLine {
    long epochMillis;
    String line;

    static LogLine fromData(LogLineData log) {
        return new LogLine(log.epochMillis, log.line);
    }

    LogLineData toData() {
        LogLineData log = new LogLineData();
        log.setEpochMillis(epochMillis);
        log.setLine(line);

        return log;
    }

    /**
     * Used to sort log lines in Redis sorted set
     */
    double score() {
        return (double)epochMillis / 1000.0;
    }

    @Data
    static class LogLineData {
        long epochMillis;
        String line;
    }
}
