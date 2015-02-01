package org.jenkinsci.plugins.spoontrigger.validation;

import com.google.common.base.Predicate;

import java.io.File;

import static org.jenkinsci.plugins.spoontrigger.Messages.PATH_SHOULD_BE_ABSOLUTE;

public final class FileValidators {

    public static Validator<File> isFile(String failureMsg) {
        return new PredicateValidator<File>(Predicates.IS_FILE, failureMsg, Level.ERROR);
    }

    public static Validator<File> isDirectory(String failureMsg) {
        return new PredicateValidator<File>(Predicates.IS_DIRECTORY, failureMsg, Level.ERROR);
    }

    public static Validator<File> exists(String failureMsg) {
        return new PredicateValidator<File>(Predicates.EXISTS, failureMsg, Level.ERROR);
    }

    public static Validator<File> isPathAbsolute() {
        return new PredicateValidator<File>(Predicates.IS_PATH_ABSOLUTE, PATH_SHOULD_BE_ABSOLUTE, Level.WARNING);
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
