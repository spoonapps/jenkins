package org.jenkinsci.plugins.spoontrigger.validation;

import com.google.common.base.Predicate;
import org.jenkinsci.plugins.spoontrigger.utils.Patterns;

public final class StringValidators {

    public static Validator<String> isNotNull(String failureMsg, Level level) {
        return new PredicateValidator<String>(Predicates.IS_NOT_NULL, failureMsg, level);
    }

    public static Validator<String> isVersionNumber() {
        return new PredicateValidator<String>(Patterns.Predicates.VERSION_NUMBER, "Spoon VM version number should consist of 4 numbers separated by dot", Level.ERROR);
    }

    public static Validator<String> isSingleWord(String failureMsg) {
        return new PredicateValidator<String>(Patterns.Predicates.SINGLE_WORD, failureMsg, Level.ERROR);
    }

    enum Predicates implements Predicate<String> {
        IS_NULL {
            @Override
            public boolean apply(String value) {
                return value == null;
            }
        },
        IS_NOT_NULL {
            @Override
            public boolean apply(String value) {
                return value != null;
            }
        };

        @Override
        public abstract boolean apply(String s);
    }
}
