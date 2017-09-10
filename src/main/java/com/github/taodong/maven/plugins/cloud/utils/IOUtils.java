package com.github.taodong.maven.plugins.cloud.utils;

import java.io.File;
import java.io.IOException;

public class IOUtils {
    /**
     * Create folder if not exists
     * @param file - Folder to be created
     * @return true if folder created, otherwise false
     * @throws IOException
     */
    public static boolean createFolderIfNotExist(File file) throws IOException {
        boolean rs = false;

        if (!file.exists() || !file.isDirectory()) {
            rs = file.mkdir();
        }

        return rs;
    }
}
