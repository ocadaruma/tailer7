package com.mayreh.tailer7;

import io.lettuce.core.RedisClient;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Provides feature to tail log from Redis
 */
@Slf4j
public class LogTailer {
    private final com.mayreh.tailer7.RedisClient client;
    private final LogTailerConfig config;
    private final LogTailerListener listener;

    private final LinkedBlockingQueue<LogLine> queue = new LinkedBlockingQueue<>();
    private volatile boolean subscribing = false;

    public LogTailer(
            RedisClusterClient client,
            LogTailerConfig config,
            LogTailerListener listener) {
        this.client = new RedisClients.Cluster(client);
        this.config = config;
        this.listener = listener;
    }

    public LogTailer(
            RedisClient client,
            LogTailerConfig config,
            LogTailerListener listener) {
        this.client = new RedisClients.Standalone(client);
        this.config = config;
        this.listener = listener;
    }

    /**
     * Once subscription started, the thread blocks until closed
     * Hence subscription should be started in a separate thread
     */
    public void subscribe(String key) throws InterruptedException {
        try (CombinedConnection connection = new CombinedConnection(client)) {

            connection.open();

            RedisCommands commands = connection.commands();
            RedisPubSubCommands pubSubCommands = connection.pubSubCommands();

            final long[] currentTimestamp = {-1};

            connection.addListener(new RedisPubSubListener<String, LogLine>() {
                @Override
                public void message(String channel, LogLine message) {
                    log.debug("on message. channel: {}, message: {}", channel, message);

                    if (channel.equals(key) && currentTimestamp[0] < message.getEpochMillis()) {
                        queue.offer(message);
                        currentTimestamp[0] = message.getEpochMillis();
                    }
                }

                @Override
                public void message(String pattern, String channel, LogLine message) {
                    log.debug("on message. pattern: {}, channel: {}, message: {}", pattern, channel, message);
                }

                @Override
                public void subscribed(String channel, long count) {
                    log.debug("subscribed to channel: {}, count: {}", channel, count);

                    if (channel.equals(key)) {
                        // for the first time
                        List<LogLine> previousLines =
                                commands.zrange(key, 0, -1);

                        for (LogLine line : previousLines) {
                            queue.offer(line);
                            currentTimestamp[0] = line.getEpochMillis();
                        }
                    }
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

            while (subscribing) {
                LogLine line = queue.poll(config.getDelayMillis(), TimeUnit.MILLISECONDS);
                if (line != null) {
                    listener.onSent(line);
                }
            }
        }
    }

    public void stop() {
        subscribing = false;
    }
}
