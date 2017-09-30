package com.github.taodong.maven.plugins.cloud.finder;

import java.util.List;
import java.util.Map;

public class CloudVariable {
    private String name;
    private String value;
    private Map<String, String> toolVars;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, String> getToolVars() {
        return toolVars;
    }

    public void setToolVars(Map<String, String> toolVars) {
        this.toolVars = toolVars;
    }
}
