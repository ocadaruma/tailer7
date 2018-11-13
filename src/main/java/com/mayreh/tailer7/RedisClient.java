package com.mayreh.tailer7;

interface RedisClient {
    RedisConnection connect();
    RedisPubSubConnection connectPubSub();
}
