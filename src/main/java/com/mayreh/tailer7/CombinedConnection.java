package com.mayreh.tailer7;

import io.lettuce.core.pubsub.RedisPubSubListener;

/**
 * Combines two types of connections for convenience
 */
class CombinedConnection implements Closeable {
    private final RedisClient client;

    private RedisConnection connection;
    private RedisPubSubConnection pubSubConnection;

    private boolean isOpen = false;

    CombinedConnection(RedisClient client) {
        this.client = client;
    }

    public void open() {
        if (isOpen) {
            return;
        }

        try {
            connection = client.connect();
            pubSubConnection = client.connectPubSub();
            isOpen = true;
        } catch (RuntimeException e) {
            // We do not have to release pubSubConnection
            // because pubSubConnection will be null if connectPubSub() fails
            if (connection != null) {
                connection.close();
            }
            throw e;
        }
    }

    public void addListener(RedisPubSubListener<String, LogLine> listener) {
        if (!isOpen) {
            throw new IllegalStateException("connection is not open");
        }

        pubSubConnection.addListener(listener);
    }

    public RedisCommands commands() {
        if (!isOpen) {
            throw new IllegalStateException("connection is not open");
        }

        return connection.sync();
    }

    public RedisPubSubCommands pubSubCommands() {
        if (!isOpen) {
            throw new IllegalStateException("connection is not open");
        }

        return pubSubConnection.sync();
    }

    @Override
    public void close() {
        if (!isOpen) {
            return;
        }

        try {
            if (connection != null) {
                connection.close();
            }
            if (pubSubConnection != null) {
                pubSubConnection.close();
            }
        } catch (RuntimeException e) {
            if (pubSubConnection != null) {
                pubSubConnection.close();
            }
            throw e;
        }
    }
}
