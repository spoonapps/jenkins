package org.jenkinsci.plugins.spoontrigger;

import com.google.common.base.Optional;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import lombok.AccessLevel;
import lombok.Getter;
import org.jenkinsci.plugins.spoontrigger.client.SpoonClient;
import org.jenkinsci.plugins.spoontrigger.utils.TaskListeners;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;

abstract class SpoonBasePublisher extends Publisher {

    @Getter(AccessLevel.MODULE)
    private transient Optional<String> imageName = Optional.absent();

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> abstractBuild, BuildListener listener) {
        if (!(abstractBuild instanceof SpoonBuild)) {
            listener.fatalError("build must be an instance of %s class", SpoonBuild.class.getSimpleName());
            return false;
        }

        return super.prebuild(abstractBuild, listener);
    }

    protected void beforePublish(SpoonBuild build, BuildListener listener) throws IllegalStateException {
        Optional<Result> buildResult = Optional.fromNullable(build.getResult());
        checkState(buildResult.isPresent(), "%s requires a healthy build to continue."
                + " The result of current build is not available", this.getClass().getName());
        checkState(buildResult.get().isBetterThan(Result.FAILURE), "%s requires a healthy build to continue."
                + " Result of the current build is %s.", this.getClass().getName(), buildResult.get().toString());

        Optional<String> builtImage = build.getBuiltImage();

        checkState(builtImage.isPresent(), "built image name must be provided");

        this.imageName = builtImage;
    }

    protected abstract void publish(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IllegalStateException;

    @Override
    public final boolean perform(AbstractBuild<?, ?> abstractBuild, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        try {
            SpoonBuild build = (SpoonBuild) abstractBuild;
            this.beforePublish(build, listener);
            this.publish(build, launcher, listener);
            return true;
        } catch (IllegalStateException ex) {
            TaskListeners.logFatalError(listener, ex);
            return false;
        }
    }

    protected SpoonClient createClient(AbstractBuild<?, ?> abstractBuild, Launcher launcher, BuildListener listener) {
        SpoonBuild build = (SpoonBuild) abstractBuild;
        return SpoonClient.builder(build).launcher(launcher).listener(listener).build();
    }
}
