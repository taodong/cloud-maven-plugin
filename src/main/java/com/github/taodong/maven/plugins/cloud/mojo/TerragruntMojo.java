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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Plug in to run terragrunt scripts
 * @Author Tao Dong
 */
@Mojo(name = "terragrunt", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true)
public class TerragruntMojo  extends CloudAbstractMojo {
    private static final String terragruntUrl = "https://github.com/gruntwork-io/terragrunt/releases/download/";

    private static final String TERRAGRUNT_HEADER = "general-head.txt";

    /**
     * Packer version
     */
    @Parameter(property = "cloud.terragrunt.version", defaultValue = "SYSTEM")
    protected String version;

    /**
     * System running packer
     */
    @Parameter(property = "cloud.terragrunt.system")
    protected String system;

    /**
     * System format
     */
    @Parameter(property = "cloud.terragrunt.systemFormat")
    protected String format;

    /**
     * terraform arguments
     */
    @Parameter(property = "cloud.terragrunt.arguments")
    protected String arguments;

    /**
     * subfolder contains modules
     */
    @Parameter(property = "cloud.terragrunt.modules", required = true)
    protected List<String> modules;

    /**
     * terragrunt command timeout in seconds, default 15 min
     */
    @Parameter(property = "cloud.terragrunt.commandTimeOut", defaultValue = "900")
    protected long timeout;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (matchCloudBuilder("terragrunt")) {
            try {
                String terragruntCommand = "terragrunt";

                final List<CommandLine> commands = new ArrayList<>();
                List<File> moduleFolders = new ArrayList<>();

                File terragruntFolder = new File(TARGET);

                if (StringUtils.isNotBlank(buildFolder)) {
                    terragruntFolder = new File(TARGET, buildFolder);
                }

                if (terragruntFolder.exists() && terragruntFolder.isDirectory()) {
                    // if modules, match up sub-folder with module name
                    if (modules != null && !modules.isEmpty()) {
                        final File parentFolder = terragruntFolder;
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
                        moduleFolders.add(terragruntFolder);
                    }

                    // download terragrunt if needed
                    if (!StringUtils.equalsIgnoreCase(DEFAULT_VERSION, StringUtils.trim(version))) {
                        OS os = OS.getOS(system);
                        OSFormat osFormat = OSFormat.getOSFormat(format);
                        if (os == OS.UNKNOWN || osFormat == OSFormat.UNKNOWN) {
                            getLog().warn(Joiner.on(" ").skipNulls().join("System", system, format, "is un-supported. Use terragrunt installed by user."));
                        } else {
                            File terragruntLoc = new File(envToolDir, Joiner.on("/").skipNulls().join("terragrunt", version));
                            File terragruntExe = new File(terragruntLoc, "terragrunt");
                            if (terragruntExe.exists()) {
                                getLog().info(Joiner.on(" ").skipNulls().join("terragrunt version ", version, "found"));
                                terragruntCommand = terragruntExe.getAbsolutePath();
                            } else {
                                String scriptName = Joiner.on("").skipNulls().join("terragrunt_", os.getPackageName(), "_", osFormat.getFormat());
                                String downloadUrl = Joiner.on("").skipNulls().join(terragruntUrl, StringUtils.startsWith(version, "v") ? null : "v",version, "/", scriptName);
                                File terraFile = new File(envToolDir, scriptName);
                                getLog().info(Joiner.on(" ").skipNulls().join("Downloading terragrunt from", downloadUrl));
                                try {
                                    FileIOUtils.createFolderIfNotExist(terragruntLoc);
                                    FileIOUtils.downloadFileFromUrl(downloadUrl, terragruntExe);
                                    if (terragruntExe.exists()) {
                                        terragruntExe.setExecutable(true);
                                        terragruntCommand = terragruntExe.getAbsolutePath();
                                    } else {
                                        throw new FileNotFoundException(Joiner.on(" ").skipNulls().join("Error occurs during downloading, terragrunt",
                                                terragruntExe.getAbsolutePath(), "is not found"));
                                    }
                                } catch (Exception e) {
                                    getLog().warn(Joiner.on("").skipNulls().join("Failed to download or install terragrunt from ", downloadUrl,
                                            ". Fall back to use terragrunt installed by user"), e);
                                }
                            }
                        }
                    }

                    StringJoiner sj = new StringJoiner("").add("");

                    Map<String, String> terragruntVars = cloudVariables.get(CloudTool.TERRAGRUNT);
                    if (terragruntVars != null && !terragruntVars.isEmpty()) {
                        getLog().info("Found extra variables passed through Clound Variable plugin. Generating variables.tfvars.");
                        File tempFolder = new File(buildFolder, TEMP);
                        FileIOUtils.createFolderIfNotExist(tempFolder);
                        File variableFile = new File(tempFolder, "variables.tfvars.json");
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writeValue(variableFile, terragruntVars);
                        sj = sj.add("-var-file=").add(variableFile.getAbsolutePath());
                    } else {
                        getLog().info("No extra variables found.");
                    }

                    final String varFileArg = StringUtils.isBlank(sj.toString()) ? null : sj.toString();

                    CommandLine commandLine = new CommandLine(terragruntCommand);
                    if (StringUtils.isNotBlank(arguments)) {
                        commandLine.addArguments(arguments, true);
                    } else {
                        commandLine.addArguments("apply");
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

                        FileIOUtils.generateShellScript(terragruntFolder, "build.sh", content);

                        getLog().info(Joiner.on(" ").skipNulls().join("Generated script build.sh under", terragruntFolder.getAbsolutePath()));
                    }

                }
            } catch (Exception e) {
                getLog().error(Joiner.on(" ").skipNulls().join("Failed to run task terragrunt:", e.getMessage()));
                throw new MojoFailureException("Failed to run task terragrunt", e);
            }
        } else {
            getLog().info(Joiner.on(" ").skipNulls().join("Skip terragrunt build. Cloud executor is set to", cloudExe));
        }
    }
}
