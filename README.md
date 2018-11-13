# tailer7

A scalable tailer powered by Redis.

## Installation

### Maven

```
<dependency>
  <groupId>com.mayreh</groupId>
  <artifactId>tailer7</artifactId>
  <version>0.0.1</version>
</dependency>
```

### Gradle

```
compile 'com.mayreh:tailer7:0.0.1'
```

## Usage

See also [Example](https://github.com/ocadaruma/tailer7/tree/master/tailer7-example).

### LogSender

```java
RedisClient client = RedisClient.create(RedisURI.create("127.0.0.1", 6379));
LogSender sender = new LogSender(client, LogSenderConfig.builder().build());

sender.open();
sender.send("foo", "seeeeeeennnnnnnd");
```

### LogTailer

```java
RedisClient client = RedisClient.create(RedisURI.create("127.0.0.1", 6379));
LogTailer tailer = new LogTailer(
        client,
        LogTailerConfig.builder().build(),
        log -> System.out.println(log.getLine()));

tailer.subscribe("foo");
// => will output "seeeeeeennnnnnnd"
```
