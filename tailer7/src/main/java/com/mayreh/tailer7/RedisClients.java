package com.mayreh.tailer7;

import io.lettuce.core.Range;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.cluster.pubsub.api.sync.RedisClusterPubSubCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.RequiredArgsConstructor;

import java.util.List;

class RedisClients {

    @RequiredArgsConstructor
    static class Standalone implements RedisClient {
        private final LogLineCodec codec = new LogLineCodec();
        private final io.lettuce.core.RedisClient delegate;

        @Override
        public RedisConnection connect() {
            return new Connection(delegate.connect(codec));
        }

        @Override
        public RedisPubSubConnection connectPubSub() {
            return new PubSubConnection(delegate.connectPubSub(codec));
        }

        @RequiredArgsConstructor
        static class Commands implements RedisCommands {
            private final io.lettuce.core.api.sync.RedisCommands<String, LogLine> delegate;

            @Override
            public Long zadd(String key, double score, LogLine logLine) {
                return delegate.zadd(key, score, logLine);
            }

            @Override
            public List<LogLine> zrange(String key, long start, long stop) {
                return delegate.zrange(key, start, stop);
            }

            @Override
            public Long zcount(String key, Range<? extends Number> range) {
                return delegate.zcount(key, range);
            }

            @Override
            public Boolean expire(String key, long expireSeconds) {
                return delegate.expire(key, expireSeconds);
            }
        }

        @RequiredArgsConstructor
        static class PubSubCommands implements RedisPubSubCommands {
            private final io.lettuce.core.pubsub.api.sync.RedisPubSubCommands<String, LogLine> delegate;

            @Override
            public void subscribe(String... channels) {
                delegate.subscribe(channels);
            }

            @Override
            public Long publish(String key, LogLine logLine) {
                return delegate.publish(key, logLine);
            }
        }

        @RequiredArgsConstructor
        static class Connection implements RedisConnection {
            private final StatefulRedisConnection<String, LogLine> delegate;

            @Override
            public RedisCommands sync() {
                return new Commands(delegate.sync());
            }

            @Override
            public void close() {
                delegate.close();
            }
        }

        @RequiredArgsConstructor
        static class PubSubConnection implements RedisPubSubConnection {
            private final StatefulRedisPubSubConnection<String, LogLine> delegate;

            @Override
            public RedisPubSubCommands sync() {
                return new PubSubCommands(delegate.sync());
            }

            @Override
            public void addListener(RedisPubSubListener<String, LogLine> listener) {
                delegate.addListener(listener);
            }

            @Override
            public void close() {
                delegate.close();
            }
        }
    }

    @RequiredArgsConstructor
    static class Cluster implements RedisClient {
        private final LogLineCodec codec = new LogLineCodec();
        private final RedisClusterClient delegate;

        @Override
        public RedisConnection connect() {
            return new Connection(delegate.connect(codec));
        }

        @Override
        public RedisPubSubConnection connectPubSub() {
            return new PubSubConnection(delegate.connectPubSub(codec));
        }

        @RequiredArgsConstructor
        static class Commands implements RedisCommands {
            private final RedisAdvancedClusterCommands<String, LogLine> delegate;

            @Override
            public Long zadd(String key, double score, LogLine logLine) {
                return delegate.zadd(key, score, logLine);
            }

            @Override
            public List<LogLine> zrange(String key, long start, long stop) {
                return delegate.zrange(key, start, stop);
            }

            @Override
            public Long zcount(String key, Range<? extends Number> range) {
                return delegate.zcount(key, range);
            }

            @Override
            public Boolean expire(String key, long expireSeconds) {
                return delegate.expire(key, expireSeconds);
            }
        }

        @RequiredArgsConstructor
        static class PubSubCommands implements RedisPubSubCommands {
            private final RedisClusterPubSubCommands<String, LogLine> delegate;

            @Override
            public void subscribe(String... channels) {
                delegate.subscribe(channels);
            }

            @Override
            public Long publish(String key, LogLine logLine) {
                return delegate.publish(key, logLine);
            }
        }

        @RequiredArgsConstructor
        static class Connection implements RedisConnection {
            private final StatefulRedisClusterConnection<String, LogLine> delegate;

            @Override
            public RedisCommands sync() {
                return new Commands(delegate.sync());
            }

            @Override
            public void close() {
                delegate.close();
            }
        }

        @RequiredArgsConstructor
        static class PubSubConnection implements RedisPubSubConnection {
            private final StatefulRedisClusterPubSubConnection<String, LogLine> delegate;

            @Override
            public RedisPubSubCommands sync() {
                return new PubSubCommands(delegate.sync());
            }

            @Override
            public void addListener(RedisPubSubListener<String, LogLine> listener) {
                delegate.addListener(listener);
            }

            @Override
            public void close() {
                delegate.close();
            }
        }
    }
}
