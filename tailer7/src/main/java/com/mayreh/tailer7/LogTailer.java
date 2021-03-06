package com.mayreh.tailer7;

import io.lettuce.core.Range;
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
    private volatile boolean run = true;

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
     * Once subscription started, the thread blocks until stop
     * Hence subscription should be started in a separate thread
     */
    public void subscribe(String key) throws InterruptedException {
        try (CombinedConnection connection = new CombinedConnection(client)) {

            if (!run) {
                return;
            }

            connection.open();

            RedisCommands commands = connection.commands();
            RedisPubSubCommands pubSubCommands = connection.pubSubCommands();

            final int[] currentSequence = {-1};

            connection.addListener(new RedisPubSubListener<String, LogLine>() {
                @Override
                public void message(String channel, LogLine message) {
                    log.debug("on message. channel: {}, message: {}", channel, message);

                    if (channel.equals(key) && currentSequence[0] < message.getSequence()) {
                        queue.offer(message);
                        currentSequence[0] = message.getSequence();
                    }
                }

                @Override
                public void message(String pattern, String channel, LogLine message) {
                    log.debug("on message. pattern: {}, channel: {}, message: {}", pattern, channel, message);
                }

                @Override
                public void subscribed(String channel, long count) {
                    log.debug("subscribed to channel: {}, count: {}", channel, count);

                    if (channel.equals(key) && config.getStartMode() == LogTailerConfig.StartMode.GO_BACK) {
                        // for the first time
                        List<LogLine> previousLines =
                                commands.zrange(key, config.getStartOffset(), -1);

                        for (LogLine line : previousLines) {
                            queue.offer(line);
                            currentSequence[0] = line.getSequence();
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

            while (run) {
                LogLine line = queue.poll(config.getDelayMillis(), TimeUnit.MILLISECONDS);
                if (line != null) {
                    listener.onSent(line);
                }
            }
        }
    }

    public long count(String key) {
        try (RedisConnection connection = this.client.connect()) {
            return connection.sync().zcount(key, Range.unbounded());
        }
    }

    public List<LogLine> getLogs(String key, int start, int end) {
        try (RedisConnection connection = this.client.connect()) {
            return connection.sync().zrange(key, start, end);
        }
    }

    public void stop() {
        run = false;
    }
}
