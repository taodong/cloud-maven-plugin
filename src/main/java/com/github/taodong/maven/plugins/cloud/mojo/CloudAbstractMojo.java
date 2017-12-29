package com.github.taodong.maven.plugins.cloud.mojo;

import com.github.taodong.maven.plugins.cloud.finder.CloudTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class CloudAbstractMojo extends AbstractMojo {
    protected static final String DEFAULT_VERSION = "SYSTEM";

    protected static final String TARGET = "target";

    protected static final String SRC = "src";

    protected static final String TEMP = "temp";

    protected static final Map<CloudTool, Map<String, String>> cloudVariables = new HashMap<>();


    /**
     * File folder to store build tools of specified version
     */
    @Parameter(property = "cloud.envToolDir", required = true)
    protected File envToolDir;

    /**
     * The folder all the cloud configuration files reside
     */
    @Parameter(property = "cloud.buildFolder", defaultValue = "")
    protected String buildFolder;

    /**
     * The executor to build the cloud: the value should be one of following: packer, ansible, terraform, terragrunt
     */
    @Parameter(property = "cloud.executor", required = true)
    protected String cloudExe;

    /**
     * Only generates linux shell script when true, otherwise plug in will try to run commands directly through desired executor
     */
    @Parameter(property = "cloud.genScriptOnly", defaultValue = "true")
    protected boolean genScriptOnly;

    /**
     * file contains beginning part of customized shell script, only used when genScriptOnly equals true
     */
    @Parameter(property = "cloud.scriptHead")
    protected File customScriptHead;

    /**
     * file contains ending part of customized shell script, only used when genScriptOnly equals true
     */
    @Parameter(property = "cloud.scriptTail")
    protected File customScriptTail;


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

    protected boolean matchCloudBuilder(final String curExecutor) {
        return StringUtils.equalsIgnoreCase(StringUtils.trim(cloudExe), StringUtils.trimToNull(curExecutor));
    }
}
