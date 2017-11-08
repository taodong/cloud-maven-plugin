package com.github.taodong.maven.plugins.cloud.mojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.taodong.maven.plugins.cloud.finder.CloudTool;
import com.github.taodong.maven.plugins.cloud.utils.FileIOUtils;
import com.github.taodong.maven.plugins.cloud.utils.OS;
import com.github.taodong.maven.plugins.cloud.utils.OSFormat;
import com.github.taodong.maven.plugins.cloud.utils.ShellExecutor;
import com.google.common.base.Joiner;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Plug in to run packer scripts
 * @Author Tao Dong
 */
@Mojo(name = "packer", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true)
public class PackerMojo extends CloudAbstractMojo{

    private static final String packerUrl = "https://releases.hashicorp.com/packer/";

    /**
     * Packer version
     */
    @Parameter(property = "cloud.packer.version", required = false, defaultValue = "SYSTEM")
    protected String version;

    /**
     * System running packer
     */
    @Parameter(property = "cloud.packer.system", required = false)
    protected String system;

    /**
     * System format
     */
    @Parameter(property = "cloud.packer.systemFormat", required = false)
    protected String format;

    /**
     * packer arguments
     */
    @Parameter(property = "cloud.packer.arguments", required = false)
    protected String arguments;

    /**
     * configuration file to be executed by packer
     */
    @Parameter(property = "cloud.packer.configFiles", required = true)
    protected List<String> configFiles;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (matchCloudBuilder("packer")) {
            try {
                String packerCommand = "packer";

                List<CommandLine> commands = new ArrayList<>();

                File packerFolder = new File(TARGET);

                if (StringUtils.isNotBlank(buildFolder)) {
                    packerFolder = new File(TARGET, buildFolder);
                }

                if (packerFolder.exists() && packerFolder.isDirectory()) {
                    StringJoiner sj = new StringJoiner("").add("");

                    Map<String, String> packerVars = cloudVariables.get(CloudTool.PACKER);
                    if (packerVars != null && !packerVars.isEmpty()) {
                        getLog().info("Found extra variables passed through Clound Variable plugin. Generating variables.json.");
                        File tempFolder = new File(buildFolder, TEMP);
                        FileIOUtils.createFolderIfNotExist(tempFolder);
                        File variableFile = new File(tempFolder, "variables.json");
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writeValue(variableFile, packerVars);
                        sj = sj.add("-var-file=").add(variableFile.getAbsolutePath());
                    } else {
                        getLog().info("No extra variables found.");
                    }

                    final String varFileArg = StringUtils.isBlank(sj.toString()) ? null : sj.toString();

                    // look for config files
                    Collection<File> images = FileIOUtils.matchAllFilesByNameFirstFound(packerFolder, configFiles);

                    if (images != null && !images.isEmpty()) {

                        final ShellExecutor executor = new ShellExecutor();

                        if (!StringUtils.equalsIgnoreCase(DEFAULT_VERSION, StringUtils.trim(version))) {
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
                                    String binZip = Joiner.on("").skipNulls().join("packer_", version, "_", os.getPackageName(), "_", osFormat.getFormat(), ".zip");
                                    String downloadUrl = Joiner.on("").skipNulls().join(packerUrl, version, "/", binZip);
                                    File zipFile = new File(envToolDir, binZip);
                                    getLog().info(Joiner.on(" ").skipNulls().join("Downloading Parker from", downloadUrl));
                                    try {
                                        FileIOUtils.downloadFileFromUrl(downloadUrl, zipFile);
                                        FileIOUtils.unZipFileTo(zipFile, packerLoc);
                                        if (packerExe.exists()) {
                                            packerExe.setExecutable(true);
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

                        executor.executeCommands(getLog(), commands, packerFolder, 0);

                        final String exe = packerCommand;

                        images.stream().forEach(image -> {
                            List<CommandLine> commandLines = new ArrayList<>();
                            CommandLine cmd = new CommandLine(exe);
                            cmd.addArgument("build").addArguments(arguments, true).addArguments(varFileArg, true).addArgument(image.getName());
                            commandLines.add(cmd);
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
        } else {
            getLog().info(Joiner.on(" ").skipNulls().join("Skip packer build. Cloud executor is set to", cloudExe));
        }
    }
}
