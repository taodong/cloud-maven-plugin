package com.github.taodong.maven.plugins.cloud.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Plug in to run terraform scripts
 * @Author Tao Dong
 */
@Mojo(name = "terraform", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true)
public class TerraformMojo extends CloudAbstractMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }
}
