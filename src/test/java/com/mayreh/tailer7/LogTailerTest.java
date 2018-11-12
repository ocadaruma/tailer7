package com.mayreh.tailer7;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.*;

public class LogTailerTest {

    private RedisClusterClient client;
    private ExecutorService subscriptionExecutor;

    @Before
    public void setup() {
        subscriptionExecutor = Executors.newSingleThreadExecutor();

        client = RedisClusterClient.create(RedisURI.create("127.0.0.1", 6379));
    }

    @Test
    public void testSendAndTail() throws Exception {

        try (
                LogSender sender = new LogSender(client, LogSenderConfig.builder().build());
                LogTailer tailer = new LogTailer(client)
        ) {

            sender.open();
            tailer.open();

            sender.send("key", new LogLine(0, "first line"));
            sender.send("key", new LogLine(1, "second line"));
            sender.send("key", new LogLine(2, "third line"));

            List<LogLine> received = new ArrayList<>();

            Future<?> future = subscriptionExecutor.submit(() -> {
                try {
                    tailer.subscribe("key", received::add);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            Thread.sleep(1000L);

            assertThat(received).isEqualTo(Arrays.asList(
                    new LogLine(0, "first line"),
                    new LogLine(1, "second line"),
                    new LogLine(2, "third line")
            ));
        }
    }
}
