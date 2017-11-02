package com.github.taodong.maven.plugins.cloud.finder;

import java.io.*;
import java.util.Properties;

public class PropertyValueFinder implements ValueFinder{

    private Properties properties = new Properties();

    public PropertyValueFinder(File propertyFile) throws IOException {
        try (InputStream input = new FileInputStream(propertyFile)) {
            properties.load(input);
        }
    }

    @Override
    public String lookup(String variableName){
        String value = properties.getProperty(variableName, "");
        return value;
    }
}
