package com.github.taodong.maven.plugins.cloud.utils;

import com.github.taodong.maven.plugins.cloud.finder.CloudVariable;

import java.util.List;
import java.util.Map;

public class CloudVariableConfig {
    private String source;
    private String format;
    private List<CloudVariable> variables;
    private Map<String, String> sourceConfig;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<CloudVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<CloudVariable> variables) {
        this.variables = variables;
    }

    public Map<String, String> getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(Map<String, String> sourceConfig) {
        this.sourceConfig = sourceConfig;
    }
}
