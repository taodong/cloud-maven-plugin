package com.github.taodong.maven.plugins.cloud.mojo;

import com.github.taodong.maven.plugins.cloud.utils.FileIOUtils;
import com.google.common.base.Joiner;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plug in to assemble project, will ignore files starts with . when copy
 */
@Mojo(name = "assemble", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class AssembleMojo extends CloudAbstractMojo{

    protected Map<String, String> paramMap = new HashMap<>();

    @Parameter(property = "cloudRoot", required = false)
    private File cloudRoot;

    /**
     * extra folders to copy to target
     */
    @Parameter(property = "extra", required = false)
    private List<File> extra;

    /**
     * files not to copy
     */
    @Parameter(property = "ignoreFiles", required = false)
    private List<String> ignoreFiles;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            boolean ignoreExtra = false;

            if (cloudRoot == null || !cloudRoot.exists()) {
                cloudRoot = new File(SRC);
                ignoreExtra = true;
            }

            File targetFolder = null;
            if (StringUtils.isBlank("buildFolder")) {
                targetFolder = new File(TARGET);
            } else {
                targetFolder = new File(TARGET, buildFolder);
            }

            List<IOFileFilter> exclusions = new ArrayList<>();
            exclusions.add(FileFilterUtils.notFileFilter(new PrefixFileFilter(".")));

            for (String ignore : ignoreFiles) {
                if (StringUtils.isNotBlank(ignore)) {
                    IOFileFilter filter = FileIOUtils.createIOFileFilter(ignore);
                    if (ignore != null) {
                        exclusions.add(FileFilterUtils.notFileFilter(filter));
                    } else {
                        getLog().warn(Joiner.on(" ").skipNulls().join("Can't ignore file", ignore, "due to unsupported format."));
                    }
                }
            }

            FileIOUtils.copyDirectoryWithExclusion(cloudRoot, targetFolder, exclusions);

            if (!ignoreExtra) {
                for (File exFolder : extra) {
                    FileIOUtils.copyDirectoryWithExclusion(exFolder, new File(targetFolder, exFolder.getName()), exclusions);
                }
            }
        } catch (Exception e) {
            getLog().error(Joiner.on(" ").skipNulls().join("Failed to run task assemble:", e.getMessage()));
            throw new MojoFailureException("Failed to run task assemble", e);
        }
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }
}
