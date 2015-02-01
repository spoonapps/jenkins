package org.jenkinsci.plugins.spoontrigger.client;

import hudson.util.ArgumentListBuilder;

import java.util.regex.Pattern;

public class VersionCommand extends StringPatternCommand {

    private static final Pattern VERSION_PATTERN = Pattern.compile("\\s*Version:\\s+(\\S+)", Pattern.CASE_INSENSITIVE);

    private VersionCommand(ArgumentListBuilder argumentList) {
        super(argumentList, VERSION_PATTERN);
    }

    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    public static final class CommandBuilder {

        public VersionCommand build() {
            ArgumentListBuilder versionArgs = new ArgumentListBuilder(SPOON_CLIENT, "version");
            return new VersionCommand(versionArgs);
        }
    }
}
