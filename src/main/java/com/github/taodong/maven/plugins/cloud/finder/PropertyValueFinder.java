package com.github.taodong.maven.plugins.cloud.finder;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class PropertyValueFinder implements ValueFinder{

    private Properties properties = new Properties();

    public PropertyValueFinder(String propertyFileLoc) throws Exception {
        try (InputStream input = new FileInputStream(propertyFileLoc)) {
            properties.load(input);
        }
    }

    @Override
    public String lookup(String variableName) throws Exception{
        String value = properties.getProperty(variableName, "");
        return value;
    }
}
