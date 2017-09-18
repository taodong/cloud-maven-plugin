package com.github.taodnog.maven.plugins.cloud.utils;

import com.github.taodong.maven.plugins.cloud.utils.FileIOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class FileIOUtilsTests {

    private static File SANDBOX = new File("src/test/sandbox");
    private static File OUTPUT = new File(SANDBOX, "output");

    @BeforeClass
    public static void setup() throws Exception {
        if (!OUTPUT.exists() && !OUTPUT.isDirectory()) {
            OUTPUT.mkdir();
        }
    }

    @AfterClass
    public static void teardown() throws Exception {
        if (OUTPUT.exists()) {
            FileUtils.deleteDirectory(OUTPUT);
        }
    }

    @Test
    public void testCreateFolderIfNotExist() throws Exception {
        File dbFolder = new File(OUTPUT, "abc/def");

        boolean rs = FileIOUtils.createFolderIfNotExist(dbFolder);

        assertTrue(rs);
        assertTrue(dbFolder.exists() && dbFolder.isDirectory());
    }

    @Test
    public void testDownloadFileFromUrl() throws Exception {
        String url = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png";

        File download = new File(OUTPUT, "download/google.png");

        FileIOUtils.downloadFileFromUrl(url, download);

        assertTrue(download.exists());
    }

    @Test
    public void testUnZipFileTo() throws Exception {
        File zipFile = new File(SANDBOX, "packer.zip");


        System.out.println(zipFile.getAbsolutePath());
        File target = new File(OUTPUT, "packer");

        FileIOUtils.unZipFileTo(zipFile, target);

        File content = new File(OUTPUT, "packer/packer");
        assertTrue(content.exists());
    }

    @Test
    public void testMatchAllFilesByName() throws Exception {
        File folder = new File(SANDBOX, "sample");
        List<String> fileNames = new ArrayList<String>() {{
            add("config.xml");
            add("test.xml");
        }};

        Collection<File> found = FileIOUtils.matchAllFilesByName(folder, fileNames);
        assertEquals(3, found.size());
    }

    @Test
    public void testMatchAllFilesByNameFirstFound() throws Exception {
        File folder = new File(SANDBOX, "sample");
        List<String> fileNames = new ArrayList<String>() {{
            add("config.xml");
            add("test.xml");
        }};

        Collection<File> found = FileIOUtils.matchAllFilesByNameFirstFound(folder, fileNames);
        assertEquals(2, found.size());
    }

    @Test
    public void testCopyDirectoryWithExclusion() throws Exception {
        File srcFolder = new File(SANDBOX, "sample");
        File destFolder = new File(OUTPUT, "sample");
        List<IOFileFilter> excludes = new ArrayList<>();

        IOFileFilter filter1 = FileFilterUtils.notFileFilter(new NameFileFilter("test.xml"));
        excludes.add(filter1);

        IOFileFilter filter2 = FileFilterUtils.notFileFilter(new SuffixFileFilter(".txt"));
        excludes.add(filter2);

        FileIOUtils.copyDirectoryWithExclusion(srcFolder, destFolder, excludes);

        assertTrue(new File(OUTPUT, "sample/sub2/subsub2/config.xml").exists());
        assertFalse(new File(OUTPUT, "sample/abc.txt").exists());
        assertFalse(new File(OUTPUT, "sample/sub2/test.xml").exists());
    }

    @Test
    public void testCreateIOFileFilter() throws Exception {
        String prefix = "abc*";

        IOFileFilter filter = FileIOUtils.createIOFileFilter(prefix);
        assertTrue(filter instanceof PrefixFileFilter);

        String suffix = "*.txt";
        filter = FileIOUtils.createIOFileFilter(suffix);
        assertTrue(filter instanceof SuffixFileFilter);

        String name = "abc.config";
        filter = FileIOUtils.createIOFileFilter(name);
        assertTrue(filter instanceof NameFileFilter);

        String invalid = "/*.*txt";
        filter = FileIOUtils.createIOFileFilter(invalid);
        assertNull(filter);

    }

}
