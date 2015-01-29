package org.jenkinsci.plugins.spoontrigger.validation;

import com.google.common.base.Predicate;
import hudson.util.FormValidation;

public class PredicateValidator<T> implements Validator<T> {

    private final String failureMsg;
    private final Level level;
    private final Predicate<T> predicate;

    public PredicateValidator(Predicate<T> predicate, String failureMsg, Level level) {
        this.predicate = predicate;
        this.failureMsg = failureMsg;
        this.level = level;
    }

    @Override
    public void validate(T value) throws ValidationException {
        if (this.predicate.apply(value)) {
            return;
        }

        FormValidation formValidation = this.level.createForm(this.failureMsg);
        throw new ValidationException(formValidation);
    }
}
