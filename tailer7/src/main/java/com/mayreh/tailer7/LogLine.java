package com.mayreh.tailer7;

import lombok.Data;
import lombok.Value;

/**
 * Represents a line in a log file
 */
@Value
public class LogLine {
    int sequence;
    String line;

    static LogLine fromData(LogLineData log) {
        return new LogLine(log.sequence, log.line);
    }

    LogLineData toData() {
        LogLineData log = new LogLineData();
        log.setSequence(sequence);
        log.setLine(line);

        return log;
    }

    /**
     * Used to sort log lines in Redis sorted set
     */
    double score() {
        return sequence;
    }

    @Data
    static class LogLineData {
        int sequence;
        String line;
    }
}
