package com.mayreh.tailer7;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.*;

public class LogLineCodecTest {
    @Test
    public void serde() {
        LogLineCodec codec = new LogLineCodec();
        LogLine log = new LogLine(1542118644469L, "abcde");

        assertThat(new String(codec.encodeValue(log).array()))
                .isEqualTo("{\"epochMillis\":1542118644469,\"line\":\"abcde\"}");

        assertThat(
                codec.decodeValue(ByteBuffer.wrap("{\"epochMillis\":1542118644469,\"line\":\"abcde\"}".getBytes())))
                .isEqualTo(log);
    }
}
