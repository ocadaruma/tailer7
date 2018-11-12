package com.mayreh.tailer7;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LogSenderConfig {
    public static final long EXPIRE_SECONDS_DEFAULT = 3600L * 3; // 3 hours

    @Builder.Default
    long expireSeconds = EXPIRE_SECONDS_DEFAULT;
}
