package com.github.taodong.maven.plugins.cloud.finder;

import com.github.taodong.maven.plugins.cloud.utils.ShellExecutor;
import com.google.common.base.Joiner;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

// credstash -t credential-store-mc-dev -r us-west-2 get mc-dev-db_username-173

public class CredstashValueFinder implements ValueFinder{
    private static final String REGION = "region";
    private static final String TABLE = "table";
    private static final String KEY = "key";
    private static final String NO_LINE = "noline";

    private ShellExecutor executor = new ShellExecutor();
    private String commandArgument = "";
    private Log logger;

    public CredstashValueFinder(Log logger, Map<String, String> config) {

        if (config != null && !config.isEmpty()) {
            StringJoiner joiner = new StringJoiner(" ");
            if (StringUtils.isNotBlank(config.get(REGION))) {
                joiner.add("-r").add(config.get(REGION));
            }
            if (StringUtils.isNotBlank(TABLE)) {
                joiner.add("-t").add(config.get(TABLE));
            }
            if (StringUtils.isNotBlank(config.get(KEY))) {
                joiner.add("-k").add(config.get(KEY));
            }
            if (StringUtils.isNotBlank(config.get(NO_LINE))) {
                joiner.add("-n");
            }

            this.commandArgument = joiner.toString();
        }

        this.logger = logger;
    }

    @Override
    public String lookup(String variableName) throws Exception {

//        String command = Joiner.on(" ").skipNulls().join("credstash", StringUtils.isBlank(commandArgument) ? null : commandArgument,
//                "get", variableName);
        CommandLine command = new CommandLine("credstash");
        if (StringUtils.isNotBlank(commandArgument)) {
            command.addArgument(commandArgument);
        }
        command.addArgument("get").addArgument(variableName);
        List<String> rs = executor.executeSingleCommandGetOutput(this.logger, command, null, -1);

        if (rs != null && !rs.isEmpty()) {
            return rs.get(0);
        } else {
            return "";
        }
    }
}
