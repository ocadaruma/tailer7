package com.mayreh.tailer7;

interface RedisPubSubConnection extends AutoCloseable {
    RedisPubSubCommands sync();
    void addListener()
}
