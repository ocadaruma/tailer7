package com.mayreh.tailer7;

interface RedisConnection extends AutoCloseable {
    RedisCommands sync();
}
