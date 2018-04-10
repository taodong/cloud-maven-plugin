package com.github.taodnog.maven.plugins.cloud.utils;

import com.github.taodong.maven.plugins.cloud.utils.DataUtils;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Author: tao.dong
 * Date: 4/6/18
 */
public class DataUtilsTests {

    @Test
    public void testStringToList() {
        String arrStr = "[\"hello\", \"world\"]";
        List<String> rs = DataUtils.stringToList(arrStr);
        assertEquals(2, rs.size());
        assertEquals("world", rs.get(1));

        arrStr = "abc";
        rs = DataUtils.stringToList(arrStr);
        assertEquals(1, rs.size());

        arrStr = null;
        rs = DataUtils.stringToList(arrStr);
        assertNull(rs);

    }

    @Test
    public void testStringToMap() {
        String mapStr = "{\"name1\": \"value1\", \"name2\": \"value2\"}";
        Map<String, String> rs = DataUtils.stringToMap(mapStr);
        assertEquals(2, rs.size());
        assertEquals("value2", rs.get("name2"));

        mapStr = "invalid";
        rs = DataUtils.stringToMap(mapStr);
        assertNull(rs);
    }
}
