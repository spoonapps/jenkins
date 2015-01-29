package org.jenkinsci.plugins.spoontrigger.client;

import hudson.util.ArgumentListBuilder;

abstract class BaseCommand {

    protected static final String CONSOLE_APP = "spoon";
    private final ArgumentListBuilder argumentList;

    BaseCommand(ArgumentListBuilder argumentList) {
        this.argumentList = argumentList;
    }

    public ArgumentListBuilder getArgumentList() {
        return argumentList;
    }
}
