package com.github.taodong.maven.plugins.cloud.utils;

import com.google.common.io.Files;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;

import java.io.*;
import java.util.Enumeration;

/**
 * Util functions to handle IO or File related operations
 * @author Tao Dong
 */
public class FileIOUtils {
    /**
     * Create folder if not exists
     * @param file - Folder to be created
     * @return true if folder created, otherwise false
     * @throws IOException
     */
    public static boolean createFolderIfNotExist(File file) throws IOException {
        boolean rs = false;

        Files.createParentDirs(file);

        if (!file.exists() || !file.isDirectory()) {
            rs = file.mkdir();
        }

        return rs;
    }

    /**
     * Download file from a url, this connection time is set to 15 seconds and socket time out is set to 2 minutes
     * @param url - source file url
     * @param targetFile - target file
     * @throws Exception
     */
    public static void downloadFileFromUrl(final String url, final File targetFile) throws Exception {

        Files.createParentDirs(targetFile);

        Request.Get(url).connectTimeout(15000).socketTimeout(120000).execute()
                .saveContent(targetFile);
    }

    /**
     * Unzip file(s) into target folder
     * @param zipFile - .zip file to be decompress
     * @param targetFolder - target folder for the unzipped files
     */
    public static void unZipFileTo(File zipFile, File targetFolder) throws Exception {
        createFolderIfNotExist(targetFolder);

        try (ZipFile compressed = new ZipFile(zipFile)) {
            for (final Enumeration<ZipArchiveEntry> zes = compressed.getEntries(); zes.hasMoreElements();) {
                final ZipArchiveEntry ze = zes.nextElement();
                final File entryFile = new File(targetFolder, ze.getName());
                if (StringUtils.indexOfAny(ze.getName(), '/', '\\') > -1) {
                    Files.createParentDirs(entryFile);
                }
                try (FileOutputStream out = new FileOutputStream(entryFile);
                     InputStream in = compressed.getInputStream(ze)) {
                    IOUtils.copy(in, out);
                }
            }
        }
    }
}
