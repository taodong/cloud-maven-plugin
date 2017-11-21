package com.github.taodong.maven.plugins.cloud.finder;

import org.apache.commons.lang3.StringUtils;

public enum CloudTool {
    PACKER,
    ANSIBLE,
    TERRAFORM,
    TERRAGRUNT,
    UNKNOWN;

    public static CloudTool getCloudToolByName(String name) {
        try {
            return CloudTool.valueOf(StringUtils.upperCase(name));
        } catch (Exception e) {
            return CloudTool.UNKNOWN;
        }
    }
}
