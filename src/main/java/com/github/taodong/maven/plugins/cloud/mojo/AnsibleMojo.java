package com.github.taodong.maven.plugins.cloud.mojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.taodong.maven.plugins.cloud.finder.CloudTool;
import com.github.taodong.maven.plugins.cloud.utils.FileIOUtils;
import com.github.taodong.maven.plugins.cloud.utils.ShellExecutor;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.activation.UnsupportedDataTypeException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Plug in to run ansible scripts
 * @Author Tao Dong
 */
@Mojo(name = "ansible", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true)
public class AnsibleMojo extends CloudAbstractMojo {

    /**
     * ansible executable: ansible or ansible-playbook
     */
    @Parameter(property = "cloud.ansible.exe", defaultValue = "ansible-playbook")
    private String exec;

    /**
     * ansible arguments
     */
    @Parameter(property = "cloud.ansible.arguments")
    protected String arguments;

    /**
     * file to be executed as play book
     */
    @Parameter(property = "cloud.ansible.playbookFile")
    protected String playbookFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (matchCloudBuilder("ansible")) {
            try {
                boolean isPlaybook = StringUtils.equalsAnyIgnoreCase("ansible-playbook");
                String ansibleCommand = isPlaybook ? "ansible-playbook" : "ansible";
                List<CommandLine> commands = new ArrayList<>();

                File workFolder = new File(TARGET);
                if (StringUtils.isNotBlank(buildFolder)) {
                    workFolder = new File(TARGET, buildFolder);
                }

                if (workFolder.exists() && workFolder.isDirectory()) {
                    CommandLine commandLine = new CommandLine(ansibleCommand);

                    if (isPlaybook) {
                        if (StringUtils.endsWith(playbookFile, ".yml")) {
                            Collection<File> playbooks = FileIOUtils.matchAllFilesByNameFirstFound(workFolder, new ArrayList<String>() {{
                                add(playbookFile);
                            }});

                            if (playbooks == null || playbooks.isEmpty()) {
                                throw new FileNotFoundException(Joiner.on(" ").join("playbookFile", playbookFile, "is not found"));
                            } else {
                                File pb = Iterables.getFirst(playbooks, null);
                                commandLine.addArgument(pb.getAbsolutePath());
                                if (playbooks.size() > 1) {
                                    getLog().warn(Joiner.on(" ").skipNulls().join("Multiple files match the playbook name. Only the first match", pb.getAbsolutePath(), "is played"));
                                }
                            }
                        } else {
                            throw new UnsupportedDataTypeException(Joiner.on(" ").skipNulls().join("Ansible playbook file has to be a .yml file. Found", playbookFile, "instead."));
                        }
                    }

                    if (StringUtils.isNotBlank(arguments)) {
                        commandLine.addArgument(arguments, true);
                    }

                    Map<String, String> ansibleVars = cloudVariables.get(CloudTool.ANSIBLE);
                    if (ansibleVars != null && !ansibleVars.isEmpty()) {
                        getLog().info("Found extra variables passed through Clound Variable plugin. Generating variables.json.");
                        File tempFolder = new File(buildFolder, TEMP);
                        FileIOUtils.createFolderIfNotExist(tempFolder);
                        File variableFile = new File(tempFolder, "variables.json");
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writeValue(variableFile, ansibleVars);
                        commandLine.addArgument("--extra-vars").addArgument(Joiner.on("").skipNulls().join("\"@", variableFile.getAbsolutePath(), "\""), true);
                    } else {
                        getLog().info("No extra variables found.");
                    }

                    commands.add(commandLine);

                    final ShellExecutor executor = new ShellExecutor();
                    executor.executeCommands(getLog(), commands, workFolder, 0);
                }
            } catch (Exception e) {
                getLog().error(Joiner.on(" ").skipNulls().join("Failed to run task ansible:", e.getMessage()));
                throw new MojoFailureException("Failed to run task ansible", e);
            }
        } else {
            getLog().info(Joiner.on(" ").skipNulls().join("Skip ansible build. Cloud executor is set to", cloudExe));
        }
    }
}
