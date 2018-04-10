package com.github.taodong.maven.plugins.cloud.finder;

import java.util.Map;

public class CloudVariable {
    private String name;
    private String value;
    private String type;
    private boolean required = false;
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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
