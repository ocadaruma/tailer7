package com.mayreh.tailer7;

import io.lettuce.core.RedisClient;
import io.lettuce.core.cluster.RedisClusterClient;

import java.time.Clock;

/**
 * Provides feature to publish log
 * This class is intended to use with local file tailer such as Apache Commons IO Tailer
 */
public class LogSender implements Closeable {
    private final CombinedConnection connection;
    private final LogSenderConfig config;
    private final Clock clock;

    private RedisCommands commands;
    private RedisPubSubCommands pubSubCommands;

    public LogSender(RedisClusterClient client, LogSenderConfig config, Clock clock) {
        this.connection = new CombinedConnection(new RedisClients.Cluster(client));
        this.config = config;
        this.clock = clock;
    }

    public LogSender(RedisClient client, LogSenderConfig config, Clock clock) {
        this.connection = new CombinedConnection(new RedisClients.Standalone(client));
        this.config = config;
        this.clock = clock;
    }

    public LogSender(RedisClusterClient client, LogSenderConfig config) {
        this.connection = new CombinedConnection(new RedisClients.Cluster(client));
        this.config = config;
        this.clock = Clock.systemDefaultZone();
    }

    public LogSender(RedisClient client, LogSenderConfig config) {
        this.connection = new CombinedConnection(new RedisClients.Standalone(client));
        this.config = config;
        this.clock = Clock.systemDefaultZone();
    }

    public void open() {
        connection.open();

        commands = connection.commands();
        pubSubCommands = connection.pubSubCommands();
    }

    public void send(String key, String line) {
        LogLine logLine = new LogLine(clock.millis(), line);

        commands.zadd(key, logLine.score(), logLine);
        commands.expire(key, config.getExpireSeconds());

        pubSubCommands.publish(key, logLine);
    }

    @Override
    public void close() {
        connection.close();
    }
}