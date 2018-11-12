package com.mayreh.tailer7;

interface RedisPubSubCommands {
    void publish(String key, LogLine logLine);
}
