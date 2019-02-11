package com.mayreh.tailer7;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LogTailerConfig {
    public static final long DELAY_MILLIS_DEFAULT = 500L;

    public enum StartMode {
        /**
         * Read previous lines when subscription starts
         */
        GO_BACK,

        /**
         * Only reads newly arrived lines
         */
        LATEST,
    }

    /**
     * Specify polling delay
     */
    @Builder.Default
    long delayMillis = DELAY_MILLIS_DEFAULT;

    /**
     * Specify the line offset the tailer read from
     * Negative number (- n) means read from last n lines
     * Available only startMode is GO_BACK
     */
    @Builder.Default
    int startOffset = 0;

    /**
     * Specify the behavior when subscription starts
     */
    @Builder.Default
    StartMode startMode = StartMode.GO_BACK;
}
