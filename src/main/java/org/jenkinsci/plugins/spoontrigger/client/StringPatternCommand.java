package org.jenkinsci.plugins.spoontrigger.client;

import com.google.common.base.Optional;
import com.google.common.io.Closeables;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.util.regex.Pattern;

class StringPatternCommand extends BaseCommand {

    private final Pattern groupValuePattern;

    StringPatternCommand(ArgumentListBuilder argumentList, Pattern groupValuePattern) {
        super(argumentList);

        this.groupValuePattern = groupValuePattern;
    }

    public String run(SpoonClient client) throws IllegalStateException {
        PatternGroupExtractorOutputStream outputStream = new PatternGroupExtractorOutputStream(this.groupValuePattern,
                client.getLogger(), client.getCharset());
        try {
            client.launch(this.getArgumentList(), outputStream);
            Optional<String> group = outputStream.getGroup();

            if (group.isPresent()) {
                return group.get();
            }

            throw new IllegalStateException("Result was not found in output from the execution of '" + getArgumentList().toString() + "' command");
        } finally {
            try {
                final boolean swallowException = true;
                Closeables.close(outputStream, swallowException);
            } catch (IOException ex) {
                // no-op
            }
        }
    }
}
