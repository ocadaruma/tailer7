package com.mayreh.tailer7;

import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.pubsub.api.sync.RedisClusterPubSubCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Provides feature to tail log from Redis
 */
@Slf4j
public class LogTailer implements AutoCloseable {
    private final CombinedConnection connection;

    private RedisAdvancedClusterCommands<String, LogLine> clusterCommands;
    private RedisClusterPubSubCommands<String, LogLine> pubSubCommands;

    private final LinkedBlockingQueue<LogLine> queue = new LinkedBlockingQueue<>();
    private int currentLineNum = -1;
    private boolean subscribing = false;

    public LogTailer(RedisClusterClient client) {
        this.connection = new CombinedConnection(client);
    }

    public void open() {
        connection.open();

        clusterCommands = connection.clusterCommands();
        pubSubCommands = connection.pubSubCommands();
    }

    public void subscribe(String key, LogTailerListener listener) throws InterruptedException {
        connection.addListener(new RedisPubSubListener<String, LogLine>() {
            @Override
            public void message(String channel, LogLine message) {
                log.debug("on message. channel: {}, message: {}", channel, message);

                if (channel.equals(key)) {
                    // for the first time
                    if (currentLineNum < 0 && message.getLineNum() > 0) {
                        List<LogLine> previousLines =
                                clusterCommands.zrange(key, 0, message.getLineNum() - 1);

                        for (LogLine line : previousLines) {
                            queue.offer(line);
                            currentLineNum++;
                        }
                    }
                    queue.offer(message);
                    currentLineNum++;
                }
            }

            @Override
            public void message(String pattern, String channel, LogLine message) {
                log.debug("on message. pattern: {}, channel: {}, message: {}", pattern, channel, message);
            }

            @Override
            public void subscribed(String channel, long count) {
                log.debug("subscribed to channel: {}, count: {}", channel, count);
            }

            @Override
            public void psubscribed(String pattern, long count) {
                log.debug("psubscribed to pattern: {}, count: {}", pattern, count);
            }

            @Override
            public void unsubscribed(String channel, long count) {
                log.debug("unsubscribed from channel: {}, count: {}", channel, count);
            }

            @Override
            public void punsubscribed(String pattern, long count) {
                log.debug("punsubscribed from pattern: {}, count: {}", pattern, count);
            }
        });

        pubSubCommands.subscribe(key);
        subscribing = true;

        while(subscribing) {
            LogLine line = queue.take();
            listener.onSent(line);
        }
    }

    @Override
    public void close() {
        subscribing = false;
        connection.close();
    }
}
