package com.github.taodong.maven.plugins.cloud.finder;

import com.google.common.base.Joiner;

public class NoValueFoundException extends RuntimeException {
    private String variable;

    public NoValueFoundException(String variable) {
        super(Joiner.on(" ").skipNulls().join("Missing value for required variable", variable));
        this.variable = variable;
    }

    public NoValueFoundException(String variable, String message) {
        super(message);
        this.variable = variable;
    }

    public NoValueFoundException(String variable, String message, Throwable cause) {
        super(message, cause);
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }
}
