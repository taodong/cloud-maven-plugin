package com.github.taodong.maven.plugins.cloud.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.List;

public class CloudVariableLoader {
    /**
     * Load cloud variable configurations
     * @param logger - logger
     * @param configFile - config file in json format
     * @return list of <code>CloudVairableConfig</code> for each source/format
     */
    public static List<CloudVariableConfig> loadConfig(Log logger, File configFile) {
        if (configFile == null || !configFile.exists()) {
            logger.error("No file to load.");
            return null;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<CloudVariableConfig> configs = objectMapper.readValue(configFile, new TypeReference<List<CloudVariableConfig>>(){});
            return configs;
        } catch (Exception e) {
            logger.error(Joiner.on(" ").skipNulls().join("Failed to parse config file", configFile.getAbsolutePath()));
            return null;
        }
    }
}
