package com.mayreh.tailer7;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.*;

public class LogLineCodecTest {
    @Test
    public void serde() {
        LogLineCodec codec = new LogLineCodec();
        LogLine log = new LogLine(55301, "abcde");

        assertThat(new String(codec.encodeValue(log).array()))
                .isEqualTo("{\"lineNum\":55301,\"line\":\"abcde\"}");

        assertThat(
                codec.decodeValue(ByteBuffer.wrap("{\"lineNum\":55301,\"line\":\"abcde\"}".getBytes())))
                .isEqualTo(log);
    }
}
