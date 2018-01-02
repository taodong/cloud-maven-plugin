package com.github.taodong.maven.plugins.cloud.mojo;

import com.github.taodong.maven.plugins.cloud.utils.FileIOUtils;
import com.google.common.base.Joiner;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Plug in executor to build Python virtual environment. Don't find a way to execute source command
 * in Java, this plugin only export scripts
 * @Author: Tao Dong
 */
@Mojo(name = "python-env", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
public class PythonEnvMojo extends CloudAbstractMojo{
    private static final String VIRTUAL_ENV = "python-env";

    private static final String PYTHON_HEADER = "general-head.txt";

    /**
     * Python packages to be installed by pip
     */
    @Parameter(property = "python.packages")
    private List<String> packages;

    /**
     * Python packages with specified version
     */
    @Parameter(property = "python.versionedPackages")
    private Map<String, String> versionedPackages;

    /**
     * Force rebuild Python virtual environment or not
     */
    @Parameter(property = "python.rebuild", defaultValue = "false")
    private Boolean rebuild;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            File targetFolder = new File(TARGET);
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
                CommandLine cmd = new CommandLine("pushd").addArguments(new String[]{envToolDir.getAbsolutePath(), ">", "/dev/null"});
                commands.add(cmd);
                cmd = new CommandLine("virtualenv");
                cmd.addArgument(VIRTUAL_ENV);
                commands.add(cmd);
                cmd = new CommandLine("popd").addArguments(new String[]{">", "/dev/null"});
                commands.add(cmd);
            }

            getLog().info("Config Python virtual environment...");

            // enable Python virtual environment
            CommandLine cmd = new CommandLine("source");
            cmd.addArgument(Joiner.on("").join(virEnv.getAbsolutePath(), "/bin/activate'"));
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

            final List<String> content = new ArrayList<>();
            content.add(SHELL_HEADER);

            commands.stream().forEach(commd -> content.add(commandLine2Str(commd)));
            FileIOUtils.generateShellScript(targetFolder, "python-env.sh", content);

            getLog().info("Generated script python-env.sh under target folder.");

        } catch (Exception e) {
            getLog().error(Joiner.on(" ").skipNulls().join("Failed to run task python-env:", e.getMessage()));
            throw new MojoFailureException("Failed to run task python-env", e);
        }

    }
}
