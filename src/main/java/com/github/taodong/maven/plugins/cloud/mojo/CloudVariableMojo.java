package com.github.taodong.maven.plugins.cloud.mojo;

import com.github.taodong.maven.plugins.cloud.finder.*;
import com.github.taodong.maven.plugins.cloud.utils.CloudVariableConfig;
import com.github.taodong.maven.plugins.cloud.utils.CloudVariableLoader;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;
import java.util.Map;

@Mojo(name = "variables", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class CloudVariableMojo extends CloudAbstractMojo {
    final String FORMAT_PROPERTY = "properties";
    final String FORMAT_CREDSTAH = "credstash";

    /**
     * File folder to store build tools of specified version
     */
    @Parameter(property = "variable.config", required = true)
    protected File configFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (configFile != null && configFile.exists() && !configFile.isDirectory()) {
            List<CloudVariableConfig> configs = CloudVariableLoader.loadConfig(getLog(), configFile);
            if (configs != null && !configs.isEmpty()) {
                configs.stream().forEach(config -> processConfig(config));
            }
        } else {
            getLog().warn(Joiner.on(" ").skipNulls().join("Skip loading cloud variables: Couldn't read config file", configFile == null ? null : configFile.getAbsolutePath()));
        }
    }

    private void processConfig(final CloudVariableConfig config) {
        String format = config.getFormat();
        if (StringUtils.equalsIgnoreCase(FORMAT_PROPERTY, format)) {
            String source = config.getSource();
            try {
                final PropertyValueFinder valueFinder = new PropertyValueFinder(source);
                List<CloudVariable> variables = config.getVariables();
                if (variables != null && !variables.isEmpty()) {
                    lookupValues(valueFinder, variables);
                }
            } catch (Exception e) {
                getLog().error(Joiner.on(" ").skipNulls().join("Failed to load property file: ", source, "Value lookup skipped"), e);
            }
        } else if (StringUtils.equalsIgnoreCase(FORMAT_CREDSTAH, format)) {
            final CredstashValueFinder valueFinder = new CredstashValueFinder(getLog(), config.getSourceConfig());

            List<CloudVariable> variables = config.getVariables();
            if (variables != null && !variables.isEmpty()) {
                lookupValues(valueFinder, variables);
            }
        } else {
            getLog().error(Joiner.on(" ").skipNulls().join("Un-supported cloud variable config format:", format));
        }

    }

    private void lookupValues(ValueFinder valueFinder, List<CloudVariable> variables) {
        variables.stream().forEach(variable -> {
            String variableName = variable.getName();
            try {
                final String value = valueFinder.lookup(variableName);
                if (StringUtils.isNotBlank(value)) {
                    Map<String, String> toolVars = variable.getToolVars();
                    toolVars.entrySet().stream().forEach(entry -> {
                        boolean rs = saveCloudVariable(entry.getKey(), entry.getValue(), value);
                        if (!rs) {
                            getLog().info(Joiner.on(" ").skipNulls().join("Failed to process value of", entry.getValue(), "for", entry.getKey()));
                        }
                    });
                } else {
                    getLog().info(Joiner.on(" ").skipNulls().join("Skip variable", variableName, "due to empty value"));
                }
            } catch (Exception e) {
                getLog().warn(Joiner.on(" ").skipNulls().join("Failed to read value for", variableName, "Skipped"));
            }
        });
    }


}
