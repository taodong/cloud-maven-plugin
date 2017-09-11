package com.github.taodong.maven.plugins.cloud.utils;

import java.util.Arrays;

/**
 *
 */
public enum OS {
    MAC("Mac", "darwin"),
    FREEBSD("FreeBSD", "freebsd"),
    LINUX("Linux", "linux"),
    OPENBSD("OpenBSD", "openbsd"),
    WINDOWS("Windows", "windows"),
    UNKNOWN("Unknown", "Unknown")
    ;

    private String name;
    private String packageName;

    OS(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public static OS getOS(final String osName) {
        return Arrays.stream(values()).filter(
                value -> value.getName().equalsIgnoreCase(osName)
        ).findFirst().orElse(OS.UNKNOWN);
    }
}
