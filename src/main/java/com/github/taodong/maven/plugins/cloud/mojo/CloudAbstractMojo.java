package com.github.taodong.maven.plugins.cloud.mojo;

import com.github.taodong.maven.plugins.cloud.finder.CloudTool;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class CloudAbstractMojo extends AbstractMojo {
    protected static final String DEFAULT_VERSION = "SYSTEM";

    protected static final String VIRTUAL_ENV ="VIRENV";

    protected static final String TARGET = "target";

    protected static final String SRC = "src";

    protected static final String TEMP = "temp";

    protected static final Map<CloudTool, Map<String, String>> cloudVariables = new HashMap<>();

    @Component
    protected MojoExecution execution;

    /**
     * File folder to store build tools of specified version
     */
    @Parameter(property = "envToolDir", required = true)
    protected File envToolDir;

    /**
     * The folder all the cloud configuration files reside
     */
    @Parameter(property = "buildFolder", required = false, defaultValue = "")
    protected String buildFolder;


    protected boolean isPhase(String mvnPhase) {
        String phase = execution.getLifecyclePhase();
        return mvnPhase != null && mvnPhase.equalsIgnoreCase(phase);
    }

    protected synchronized boolean saveCloudVariable(final String toolName, final String variableName, final String variableValue) {
        CloudTool cloudTool = CloudTool.getCloudToolByName(toolName);
        if (cloudTool != null && cloudTool != CloudTool.UNKNOWN) {
            Map<String, String> valueMap = cloudVariables.get(cloudTool);
            if (valueMap == null) {
                valueMap = new HashMap<>();
                cloudVariables.put(cloudTool, valueMap);
            }

            valueMap.put(variableName, variableValue);
            return true;
        } else {
            return false;
        }
    }
}
