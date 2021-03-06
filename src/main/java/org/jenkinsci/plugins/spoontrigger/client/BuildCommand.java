package org.jenkinsci.plugins.spoontrigger.client;

import com.google.common.base.Optional;
import hudson.FilePath;
import hudson.Util;
import hudson.util.ArgumentListBuilder;
import org.jenkinsci.plugins.spoontrigger.utils.Patterns;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.jenkinsci.plugins.spoontrigger.Messages.*;

public final class BuildCommand extends StringPatternCommand {

    private static final Pattern OUTPUT_IMAGE_PATTERN = Pattern.compile("Output\\s+image:\\s+(\\S+)", Pattern.CASE_INSENSITIVE);

    private BuildCommand(ArgumentListBuilder argumentList) {
        super(argumentList, OUTPUT_IMAGE_PATTERN);
    }

    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    public static class CommandBuilder {

        private Optional<String> image = Optional.absent();
        private Optional<FilePath> script = Optional.absent();
        private Optional<String> vmVersion = Optional.absent();
        private Optional<String> containerWorkingDir = Optional.absent();
        private Optional<String> sourceContainer = Optional.absent();
        private Optional<String> sourceFolder = Optional.absent();
        private Optional<String> targetFolder = Optional.absent();
        private boolean diagnostic;
        private boolean noBase;
        private boolean overwrite;

        public CommandBuilder image(String image) {
            checkArgument(Patterns.isSingleWord(image), REQUIRE_SINGLE_WORD_SP, "image", image);

            this.image = Optional.of(image.trim());
            return this;
        }

        public CommandBuilder script(FilePath script) {
            checkArgument(script != null, REQUIRE_NOT_NULL_S, "script");

            this.script = Optional.of(script);
            return this;
        }

        public CommandBuilder vmVersion(String vmVersion) {
            checkArgument(Patterns.isVersionNumber(vmVersion), "vmVersion (%s) must consist of 4 numbers separated by dot", vmVersion);

            this.vmVersion = Optional.of(vmVersion.trim());
            return this;
        }

        public CommandBuilder containerWorkingDir(String containerWorkingDir) {
            checkArgument(Util.fixEmptyAndTrim(containerWorkingDir) != null, REQUIRE_NOT_NULL_OR_EMPTY_S, "containerWorkingDir");

            this.containerWorkingDir = Optional.of(containerWorkingDir.trim());
            return this;
        }

        public CommandBuilder mount(String sourceContainer, String sourceFolder, String targetFolder) {
            checkArgument(Util.fixEmptyAndTrim(sourceContainer) != null, REQUIRE_NOT_NULL_OR_EMPTY_S, "sourceContainer");

            this.mount(sourceFolder, targetFolder);
            this.sourceContainer = Optional.of(sourceContainer.trim());
            return this;
        }

        public CommandBuilder mount(String sourceFolder, String targetFolder) {
            checkArgument(Util.fixEmptyAndTrim(sourceFolder) != null, REQUIRE_NOT_NULL_OR_EMPTY_S, "sourceFolder");
            checkArgument(Util.fixEmptyAndTrim(targetFolder) != null, REQUIRE_NOT_NULL_OR_EMPTY_S, "targetFolder");

            this.sourceFolder = Optional.of(sourceFolder.trim());
            this.targetFolder = Optional.of(targetFolder.trim());
            return this;
        }

        public CommandBuilder diagnostic(boolean diagnostic) {
            this.diagnostic = diagnostic;
            return this;
        }

        public CommandBuilder noBase(boolean noBase) {
            this.noBase = noBase;
            return this;
        }

        public CommandBuilder overwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        public BuildCommand build() {
            checkState(this.script.isPresent(), REQUIRE_PRESENT_S, "script");

            ArgumentListBuilder buildArgs = new ArgumentListBuilder(SPOON_CLIENT, "build");
            if (this.image.isPresent()) {
                buildArgs.add("--name");
                buildArgs.add(this.image.get());
            }

            if (this.vmVersion.isPresent()) {
                buildArgs.add("--vm");
                buildArgs.add(this.vmVersion.get());
            }

            if (this.containerWorkingDir.isPresent()) {
                buildArgs.add("--working-dir");
                buildArgs.addQuoted(this.containerWorkingDir.get());
            }

            if (this.sourceFolder.isPresent()) {
                buildArgs.add("--mount");

                String mountLocation = String.format("\"%s=%s\"", this.sourceFolder.get(), this.targetFolder.get());
                if (this.sourceContainer.isPresent()) {
                    mountLocation = String.format("%s:%s", this.sourceContainer.get(), mountLocation);
                }
                buildArgs.add(mountLocation);
            }

            if (this.overwrite) {
                buildArgs.add("--overwrite");
            }

            if (this.noBase) {
                buildArgs.add("--no-base");
            }

            if (this.diagnostic) {
                buildArgs.add("--diagnostic");
            }

            buildArgs.addQuoted(this.script.get().getRemote());
            return new BuildCommand(buildArgs);
        }
    }
}
