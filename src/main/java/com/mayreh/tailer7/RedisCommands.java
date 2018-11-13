package com.mayreh.tailer7;

import java.util.List;

interface RedisCommands {
    Long zadd(String key, double score, LogLine logLine);
    List<LogLine> zrange(String key, long start, long stop);
    Boolean expire(String key, long expireSeconds);
}
