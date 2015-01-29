package org.jenkinsci.plugins.spoontrigger.client;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import hudson.console.LineTransformationOutputStream;
import lombok.Getter;
import org.jenkinsci.plugins.spoontrigger.utils.Patterns;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

class PatternGroupExtractorOutputStream extends LineTransformationOutputStream {

    private final PatternGroupExtractor groupExtractor;
    private final PrintStream out;
    private final Charset charset;

    @Getter
    private Optional<String> group;

    public PatternGroupExtractorOutputStream(Pattern pattern, PrintStream out, Charset charset) {
        this.out = out;
        this.charset = charset;
        this.group = Optional.absent();
        this.groupExtractor = new PatternGroupExtractor(pattern);
    }

    @Override
    protected void eol(byte[] bytes, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, length);
        String line = this.charset.decode(buffer).toString();
        this.out.print(line);

        if (this.group.isPresent()) {
            return;
        }

        this.group = this.groupExtractor.getValue(line);
    }

    private static final class PatternGroupExtractor {

        private static final Pattern CONTAINS_GROUP_PATTERN = Pattern.compile("[^\\\\]*\\(.*[^\\\\]\\)");

        private Pattern pattern;

        public PatternGroupExtractor(Pattern pattern) {
            checkArgument(pattern != null && Patterns.matches(pattern.toString(), CONTAINS_GROUP_PATTERN),
                    "pattern '%s' must be a not null regex with a matching group", pattern);

            this.pattern = pattern;
        }

        public Optional<String> getValue(String text) {
            if (Strings.isNullOrEmpty(text)) {
                return Optional.absent();
            }

            Matcher matcher = this.pattern.matcher(text);
            if (matcher.find() && matcher.groupCount() > 0) {
                return Optional.of(matcher.group(1));
            }

            return Optional.absent();
        }
    }
}
