package com.github.taodnog.maven.plugins.cloud.utils;

import com.github.taodong.maven.plugins.cloud.utils.ShellExecutor;
import com.google.common.base.Joiner;
import org.apache.commons.exec.CommandLine;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShellExecutorTests {

    @Test
    public void testExecuteSingleCommandGetOutput() throws Exception {
        ShellExecutor shellExecutor = new ShellExecutor();
        ConsoleLogger consoleLogger = new ConsoleLogger(Logger.LEVEL_INFO, "Console");
        Log logger = new DefaultLog(consoleLogger);
        CommandLine command = new CommandLine("echo");
        command.addArgument("test");
        List<String> rs = shellExecutor.executeSingleCommandGetOutput(logger, command, null, 0);
        assertEquals("test", rs.get(0));
    }
}
