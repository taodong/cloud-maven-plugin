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
import java.util.stream.Collectors;

/**
 * Plug in to run terraform scripts
 * @Author Tao Dong
 */
@Mojo(name = "terraform", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true)
public class TerraformMojo extends CloudAbstractMojo {

    private static final String terraformUrl = "https://releases.hashicorp.com/terraform/";

    private static final String TERRAFORM_HEADER = "general-head.txt";

    /**
     * Packer version
     */
    @Parameter(property = "cloud.terraform.version", defaultValue = "SYSTEM")
    protected String version;

    /**
     * System running packer
     */
    @Parameter(property = "cloud.terraform.system")
    protected String system;

    /**
     * System format
     */
    @Parameter(property = "cloud.terraform.systemFormat")
    protected String format;

    /**
     * terraform arguments
     */
    @Parameter(property = "cloud.terraform.arguments")
    protected String arguments;

    /**
     * subfolder contains modules
     */
    @Parameter(property = "cloud.terraform.modules", required = true)
    protected List<String> modules;

    /**
     * ansible command timeout in seconds, default 15 min
     */
    @Parameter(property = "cloud.terraform.commandTimeOut", defaultValue = "900")
    protected long timeout;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (matchCloudBuilder("terraform")) {
            try {
                String terraformCommand = "terraform";

                final List<CommandLine> commands = new ArrayList<>();
                List<File> moduleFolders = new ArrayList<>();

                File terraformFolder = new File(TARGET);

                if (StringUtils.isNotBlank(buildFolder)) {
                    terraformFolder = new File(TARGET, buildFolder);
                }

                if (terraformFolder.exists() && terraformFolder.isDirectory()) {
                    // if modules, match up sub-folder with module name
                    if (modules != null && !modules.isEmpty()) {
                        final File parentFolder = terraformFolder;
                        moduleFolders = modules.stream().filter(module -> {
                            File f = new File(parentFolder, module);
                            boolean isFolder = f.exists() && f.isDirectory();
                            if (!isFolder) {
                                getLog().warn(Joiner.on(" ").skipNulls().join("Module folder", module, "doesn't exist.", "Skips"));
                            }
                            return isFolder;
                        }).map(module -> new File(parentFolder, module)).collect(Collectors.toList());
                    }

                    if (moduleFolders.isEmpty()) {
                        moduleFolders.add(terraformFolder);
                    }

                    // download terraform if needed
                    if (!StringUtils.equalsIgnoreCase(DEFAULT_VERSION, StringUtils.trim(version))) {
                        OS os = OS.getOS(system);
                        OSFormat osFormat = OSFormat.getOSFormat(format);
                        if (os == OS.UNKNOWN || osFormat == OSFormat.UNKNOWN) {
                            getLog().warn(Joiner.on(" ").skipNulls().join("System", system, format, "is un-supported. Use terraform installed by user."));
                        } else {
                            File terraformLoc = new File(envToolDir, Joiner.on("/").skipNulls().join("terraform", version));
                            File terraformExe = new File(terraformLoc, "terraform");
                            if (terraformExe.exists()) {
                                getLog().info(Joiner.on(" ").skipNulls().join("terraform version ", version, "found"));
                                terraformCommand = terraformExe.getAbsolutePath();
                            } else {
                                String binZip = Joiner.on("").skipNulls().join("terraform_", version, "_", os.getPackageName(), "_", osFormat.getFormat(), ".zip");
                                String downloadUrl = Joiner.on("").skipNulls().join(terraformUrl, version, "/", binZip);
                                File zipFile = new File(envToolDir, binZip);
                                getLog().info(Joiner.on(" ").skipNulls().join("Downloading terraform from", downloadUrl));
                                try {
                                    FileIOUtils.downloadFileFromUrl(downloadUrl, zipFile);
                                    FileIOUtils.unZipFileTo(zipFile, terraformLoc);
                                    if (terraformExe.exists()) {
                                        terraformExe.setExecutable(true);
                                        terraformCommand = terraformExe.getAbsolutePath();
                                    } else {
                                        throw new FileNotFoundException(Joiner.on(" ").skipNulls().join("Error occurs during unzip, terraform",
                                                terraformExe.getAbsolutePath(), "is not found"));
                                    }
                                } catch (Exception e) {
                                    getLog().warn(Joiner.on("").skipNulls().join("Failed to download or install terraform from ", downloadUrl,
                                            ". Fall back to use terraform installed by user"), e);
                                }
                            }
                        }
                    }

                    StringJoiner sj = new StringJoiner("").add("");

                    Map<String, String> terraformVars = cloudVariables.get(CloudTool.TERRAFORM);
                    if (terraformVars != null && !terraformVars.isEmpty()) {
                        getLog().info("Found extra variables passed through Clound Variable plugin. Generating variables.tfvars.");
                        File tempFolder = new File(buildFolder, TEMP);
                        FileIOUtils.createFolderIfNotExist(tempFolder);
                        File variableFile = new File(tempFolder, "variables.tfvars");
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writeValue(variableFile, terraformVars);
                        sj = sj.add("-var-file=").add(variableFile.getAbsolutePath());
                    } else {
                        getLog().info("No extra variables found.");
                    }

                    final String varFileArg = StringUtils.isBlank(sj.toString()) ? null : sj.toString();

                    CommandLine commandLine = new CommandLine(terraformCommand);
                    if (StringUtils.isNotBlank(arguments)) {
                        commandLine.addArguments(arguments, true);
                    }

                    if (StringUtils.isNotBlank(varFileArg)) {
                        commandLine.addArguments(varFileArg, true);
                    }

                    commands.add(commandLine);

                    final ShellExecutor executor = new ShellExecutor();

                    final boolean genScript = genScriptOnly;
                    final List<String> content = new ArrayList<>();

                    if (genScript) {
                        if (customScriptHead != null && customScriptHead.exists()) {
                            content.addAll(FileIOUtils.readFromFile(customScriptHead.getAbsolutePath(), false));
                        } else {
                            content.add(SHELL_HEADER);
                        }
                    }

                    moduleFolders.stream().forEach(module -> {
                        if (genScript) {
                            content.add(Joiner.on(" ").skipNulls().join("pushd", module.getAbsolutePath(), " > /dev/null"));
                            for (CommandLine cl : commands) {
                                content.add(commandLine2Str(cl));
                            }
                            content.add("popd > /dev/null");
                        } else {
                            executor.executeCommands(getLog(), commands, module, timeout);
                        }
                    });

                    if (genScript) {
                        if (customScriptTail != null && customScriptTail.exists()) {
                            content.addAll(FileIOUtils.readFromFile(customScriptTail.getAbsolutePath(), false));
                        }

                        FileIOUtils.generateShellScript(terraformFolder, "build.sh", content);

                        getLog().info(Joiner.on(" ").skipNulls().join("Generated script build.sh under", terraformFolder.getAbsolutePath()));
                    }

                }
            } catch (Exception e) {
                getLog().error(Joiner.on(" ").skipNulls().join("Failed to run task terraform:", e.getMessage()));
                throw new MojoFailureException("Failed to run task terraform", e);
            }
        } else {
            getLog().info(Joiner.on(" ").skipNulls().join("Skip terraform build. Cloud executor is set to", cloudExe));
        }
    }
}
