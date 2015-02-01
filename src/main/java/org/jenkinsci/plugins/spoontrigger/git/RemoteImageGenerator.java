package org.jenkinsci.plugins.spoontrigger.git;

import hudson.plugins.git.Revision;
import hudson.plugins.git.util.BuildData;

import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public class RemoteImageGenerator {

    public static String fromPush(PushCause cause){
        return generateRemoteImageName(cause.getRepository(), cause.getBranch());
    }

    public static String fromPull(BuildData buildData) {
        Set<String> remoteUrls = buildData.getRemoteUrls();

        checkArgument(!remoteUrls.isEmpty(), "buildData does not contain any remote URLs");

        Revision lastBuiltRevision = buildData.getLastBuiltRevision();
        Collection<hudson.plugins.git.Branch> scmBranches = lastBuiltRevision.getBranches();

        checkArgument(!scmBranches.isEmpty(), "buildData last revision does not contain any branches");

        String firstRemoteUrl = remoteUrls.iterator().next();
        Repository repository = new Repository(firstRemoteUrl);

        hudson.plugins.git.Branch firstScmBranch = scmBranches.iterator().next();
        Branch branch = new Branch(firstScmBranch);

        return generateRemoteImageName(repository, branch);
    }

    private static String generateRemoteImageName(Repository repository, Branch branch) {
        return String.format("%s/%s:%s.%s", repository.getOrganization(), repository.getProject(), branch.getShortName(), branch.getHeadChunk());
    }
}
