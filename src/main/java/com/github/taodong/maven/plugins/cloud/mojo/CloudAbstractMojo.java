package com.github.taodong.maven.plugins.cloud.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

public abstract class CloudAbstractMojo extends AbstractMojo {
    @Component
    protected MojoExecution execution;

    /**
     * File folder to store build tools of specified version
     */
    @Parameter(property = "envToolDir", required = true)
    protected File envToolDir;

    protected boolean isPhase(String mvnPhase) {
        String phase = execution.getLifecyclePhase();
        return mvnPhase != null && mvnPhase.equalsIgnoreCase(phase);
    }
}
