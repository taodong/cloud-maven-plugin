package com.github.taodnog.maven.plugins.cloud.utils;

import com.github.taodong.maven.plugins.cloud.utils.CloudVariableConfig;
import com.github.taodong.maven.plugins.cloud.utils.CloudVariableLoader;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CloudVariableLoaderTests {

    private Log logger;

    @Before
    public void setup() {
        ConsoleLogger consoleLogger = new ConsoleLogger(Logger.LEVEL_INFO, "Console");
        this.logger = new DefaultLog(consoleLogger);
    }


    @Test
    public void testLoadConfig() {
        File configFile = new File("src/test/sandbox/variables-config.json");
        List<CloudVariableConfig> configs = CloudVariableLoader.loadConfig(this.logger, configFile);
        assertEquals(2, configs.size());
    }

}
