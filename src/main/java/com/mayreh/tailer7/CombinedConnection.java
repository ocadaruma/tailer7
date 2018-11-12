package com.mayreh.tailer7;

import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.cluster.pubsub.api.sync.RedisClusterPubSubCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;

/**
 * Combines two types of connections for convenience
 */
class CombinedConnection implements AutoCloseable {
    private final RedisClusterClient clusterClient;
    private final LogLineCodec codec = new LogLineCodec();

    private StatefulRedisClusterConnection<String, LogLine> clusterConnection;
    private StatefulRedisClusterPubSubConnection<String, LogLine> pubSubConnection;

    private boolean isOpen = false;

    CombinedConnection(RedisClusterClient clusterClient) {
        this.clusterClient = clusterClient;
    }

    public void open() {
        if (isOpen) {
            return;
        }

        try {
            clusterConnection = clusterClient.connect(codec);
            pubSubConnection = clusterClient.connectPubSub(codec);
            isOpen = true;
        } catch (RuntimeException e) {
            // We have to release only clusterConnection
            // because pubSubConnection will be null if connectPubSub() fails
            if (clusterConnection != null) {
                clusterConnection.close();
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

    public RedisAdvancedClusterCommands<String, LogLine> clusterCommands() {
        if (!isOpen) {
            throw new IllegalStateException("connection is not open");
        }

        return clusterConnection.sync();
    }

    public RedisClusterPubSubCommands<String, LogLine> pubSubCommands() {
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
            if (clusterConnection != null) {
                clusterConnection.close();
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
