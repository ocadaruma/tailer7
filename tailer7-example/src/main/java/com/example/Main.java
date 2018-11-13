package com.example;

import com.mayreh.tailer7.LogSender;
import com.mayreh.tailer7.LogSenderConfig;
import com.mayreh.tailer7.LogTailer;
import com.mayreh.tailer7.LogTailerConfig;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        String app = args[0];

        switch (app) {
            case "sender":
                startSender(args[1], args[2]);
                break;
            case "tailer":
                startTailer(args[1]);
                break;
            default:
                break;
        }
    }

    private static void startSender(String key, String fileName) {
        LogSender sender = new LogSender(createRedisClient(), LogSenderConfig.builder().build());
        sender.open();

        File file = new File(fileName);

        Tailer tailer = new Tailer(file, new TailerListenerAdapter() {
            @Override
            public void handle(String line) {
                System.out.println(line);
                sender.send(key, line);
            }
        }, 100L, false);

        tailer.run();
    }

    private static void startTailer(String key) throws InterruptedException {
        LogTailer tailer = new LogTailer(
                createRedisClient(),
                LogTailerConfig.builder().build(),
                log -> System.out.println(log.getLine()));

        tailer.subscribe(key);
    }

    private static RedisClient createRedisClient() {
        return RedisClient.create(RedisURI.create("127.0.0.1", 6379));
    }
}
