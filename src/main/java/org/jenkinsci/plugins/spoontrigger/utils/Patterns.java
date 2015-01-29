package org.jenkinsci.plugins.spoontrigger.utils;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

public final class Patterns {

    private static final Pattern WHITESPACE_BETWEEN_WORDS_PATTERN = Pattern.compile("\\S+\\s+\\S+");
    private static final Pattern VERSION_NUMBER_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+$");
    private static final Pattern REPOSITORY_NAME_PATTERN = Pattern.compile("^https?://([^/]+)/([^/]+)/([^/]+)$");

    public static boolean isSingleWord(@Nullable String value) {
        return Predicates.SINGLE_WORD.apply(value);
    }

    public static boolean isNullOrSingleWord(@Nullable String value) {
        return Predicates.NULL_OR_SINGLE_WORD.apply(value);
    }

    public static boolean isRepositoryName(@Nullable String value) {
        return Predicates.REPOSITORY_NAME.apply(value);
    }

    public static boolean isVersionNumber(@Nullable String value) {
        return Predicates.VERSION_NUMBER.apply(value);
    }

    public static boolean matches(String value, Pattern pattern) {
        return pattern.matcher(value).find();
    }

    public enum Predicates implements Predicate<String> {
        VERSION_NUMBER {
            @Override
            public boolean apply(@Nullable String value) {
                return value != null && matches(value, VERSION_NUMBER_PATTERN);
            }
        },
        SINGLE_WORD {
            @Override
            public boolean apply(@Nullable String value) {
                return value != null && !matches(value, WHITESPACE_BETWEEN_WORDS_PATTERN);
            }
        },
        NULL_OR_SINGLE_WORD {
            @Override
            public boolean apply(@Nullable String value) {
                return value == null || SINGLE_WORD.apply(value);
            }
        },
        REPOSITORY_NAME {
            @Override
            public boolean apply(@Nullable String value) {
                return value != null && matches(value, REPOSITORY_NAME_PATTERN);
            }
        };

        @Override
        public abstract boolean apply(@Nullable String value);
    }
}
