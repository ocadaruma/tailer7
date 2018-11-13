# Example
## Usage

First, you have to start redis on 127.0.0.1:6379

```bash
$ redis-server
```

### log sender

```bash
$ ./gradlew :tailer7-example:run --args "sender key /path/to/file"
```

### log tailer

```bash
$ ./gradlew :tailer7-example:run --args "tailer key"
```
