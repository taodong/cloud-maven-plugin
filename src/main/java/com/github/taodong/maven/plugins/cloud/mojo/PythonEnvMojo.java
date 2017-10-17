package com.github.taodong.maven.plugins.cloud.mojo;

import com.github.taodong.maven.plugins.cloud.utils.FileIOUtils;
import com.github.taodong.maven.plugins.cloud.utils.ShellExecutor;
import com.google.common.base.Joiner;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Plug in executor to build Python virtual environment
 * @Author: Tao Dong
 */
@Mojo(name = "python-env", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class PythonEnvMojo extends CloudAbstractMojo{

    /**
     * time out for shell commands in minutes
     */
    @Parameter(property = "shellTimeout", required = false, defaultValue = "10")
    protected long timeout;

    /**
     * Python packages to be installed by pip
     */
    @Parameter(property = "packages", required = false)
    private List<String> packages;

    /**
     * Python packages with specified version
     */
    @Parameter(property = "versionedPackages", required = false)
    private Map<String, String> versionedPackages;

    /**
     * Force rebuild Python virtual environment or not
     */
    @Parameter(property = "rebuild", required = false, defaultValue = "false")
    private Boolean rebuild;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            ShellExecutor shellExecutor = new ShellExecutor();
            List<CommandLine> commands = new ArrayList<>();

            boolean rs = FileIOUtils.createFolderIfNotExist(envToolDir);
            if (rs) {
                getLog().info(Joiner.on(" ").skipNulls().join(envToolDir.getAbsolutePath(), "created."));
            }

            File virEnv = new File(envToolDir, VIRTUAL_ENV);

            boolean exitingPython = virEnv.exists() && virEnv.isDirectory();

            if (rebuild && exitingPython) {
                FileUtils.deleteDirectory(virEnv);
                exitingPython = false;
            }

            if (rebuild || !exitingPython) {
                CommandLine cmd = new CommandLine("virtualenv");
                cmd.addArgument(VIRTUAL_ENV);
                commands.add(cmd);
            }

            getLog().info("Config Python virtual environment...");

            // enable Python virtual environment
            CommandLine cmd = new CommandLine("/bin/bash");
            cmd.addArgument(Joiner.on("").join("/bin/bash -c 'source ", virEnv.getAbsolutePath(), "/bin/activate'"));
            commands.add(cmd);

            if (packages != null && !packages.isEmpty()) {
                List<CommandLine> pipInstalls = packages.parallelStream()
                        .map(p -> {
                            CommandLine c = new CommandLine("pip");
                            c.addArgument("install");
                            c.addArgument(p);
                            return c;
                        }).collect(Collectors.toList());
                commands.addAll(pipInstalls);
            }

            if (versionedPackages != null && !versionedPackages.isEmpty()) {
                List<CommandLine> pipVersionedInstalls = versionedPackages.entrySet().parallelStream()
                        .map(entry -> {
                            CommandLine c = new CommandLine("pip");
                            c.addArgument("install");
                            c.addArgument(Joiner.on("").skipNulls().join(entry.getKey(), "==", entry.getValue()));
                            return c;
                        })
                        .collect(Collectors.toList());
                commands.addAll(pipVersionedInstalls);
            }

            int srs = shellExecutor.executeCommands(getLog(), commands, envToolDir, timeout);

            if (srs < 0) {
                throw new MojoFailureException("Failed to create Python virtual environment");
            }

        } catch (Exception e) {
            getLog().error(Joiner.on(" ").skipNulls().join("Failed to run task python-env:", e.getMessage()));
            throw new MojoFailureException("Failed to run task python-env", e);
        }

    }
}
