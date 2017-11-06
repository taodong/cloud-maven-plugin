package com.github.taodong.maven.plugins.cloud.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Plug in to run terragrunt scripts
 * @Author Tao Dong
 */
@Mojo(name = "terragrunt", defaultPhase = LifecyclePhase.INSTALL, threadSafe = true)
public class TerragruntMojo {
}
