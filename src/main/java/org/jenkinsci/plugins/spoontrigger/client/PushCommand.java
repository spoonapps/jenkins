package org.jenkinsci.plugins.spoontrigger.client;

import com.google.common.base.Optional;
import hudson.util.ArgumentListBuilder;
import org.jenkinsci.plugins.spoontrigger.utils.Patterns;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public final class PushCommand extends VoidCommand {

    PushCommand(ArgumentListBuilder argumentList) {
        super(argumentList);
    }

    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    public static final class CommandBuilder {

        private Optional<String> imageName = Optional.absent();
        private Optional<String> remoteImageName = Optional.absent();

        public CommandBuilder image(String image) {
            checkArgument(Patterns.isSingleWord(image), "image '%s' must be a single word", image);

            this.imageName = Optional.of(image.trim());
            return this;
        }

        public CommandBuilder remoteImage(String image) {
            checkArgument(Patterns.isSingleWord(image), "image '%s' must be a single word", image);

            this.remoteImageName = Optional.of(image.trim());
            return this;
        }

        public PushCommand build() {
            checkState(this.imageName.isPresent(), "image must be set");

            ArgumentListBuilder args = new ArgumentListBuilder(CONSOLE_APP, "push", this.imageName.get());
            if (this.remoteImageName.isPresent()) {
                args.add(this.remoteImageName.get());
            }

            return new PushCommand(args);
        }
    }
}
