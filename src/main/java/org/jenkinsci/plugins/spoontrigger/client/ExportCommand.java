package org.jenkinsci.plugins.spoontrigger.client;

import com.google.common.base.Optional;
import hudson.FilePath;
import hudson.util.ArgumentListBuilder;
import org.jenkinsci.plugins.spoontrigger.utils.Patterns;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public final class ExportCommand extends VoidCommand {

    ExportCommand(ArgumentListBuilder argumentList) {
        super(argumentList);
    }

    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    public static final class CommandBuilder {

        private Optional<FilePath> outputDirectory = Optional.absent();
        private Optional<String> image = Optional.absent();

        public CommandBuilder outputDirectory(FilePath outputDirectory) {
            checkArgument(outputDirectory != null, "outputDirectory must be set");

            this.outputDirectory = Optional.of(outputDirectory);
            return this;
        }

        public CommandBuilder image(String image) {
            checkArgument(Patterns.isSingleWord(image), "image '%s' must be a single word", image);

            this.image = Optional.of(image);
            return this;
        }

        public ExportCommand build() {
            checkState(this.image.isPresent(), "image must be set");
            checkState(this.outputDirectory.isPresent(), "outputDirectory must be set");

            ArgumentListBuilder buildArgs = new ArgumentListBuilder(CONSOLE_APP, "export", this.image.get());
            buildArgs.addQuoted(this.outputDirectory.get().getRemote());
            return new ExportCommand(buildArgs);
        }
    }
}
