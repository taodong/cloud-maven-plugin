package com.github.taodnog.maven.plugins.cloud.utils;

import com.github.taodong.maven.plugins.cloud.utils.ShellExecutor;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShellExecutorTests {

    @Test
    public void testExecuteSingleCommandGetOutput() throws Exception{
        ShellExecutor shellExecutor = new ShellExecutor();
        ConsoleLogger consoleLogger = new ConsoleLogger(Logger.LEVEL_INFO, "Console");
        Log logger = new DefaultLog(consoleLogger);
        String command = "echo test";
        List<String> rs = shellExecutor.executeSingleCommandGetOutput(logger, command, null, 0);
        assertEquals("test", rs.get(0));
    }
}
