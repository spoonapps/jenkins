package org.jenkinsci.plugins.spoontrigger.validation;

import hudson.util.FormValidation;

public enum Level {

    OK {
        @Override
        public FormValidation createForm(String message) {
            return FormValidation.ok(message);
        }
    },
    WARNING {
        @Override
        public FormValidation createForm(String message) {
            return FormValidation.warning(message);
        }
    },
    ERROR {
        @Override
        public FormValidation createForm(String message) {
            return FormValidation.error(message);
        }
    };

    public abstract FormValidation createForm(String message);
}