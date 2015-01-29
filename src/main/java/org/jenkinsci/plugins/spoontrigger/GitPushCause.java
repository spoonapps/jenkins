package org.jenkinsci.plugins.spoontrigger;

import hudson.triggers.SCMTrigger;
import lombok.Data;

@Data
public class GitPushCause extends SCMTrigger.SCMTriggerCause {

    private static final String EMPTY_POOLING_LOG = "";

    private final String newHeadId;
    private final String pusher;
    private final String repositoryUrl;

    public GitPushCause(String pusher, String repositoryUrl, String newHeadId) {
        super(EMPTY_POOLING_LOG);

        this.pusher = pusher;
        this.repositoryUrl = repositoryUrl;
        this.newHeadId = newHeadId;
    }

    @Override
    public String getShortDescription() {
        return String.format("New Head '%s' in '%s' repository pushed by '%s'",
                this.newHeadId, this.repositoryUrl, this.pusher);
    }
}
