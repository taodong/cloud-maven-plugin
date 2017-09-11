package com.github.taodong.maven.plugins.cloud.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum OSFormat {

    BIT32("386"),
    BIT64("amd64"),
    ARM("arm"),
    ARM64("arm64"),
    UNKNOWN("UNKNOWN");

    private String format;

    OSFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public static OSFormat getOSFormat(final String osFormat) {
        return Arrays.stream(values()).filter(
                value -> StringUtils.equalsIgnoreCase(value.getFormat(), osFormat)
        ).findFirst().orElse(OSFormat.UNKNOWN);
    }
}
