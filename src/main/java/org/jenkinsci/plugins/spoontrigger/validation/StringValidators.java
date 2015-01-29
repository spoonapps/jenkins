package org.jenkinsci.plugins.spoontrigger.validation;

import com.google.common.base.Predicate;
import org.jenkinsci.plugins.spoontrigger.utils.Patterns;

public final class StringValidators {

    public static Validator<String> isNull(String failureMsg, Level level) {
        return new PredicateValidator(Predicates.IS_NULL, failureMsg, level);
    }

    public static Validator<String> isNotNull(String failureMsg, Level level) {
        return new PredicateValidator(Predicates.IS_NOT_NULL, failureMsg, level);
    }

    public static Validator<String> isVersionNumber(String failureMsg, Level level) {
        return new PredicateValidator(Patterns.Predicates.VERSION_NUMBER, failureMsg, level);
    }

    public static Validator<String> isSingleWord(String failureMsg, Level level) {
        return new PredicateValidator(Patterns.Predicates.SINGLE_WORD, failureMsg, level);
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
