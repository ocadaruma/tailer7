package com.mayreh.tailer7;

import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.pubsub.api.sync.RedisClusterPubSubCommands;

/**
 * Provides feature to publish log
 * This class is intended to use with local file tailer such as Apache Commons IO Tailer
 */
public class LogSender implements AutoCloseable {
    private final CombinedConnection connection;
    private final LogSenderConfig config;

    private RedisAdvancedClusterCommands<String, LogLine> clusterCommands;
    private RedisClusterPubSubCommands<String, LogLine> pubSubCommands;

    public LogSender(RedisClusterClient client, LogSenderConfig config) {
        this.connection = new CombinedConnection(client);
        this.config = config;
    }

    public void open() {
        connection.open();

        clusterCommands = connection.clusterCommands();
        pubSubCommands = connection.pubSubCommands();
    }

    public void send(String key, LogLine logLine) {
        clusterCommands.zadd(key, (double)logLine.getLineNum(), logLine);
        clusterCommands.expire(key, config.getExpireSeconds());

        pubSubCommands.publish(key, logLine);
    }

    @Override
    public void close() {
        connection.close();
    }
}
