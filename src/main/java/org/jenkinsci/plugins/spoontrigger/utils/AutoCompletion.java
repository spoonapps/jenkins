package org.jenkinsci.plugins.spoontrigger.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import hudson.Util;
import hudson.model.AutoCompletionCandidates;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public final class AutoCompletion {

    private static final AutoCompletionCandidates NO_SUGGESTIONS = new AutoCompletionCandidates();

    private static final Predicate<File> IS_DIRECTORY = new IsDirectoryPredicate();
    private static final AutoCompletion INSTANCE = new AutoCompletion();

    private final FileSystemEnumerator fsEnumerator;

    private AutoCompletion() {
        this.fsEnumerator = new FileSystemEnumerator();
    }

    AutoCompletion(FileSystemEnumerator fsEnumerator) {
        this.fsEnumerator = fsEnumerator;
    }

    public static AutoCompletionCandidates suggestFiles(@Nullable String filePath) {
        Iterable<File> files = INSTANCE.listFileSystemItems(filePath);
        return createAutoCompletionList(files);
    }

    public static AutoCompletionCandidates suggestDirectories(@Nullable String filePath) {
        Iterable<File> files = INSTANCE.listFileSystemItems(filePath);
        Iterable<File> directories = Iterables.filter(files, IS_DIRECTORY);
        return createAutoCompletionList(directories);
    }

    private static boolean isNullOrRemotePath(String filePath) {
        return filePath == null || filePath.startsWith("\\");
    }

    private static AutoCompletionCandidates createAutoCompletionList(Iterable<File> files) {
        if (Iterables.size(files) == 0) {
            return NO_SUGGESTIONS;
        }

        AutoCompletionCandidates candidates = new AutoCompletionCandidates();
        for (File file : files) {
            try {
                String absolutePath = file.getAbsolutePath();
                candidates.add(absolutePath);
            } catch (SecurityException ex) {
                // no permissions to list a directory
            }
        }
        return candidates;
    }

    Collection<File> listFileSystemItems(@Nullable String filePath) {
        String normalizedFilePath = Util.fixEmptyAndTrim(filePath);
        if (isNullOrRemotePath(normalizedFilePath)) {
            return Collections.emptyList();
        }

        File candidateFile = new File(normalizedFilePath);
        if (candidateFile.exists()) {

            if (!candidateFile.isDirectory()) {
                return Collections.emptyList();
            }

            return this.fsEnumerator.listFiles(candidateFile);
        }

        File parentFile = candidateFile.getParentFile();
        if (parentFile == null || !parentFile.isDirectory()) {
            return Collections.emptyList();
        }

        String candidateFileName = candidateFile.getName();
        FileFilter fileFilter = new CommonPrefixFileFilter(candidateFileName);
        return this.fsEnumerator.listFiles(parentFile, fileFilter);
    }

    static class FileSystemEnumerator {

        private static Collection<File> wrap(@Nullable File[] files) {
            if (files == null || files.length == 0) {
                return Collections.emptyList();
            }
            return Arrays.asList(files);
        }

        Collection<File> listFiles(File file, FileFilter fileFilter) {
            final File[] files = file.listFiles(fileFilter);
            return wrap(files);
        }

        Collection<File> listFiles(File file) {
            final File[] files = file.listFiles();
            return wrap(files);
        }
    }

    private static final class IsDirectoryPredicate implements Predicate<File> {
        @Override
        public boolean apply(@Nullable File file) {
            return file != null && file.isDirectory();
        }
    }

    private static final class CommonPrefixFileFilter implements FileFilter {

        private final String commonPrefix;

        public CommonPrefixFileFilter(String commonPrefix) {
            this.commonPrefix = commonPrefix.toLowerCase(Locale.ROOT);
        }

        @Override
        public boolean accept(File file) {
            String lowercaseName = file.getName().toLowerCase(Locale.ROOT);
            return lowercaseName.startsWith(this.commonPrefix);
        }
    }
}
