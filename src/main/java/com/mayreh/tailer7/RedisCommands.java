package com.mayreh.tailer7;

interface RedisCommands {
    void zadd(String key, double score, LogLine logLine);
    void expire(String key, long expireSeconds);
}
