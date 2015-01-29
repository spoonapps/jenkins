package org.jenkinsci.plugins.spoontrigger.validation;

import hudson.util.FormValidation;
import lombok.Data;

@Data
public final class ValidationException extends Exception {

    private final FormValidation failureMessage;

    public ValidationException(FormValidation failureMessage) {
        this.failureMessage = failureMessage;
    }
}