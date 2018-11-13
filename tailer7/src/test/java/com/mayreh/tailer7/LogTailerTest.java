package com.mayreh.tailer7;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

public class LogTailerTest {

    private RedisClient client;
    private ExecutorService subscriptionExecutor;

    @Before
    public void setup() {
        subscriptionExecutor = Executors.newSingleThreadExecutor();

        client = RedisClient.create(RedisURI.create("127.0.0.1", 6379));
        client.connect().sync().del("key");
    }

    @After
    public void teardown() {
        subscriptionExecutor.shutdown();
    }

    private Clock mockClock() {
        return new Clock() {
            private long millis = 0;

            @Override
            public ZoneId getZone() {
                return null;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return null;
            }

            @Override
            public Instant instant() {
                return null;
            }
            @Override
            public long millis() {
                return millis++;
            }
        };
    }

    @Test
    public void testSendAndTail() throws Exception {

        try (LogSender sender = new LogSender(client, LogSenderConfig.builder().build(), mockClock())) {

            List<LogLine> received = new ArrayList<>();
            LogTailer tailer = new LogTailer(client, LogTailerConfig.builder().build(), received::add);

            sender.open();

            // send log in advance
            sender.send("key", "first line");
            sender.send("key", "second line");

            subscriptionExecutor.execute(() -> {
                try {
                    tailer.subscribe("key");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            // wait subscription
            Thread.sleep(100L);

            // send log after subscription
            sender.send("key", "third line");

            // wait consumption
            Thread.sleep(100L);

            tailer.stop();

            assertThat(received).isEqualTo(Arrays.asList(
                    new LogLine(0, "first line"),
                    new LogLine(1, "second line"),
                    new LogLine(2, "third line")
            ));
        }
    }

    @Test
    public void testSendAndTailWhenNoNewMessage() throws Exception {

        try (LogSender sender = new LogSender(client, LogSenderConfig.builder().build(), mockClock())) {

            List<LogLine> received = new ArrayList<>();
            LogTailer tailer = new LogTailer(client, LogTailerConfig.builder().build(), received::add);

            sender.open();

            // send log in advance
            sender.send("key", "first line");
            sender.send("key", "second line");
            sender.send("key", "third line");

            subscriptionExecutor.execute(() -> {
                try {
                    tailer.subscribe("key");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            // wait consumption
            Thread.sleep(100L);

            tailer.stop();

            assertThat(received).isEqualTo(Arrays.asList(
                    new LogLine(0, "first line"),
                    new LogLine(1, "second line"),
                    new LogLine(2, "third line")
            ));
        }
    }

    @Test
    public void testTailFromLast() throws Exception {

        try (LogSender sender = new LogSender(client, LogSenderConfig.builder().build(), mockClock())) {

            List<LogLine> received = new ArrayList<>();
            LogTailer tailer = new LogTailer(client, LogTailerConfig.builder().readFromStart(false).build(), received::add);

            sender.open();

            // send log in advance
            sender.send("key", "first line");
            sender.send("key", "second line");

            subscriptionExecutor.execute(() -> {
                try {
                    tailer.subscribe("key");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            // wait subscription
            Thread.sleep(100L);

            sender.send("key", "third line");

            // wait consumption
            Thread.sleep(100L);

            tailer.stop();

            assertThat(received).isEqualTo(Collections.singletonList(
                    new LogLine(2, "third line")
            ));
        }
    }
}
