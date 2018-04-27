package com.github.taodong.maven.plugins.cloud.finder;

import com.github.taodong.maven.plugins.cloud.utils.ShellExecutor;
import com.google.common.base.Joiner;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CredstashValueFinder implements ValueFinder{
    private static final String REGION = "region";
    private static final String TABLE = "table";
    private static final String KEY = "key";
    private static final String NO_LINE = "noline";

    private ShellExecutor executor = new ShellExecutor();
    private List<String> commandArguments = new ArrayList();
    private Log logger;

    public CredstashValueFinder(Log logger, Map<String, String> config) {

        if (config != null && !config.isEmpty()) {

            if (StringUtils.isNotBlank(config.get(REGION))) {
                commandArguments.add("-r");
                commandArguments.add(config.get(REGION));
            }
            if (StringUtils.isNotBlank(TABLE)) {
                commandArguments.add("-t");
                commandArguments.add(config.get(TABLE));
            }
            if (StringUtils.isNotBlank(config.get(KEY))) {
                commandArguments.add("-k");
                commandArguments.add(config.get(KEY));
            }
            if (StringUtils.isNotBlank(config.get(NO_LINE))) {
                commandArguments.add("-n");
            }
        }

        this.logger = logger;
    }

    @Override
    public String lookup(String variableName) {

        try {
            CommandLine command = new CommandLine("credstash");
            if (!commandArguments.isEmpty()) {
                command.addArguments(commandArguments.toArray(new String[commandArguments.size()]));
            }
            command.addArgument("get").addArgument(variableName);
            List<String> rs = executor.executeSingleCommandGetOutput(this.logger, command, null, -1);

            if (rs != null && !rs.isEmpty()) {
                return rs.stream().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            logger.warn(Joiner.on(" ").skipNulls().join("Failed to look up value for variable", variableName, "in credstash. Return empty string instead"), e);
        }

        return "";
    }
}
