package com.mayreh.tailer7;

import lombok.Data;
import lombok.Value;

/**
 * Represents a line in a log file
 */
@Value
public class LogLine {
    int lineNum;
    String line;

    static LogLine fromMutable(Mutable log) {
        return new LogLine(log.lineNum, log.line);
    }

    Mutable toMutable() {
        Mutable log = new Mutable();
        log.setLineNum(lineNum);
        log.setLine(line);

        return log;
    }

    @Data
    static class Mutable {
        int lineNum;
        String line;
    }
}
