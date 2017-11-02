package com.github.taodong.maven.plugins.cloud.mojo;

import com.github.taodong.maven.plugins.cloud.finder.*;
import com.github.taodong.maven.plugins.cloud.utils.CloudVariableConfig;
import com.github.taodong.maven.plugins.cloud.utils.CloudVariableLoader;
import com.github.taodong.maven.plugins.cloud.utils.FileIOUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Mojo(name = "variables", defaultPhase = LifecyclePhase.COMPILE, threadSafe = true)
public class CloudVariableMojo extends CloudAbstractMojo {
    final String FORMAT_PROPERTY = "properties";
    final String FORMAT_CREDSTAH = "credstash";

    /**
     * File folder to store build tools of specified version
     */
    @Parameter(property = "cloud.variable.config", required = true)
    protected File configFile;

    @Parameter(property = "cloud.variable.lookupFolder", required = false)
    protected File lookupFolder;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (configFile != null && configFile.exists() && !configFile.isDirectory()) {
                List<CloudVariableConfig> configs = CloudVariableLoader.loadConfig(getLog(), configFile);
                if (configs != null && !configs.isEmpty()) {
                    configs.stream().forEach(config -> processConfig(config));
                }
            } else {
                getLog().warn(Joiner.on(" ").skipNulls().join("Skip loading cloud variables: Couldn't read config file", configFile == null ? null : configFile.getAbsolutePath()));
            }
        } catch (Exception e) {
            throw new MojoExecutionException(Joiner.on(" ").skipNulls().join("Failed to find variable values", e.getMessage()), e);
        }
    }

    private void processConfig(final CloudVariableConfig config) {
        String format = config.getFormat();
        if (StringUtils.equalsIgnoreCase(FORMAT_PROPERTY, format)) {
            String source = config.getSource();
            try {
                File propertyFile = new File(source);
                if (!propertyFile.exists()) {
                    // lookup property files through lookup Folder
                    File workFolder = lookupFolder.exists() ? lookupFolder : new File(SRC);
                    if (workFolder.exists()) {
                        final String fileName = propertyFile.getName();
                        Collection<File> properties = FileIOUtils.matchAllFilesByNameFirstFound(workFolder, new ArrayList<String>(){{
                            add(fileName);
                        }});

                        if (properties != null && !properties.isEmpty()) {
                            propertyFile = Iterables.getFirst(properties, null);
                            if (propertyFile == null) {
                                throw new FileNotFoundException(Joiner.on(" ").skipNulls().join("File", source, "not found under folder", workFolder.getAbsolutePath()));
                            }
                        }
                    }
                }

                final PropertyValueFinder valueFinder = new PropertyValueFinder(propertyFile);
                List<CloudVariable> variables = config.getVariables();
                if (variables != null && !variables.isEmpty()) {
                    lookupValues(valueFinder, variables);
                }
            } catch (IOException e) {
                Optional<CloudVariable> firstRequired = findFirstRequired(config.getVariables());
                if (firstRequired.isPresent()) {
                    throw new NoValueFoundException(firstRequired.get().getName(), Joiner.on(" ").skipNulls().join(
                       "Failed to load property file", source, "which contains required variable", firstRequired.get().getName()
                    ));
                } else {
                    getLog().error(Joiner.on(" ").skipNulls().join("Failed to load property file:", source, "Value lookup skipped"), e);
                }
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
            final String variableName = variable.getName();

            final String value = valueFinder.lookup(variableName);
            final boolean isRequired = variable.isRequired();
            if (isRequired && StringUtils.isBlank(value)) {
                throw new NoValueFoundException(variableName);
            }

            if (StringUtils.isNotBlank(value)) {
                Map<String, String> toolVars = variable.getToolVars();
                toolVars.entrySet().stream().forEach(entry -> {
                    boolean rs = saveCloudVariable(entry.getKey(), entry.getValue(), value);
                    if (!rs) {
                        String message = Joiner.on(" ").skipNulls().join("Failed to process value of", entry.getValue(), "for", entry.getKey());
                        if (isRequired) {
                            throw new NoValueFoundException(variableName, message);
                        } else {
                            getLog().info(message);
                        }
                    }
                });
            } else {
                getLog().info(Joiner.on(" ").skipNulls().join("Skip variable", variableName, "due to empty value"));
            }

        });
    }

    private Optional<CloudVariable> findFirstRequired(List<CloudVariable> variables) {
        if (variables == null || variables.isEmpty()) {
            return Optional.empty();
        } else {
            return variables.stream().filter(v -> v.isRequired()).findFirst();
        }
    }


}
