package com.mayreh.tailer7;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LogTailerConfig {
    public static final long DELAY_MILLIS_DEFAULT = 500L;

    /**
     * Specify polling delay
     */
    @Builder.Default
    long delayMillis = DELAY_MILLIS_DEFAULT;
}
