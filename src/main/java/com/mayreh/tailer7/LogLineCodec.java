package com.mayreh.tailer7;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * encode/decode LogLine using JSON string
 */
@Slf4j
class LogLineCodec implements RedisCodec<String, LogLine> {
    private final RedisCodec<String, String> stringCodec = StringCodec.UTF8;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return stringCodec.decodeKey(bytes);
    }

    @Override
    public LogLine decodeValue(ByteBuffer bytes) {
        try {
            return LogLine.fromMutable(objectMapper.readValue(
                    stringCodec.decodeValue(bytes), LogLine.Mutable.class));
        } catch (IOException e) {
            log.error("failed to decode value", e);
            return null;
        }
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        return stringCodec.encodeKey(key);
    }

    @Override
    public ByteBuffer encodeValue(LogLine value) {
        try {
            return ByteBuffer.wrap(objectMapper.writeValueAsBytes(value.toMutable()));
        } catch (JsonProcessingException e) {
            log.error("failed to encode value", e);
            return null;
        }
    }
}
