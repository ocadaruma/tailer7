package com.mayreh.tailer7;

import io.lettuce.core.pubsub.RedisPubSubListener;

interface RedisPubSubConnection extends Closeable {
    RedisPubSubCommands sync();
    void addListener(RedisPubSubListener<String, LogLine> listener);
}
