package org.jenkinsci.plugins.spoontrigger.validation;

import com.google.common.base.Predicate;

import java.io.File;

public final class FileValidators {

    public static Validator<File> isFile(String failureMsg, Level level) {
        return new PredicateValidator<File>(Predicates.IS_FILE, failureMsg, level);
    }

    public static Validator<File> isDirectory(String failureMsg, Level level) {
        return new PredicateValidator<File>(Predicates.IS_DIRECTORY, failureMsg, level);
    }

    public static Validator<File> exists(String failureMsg, Level level) {
        return new PredicateValidator<File>(Predicates.EXISTS, failureMsg, level);
    }

    public static Validator<File> isPathAbsolute(String failureMsg, Level level) {
        return new PredicateValidator<File>(Predicates.IS_PATH_ABSOLUTE, failureMsg, level);
    }

    enum Predicates implements Predicate<File> {
        EXISTS {
            @Override
            public boolean apply(File file) {
                return file.exists();
            }
        },
        IS_PATH_ABSOLUTE {
            @Override
            public boolean apply(File file) {
                return file.isAbsolute();
            }
        },
        IS_FILE {
            @Override
            public boolean apply(File file) {
                return file.isFile();
            }
        },
        IS_DIRECTORY {
            @Override
            public boolean apply(File file) {
                return file.isDirectory();
            }
        };

        @Override
        public abstract boolean apply(File file);
    }
}
