package com.github.taodong.maven.plugins.cloud.utils;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.codehaus.plexus.classworlds.ClassWorld;

import java.io.*;
import java.util.*;

/**
 * Util functions to handle IO or File related operations
 * @author Tao Dong
 */
public class FileIOUtils {
    private static final String STAR_SIGN = "*";
    private static final String SLASH_SIGN = "/";
    private static final String BSLASH_SIGN = "\\";

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

    /**
     * Find all files match given file name in a directory recursively
     * @param directory - directory
     * @param filenames - file names to match
     * @return Collection of match files
     */
    public static Collection<File> matchAllFilesByName(final File directory,  final List<String> filenames) {
        return FileUtils.listFiles(directory, new NameFileFilter(filenames), TrueFileFilter.TRUE);
    }

    /**
     * Find all files match given name at the uppermost sub-directory, if a match found, all the sub-directories of
     * the discovered directory will be ignored
     * @param directory - directory
     * @param filenames - file names to match
     * @return
     */
    public static Collection<File> matchAllFilesByNameFirstFound(final File directory, final List<String> filenames) {
        Collection<File> found = new ArrayList<>();
        listFilesInFolderFirstFound(found, directory, new NameFileFilter(filenames));
        return found;
    }

    /**
     * Copy files in a folder into another excluding some files
     * @param srcDir - source directory
     * @param destDir - target directory
     * @param excludes - excluded files
     */
    public static void copyDirectoryWithExclusion(File srcDir, File destDir, List<IOFileFilter> excludes) throws IOException{
        if (excludes != null && !excludes.isEmpty()) {
            if (excludes.size() == 1) {
                FileUtils.copyDirectory(srcDir, destDir, excludes.get(0));
            } else {
                FileUtils.copyDirectory(srcDir, destDir, new AndFileFilter(excludes));
            }
        } else {
            FileUtils.copyDirectory(srcDir, destDir);
        }
    }

    /**
     * Create IOFileFilter based on file name, currently support prefix, suffix and exact name match
     * @param filename - file name
     * @return corresponded IOFileFilter or null is not supported
     */
    public static IOFileFilter createIOFileFilter(final String filename) {

        if (StringUtils.startsWith(filename, STAR_SIGN)) {
            String suffix = StringUtils.remove(filename, STAR_SIGN);
            return new SuffixFileFilter(suffix);
        } else if (StringUtils.endsWith(filename, STAR_SIGN)) {
            String prefix = StringUtils.remove(filename, STAR_SIGN);
            return new PrefixFileFilter(prefix);
        } else if (!StringUtils.containsAny(filename, STAR_SIGN, SLASH_SIGN, BSLASH_SIGN)) {
            return new NameFileFilter(filename);
        }
        return null;
    }

    /**
     * Create shell scripting file
     * @param workingFolder - the folder file resides
     * @param fileName - file name
     * @param commands - commands to add into file
     * @throws IOException
     */
    public static void generateShellScript(final File workingFolder, final String fileName, final List<String> commands) throws IOException {
        createFolderIfNotExist(workingFolder);
        File script = new File(workingFolder, fileName);
        script.setExecutable(true);

        try (FileWriter fw = new FileWriter(script, false);
            BufferedWriter bw = new BufferedWriter(fw)) {
            for (String command : commands) {
                if (StringUtils.isNotBlank(command)) {
                    bw.write(command);
                    bw.newLine();
                }
            }
        }

        script.setExecutable(true);

    }

    /**
     * Read content from a file
     * @param fileLoc - location of file to read
     * @param inClassPath - is the file in class path
     * @return
     * @throws IOException
     */
    public static List<String> readFromFile(String fileLoc, boolean inClassPath) throws IOException {

        InputStream in = null;
        try {
            if (inClassPath) {
                in = FileIOUtils.class.getClassLoader().getResourceAsStream("fileLoc");
                if (in == null) {
                    throw new FileNotFoundException(Joiner.on(" ").skipNulls().join(fileLoc, "is not found in class path"));
                }
            } else {
                File file = new File(fileLoc);
                if (!file.exists() || file.isDirectory()) {
                    throw new FileNotFoundException(Joiner.on(" ").skipNulls().join(file.getAbsolutePath(), "is not found"));
                }

                in = new FileInputStream(file);
            }


            List<String> content = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

                String line;

                while ((line = br.readLine()) != null) {
                    content.add(line);
                }
            }

            return content;
        } finally {
            if (in != null) {
                try {
                  in.close();
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
    }

    private static void listFilesInFolderFirstFound(final Collection<File> files, final File directory, final IOFileFilter filter) {
        final File[] found = directory.listFiles((FileFilter) filter);

        if (found != null && found.length > 0) {
            files.addAll(Arrays.asList(found));
        } else {
            File[] subDirs = directory.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);

            if (subDirs != null && subDirs.length > 0) {
                Arrays.stream(subDirs).forEach(subDir -> listFilesInFolderFirstFound(files, subDir, filter));
            }
        }
    }

}
