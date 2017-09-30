package com.github.taodong.maven.plugins.cloud.finder;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

import java.util.Map;

public class ValueFinderFactory {
    public static ValueFinder createValueFinder(final Log logger, final String finderName, Map<String, String> finderConfig) {
        if (StringUtils.equalsIgnoreCase("property", finderName)) {
            String fileLoc = finderConfig.get("source");
            if (StringUtils.isBlank(fileLoc)) {
                logger.error(Joiner.on("").skipNulls().join("Required configuration value missing for ", finderName, ": source"));
            }

            try {
                ValueFinder finder = new PropertyValueFinder(fileLoc);
                return finder;
            } catch (Exception e) {
                logger.error(Joiner.on(" ").skipNulls().join("Failed to create value finder", finderName), e);
                return null;
            }
        } else {
            logger.error(Joiner.on(" ").skipNulls().join("Un-supported value finder", finderName));
            return null;
        }
    }
}
