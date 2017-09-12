package com.github.taodong.maven.plugins.cloud.mojo;

import com.github.taodong.maven.plugins.cloud.utils.FileIOUtils;
import com.github.taodong.maven.plugins.cloud.utils.OS;
import com.github.taodong.maven.plugins.cloud.utils.OSFormat;
import com.github.taodong.maven.plugins.cloud.utils.ShellExecutor;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    /**
     * Whether looking for target file recursively
     */
    @Parameter(property = "recursive", required = false, defaultValue = "false")
    protected Boolean recursive;

    /**
     * packer arguments
     */
    @Parameter(property = "arguments", required = false)
    private String arguments;

    /**
     * configuration file to be executed by packer
     */
    @Parameter(property = "configFiles", required = false)
    private List<String> configFiles;

    /**
     * The folder all the cloud configuration files reside
     */
    @Parameter(property = "buildFolder", required = true)
    private String buildFolder;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            String packerCommand = "packer";

            List<String> commands = new ArrayList<>();

            File packerFolder = new File(TARGET, buildFolder);

            if (packerFolder.exists() && packerFolder.isDirectory()) {

                Collection<File> images = FileIOUtils.matchAllFilesByNameFirstFound(packerFolder, configFiles);

                if (images != null && !images.isEmpty()) {

                    final ShellExecutor executor = new ShellExecutor();

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

                    File virEnv = new File(envToolDir, VIRTUAL_ENV);
                    if (virEnv.exists() && virEnv.isDirectory()) {
                        commands.add(Joiner.on("").join("source ", virEnv.getAbsolutePath(), "/bin/activate"));
                    }

                    executor.executeCommands(getLog(), commands, packerFolder, 0);

                    final String exe = packerCommand;

                    images.stream().forEach(image -> {
                        List<String> commandLines = new ArrayList<>();
                        String commandLine = Joiner.on(" ").skipNulls().join(exe, "build",
                                StringUtils.isBlank(arguments) ? null : arguments, image.getName());
                        commandLines.add(commandLine);
                        executor.executeCommands(getLog(), commandLines, image.getParentFile(), 0);
                    });

                } else {
                    getLog().info("No matching configuration file found. Packer build skipped.");
                }
            }

        } catch (Exception e) {
            getLog().error(Joiner.on(" ").skipNulls().join("Failed to run task packer:", e.getMessage()));
            throw new MojoFailureException("Failed to run task packer", e);
        }

    }
}
