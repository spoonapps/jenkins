package org.jenkinsci.plugins.spoontrigger.utils;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.jenkinsci.plugins.spoontrigger.Messages.*;

public final class FileResolver {

    private Optional<TaskListener> taskListener = Optional.absent();
    private Optional<AbstractBuild> build = Optional.absent();
    private Optional<EnvVars> env = Optional.absent();
    private List<Probe> probingStrategy = Lists.newArrayList(Probe.WORKING_DIR);

    public static FileResolver create() {
        return new FileResolver();
    }

    public FileResolver probingStrategy(Probe... probe) {
        checkArgument(probe != null && probe.length > 0, REQUIRE_NOT_NULL_OR_EMPTY_S, "resolution strategy");
        checkArgument(ImmutableSet.copyOf(probe).size() == probe.length, "resolution strategy contains duplicates");

        this.probingStrategy = Arrays.asList(probe);
        return this;
    }

    public FileResolver listener(TaskListener taskListener) {
        checkArgument(taskListener != null, REQUIRE_NOT_NULL_S, "listener");

        this.taskListener = Optional.of(taskListener);
        return this;
    }

    public FileResolver env(EnvVars env) {
        checkArgument(env != null, REQUIRE_NOT_NULL_S, "env");

        this.env = Optional.of(env);
        return this;
    }

    public FileResolver build(AbstractBuild build) {
        checkArgument(build != null, REQUIRE_NOT_NULL_S, "build");

        this.build = Optional.of(build);
        return this;
    }

    public Optional<FilePath> resolve(String filePath) {
        checkArgument(!Strings.isNullOrEmpty(filePath), REQUIRE_NOT_NULL_OR_EMPTY_S, "filePath");
        checkState(this.build.isPresent(), REQUIRE_PRESENT_S, "build");
        checkState(this.taskListener.isPresent(), REQUIRE_PRESENT_S, "listener");

        String expandedPath = this.env.isPresent() ? this.env.get().expand(filePath) : filePath;
        for (Probe strategy : this.probingStrategy) {
            Optional<FilePath> resolvedPath = strategy.resolve(expandedPath, this.build.get(), this.taskListener.get());
            if (resolvedPath.isPresent()) {
                return resolvedPath;
            }
        }

        return Optional.absent();
    }

    public static enum Probe {
        WORKING_DIR {
            @Override
            public Optional<FilePath> resolve(String filePath, AbstractBuild build, TaskListener listener) {
                File file = new File(filePath);
                FilePath jenkinsFilePath = new FilePath(file);
                return resolve(jenkinsFilePath, listener);
            }
        },
        MODULE {
            @Override
            public Optional<FilePath> resolve(String filePath, AbstractBuild build, TaskListener listener) {
                FilePath jenkinsFilePath = new FilePath(build.getModuleRoot(), filePath);
                return resolve(jenkinsFilePath, listener);
            }
        },
        WORKSPACE {
            @Override
            public Optional<FilePath> resolve(String filePath, AbstractBuild build, TaskListener listener) {
                FilePath jenkinsFilePath = new FilePath(build.getWorkspace(), filePath);
                return resolve(jenkinsFilePath, listener);
            }
        };

        public abstract Optional<FilePath> resolve(String filePath, AbstractBuild build, TaskListener listener);

        Optional<FilePath> resolve(FilePath filePath, TaskListener listener) {
            try {
                if (filePath.exists()) {
                    return Optional.of(filePath);
                }
            } catch (Exception ex) {
                String msg = String.format("Failed to find the file at (%s)%n%s", filePath.getRemote(), Throwables.getStackTraceAsString(ex));
                listener.error(msg);
            }
            return Optional.absent();
        }
    }
}
