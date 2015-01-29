package org.jenkinsci.plugins.spoontrigger.validation;

public interface Validator<T> {
    void validate(T value) throws ValidationException;
}
