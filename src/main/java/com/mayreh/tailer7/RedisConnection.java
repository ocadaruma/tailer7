package com.mayreh.tailer7;

interface RedisConnection extends Closeable {
    RedisCommands sync();
}
