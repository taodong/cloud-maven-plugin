package com.github.taodong.maven.plugins.cloud.mojo;

import com.github.taodong.maven.plugins.cloud.utils.FileIOUtils;
import com.github.taodong.maven.plugins.cloud.utils.OS;
import com.github.taodong.maven.plugins.cloud.utils.OSFormat;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileNotFoundException;

@Mojo(name = "packer", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true)
public class PackerMojo extends CloudAbstractMojo{

    private static final String packerUrl = "https://releases.hashicorp.com/packer/";

    /**
     * Packer version
     */
    @Parameter(property = "version", required = false, defaultValue = "SYSTEM")
    protected String version;

    /**
     * System running packer
     */
    @Parameter(property = "system", required = false)
    protected String system;

    /**
     * System format
     */
    @Parameter(property = "systemFormat", required = false)
    protected String format;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String packerCommand = "packer";

        if (!StringUtils.equalsIgnoreCase(DEFAULT_VERSION, version)) {
            OS os = OS.getOS(system);
            OSFormat osFormat = OSFormat.getOSFormat(format);
            if (os == OS.UNKNOWN || osFormat == OSFormat.UNKNOWN) {
                getLog().warn(Joiner.on(" ").skipNulls().join("System", system, format, "is un-supported. Use packer installed by user."));
            } else {
                File packerLoc = new File(envToolDir, Joiner.on("").skipNulls().join("packer/", version));
                File packerExe = new File(packerLoc, "packer");
                if (packerExe.exists()) {
                    getLog().info(Joiner.on(" ").skipNulls().join("packer version ", version, "found"));
                    packerCommand = packerExe.getAbsolutePath();
                } else {
                    String binZip = Joiner.on("").skipNulls().join("packer_", version, "_", os.getName(), "_", osFormat.getFormat(), ".zip");
                    String downloadUrl = Joiner.on("").skipNulls().join(packerUrl, version, "/", binZip);
                    File zipFile = new File(envToolDir, binZip);
                    getLog().info(Joiner.on(" ").skipNulls().join("Downloading Parker from", downloadUrl));
                    try {
                        FileIOUtils.downloadFileFromUrl(downloadUrl, zipFile);
                        FileIOUtils.unZipFileTo(zipFile, packerLoc);
                        if (packerExe.exists()) {
                            packerCommand = packerExe.getAbsolutePath();
                        } else {
                            throw new FileNotFoundException(Joiner.on(" ").skipNulls().join("Error occurs during unzip, packer",
                                    packerExe.getAbsolutePath(), "is not found"));
                        }
                    } catch (Exception e) {
                        getLog().warn(Joiner.on("").skipNulls().join("Failed to download or install packer from ", downloadUrl,
                                ". Fall back to use packer installed by user"), e);
                    }
                }
            }
        } else {
            getLog().info("Use packer installed by user");
        }
    }
}
