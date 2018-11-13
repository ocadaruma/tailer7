package com.mayreh.tailer7;

interface RedisPubSubCommands {
    void subscribe(String ...channels);
    Long publish(String key, LogLine logLine);
}
