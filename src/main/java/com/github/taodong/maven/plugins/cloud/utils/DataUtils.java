package com.github.taodong.maven.plugins.cloud.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: tao.dong
 * Date: 4/6/18
 */
public class DataUtils {

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Convert a JSON array formatted string (e.g. ["xxxxx", "yyyyyy"]) into an array. If format is not recognized, create an array with one element
     * @param strVal - json array formatted string
     * @return String list
     */
    public static List<String> stringToList(String strVal) {
        if (StringUtils.isBlank(strVal)) {
            return null;
        }

        List<String> rs = new ArrayList<>();
        try {
            rs = mapper.readValue(strVal, new TypeReference<List<String>>(){});
        } catch (Exception e) {
            rs.add(strVal);
        }
        return rs;
    }

    /**
     * Convert a JSON object formatted string (e.g. {"name1": "value1", "name2", "value2"}) into an array. If format is not recognized, return null
     * @param strVal - json array formatted string
     * @return Map<String, String>
     */
    public static Map<String, String> stringToMap(String strVal) {
        if (StringUtils.isBlank(strVal)) {
            return null;
        }

        try {
            TypeReference<HashMap<String, String>> typeReference = new TypeReference<HashMap<String, String>>() {};
            Map<String, String> rs = mapper.readValue(strVal, typeReference);
            return rs;
        } catch (Exception e) {
            return null;
        }
    }
}
