package org.jenkinsci.plugins.spoontrigger.client;

import hudson.util.ArgumentListBuilder;

abstract class VoidCommand extends BaseCommand {

    VoidCommand(ArgumentListBuilder argumentList) {
        super(argumentList);
    }

    public void run(SpoonClient client) throws IllegalStateException {
        client.launch(this.getArgumentList());
    }
}
