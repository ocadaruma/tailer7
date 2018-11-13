package com.mayreh.tailer7;

import io.lettuce.core.RedisClient;
import io.lettuce.core.cluster.RedisClusterClient;

/**
 * Provides feature to publish log
 * This class is intended to use with local file tailer such as Apache Commons IO Tailer
 */
public class LogSender implements Closeable {
    private final CombinedConnection connection;
    private final LogSenderConfig config;

    private RedisCommands commands;
    private RedisPubSubCommands pubSubCommands;

    public LogSender(RedisClusterClient client, LogSenderConfig config) {
        this.connection = new CombinedConnection(new RedisClients.Cluster(client));
        this.config = config;
    }

    public LogSender(RedisClient client, LogSenderConfig config) {
        this.connection = new CombinedConnection(new RedisClients.Standalone(client));
        this.config = config;
    }

    public void open() {
        connection.open();

        commands = connection.commands();
        pubSubCommands = connection.pubSubCommands();
    }

    public void send(String key, LogLine logLine) {
        commands.zadd(key, (double)logLine.getLineNum(), logLine);
        commands.expire(key, config.getExpireSeconds());

        pubSubCommands.publish(key, logLine);
    }

    @Override
    public void close() {
        connection.close();
    }
}
