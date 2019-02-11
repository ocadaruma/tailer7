package com.mayreh.tailer7;

import io.lettuce.core.Range;

import java.util.List;

interface RedisCommands {
    Long zadd(String key, double score, LogLine logLine);
    List<LogLine> zrange(String key, long start, long stop);
    Long zcount(String key, Range<? extends Number> range);
    Boolean expire(String key, long expireSeconds);
}
