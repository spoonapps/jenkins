package org.jenkinsci.plugins.spoontrigger;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.plugins.git.util.BuildData;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import lombok.Getter;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.spoontrigger.client.PushCommand;
import org.jenkinsci.plugins.spoontrigger.client.SpoonClient;
import org.jenkinsci.plugins.spoontrigger.git.PushCause;
import org.jenkinsci.plugins.spoontrigger.git.RemoteImageGenerator;
import org.jenkinsci.plugins.spoontrigger.utils.Patterns;
import org.jenkinsci.plugins.spoontrigger.validation.Level;
import org.jenkinsci.plugins.spoontrigger.validation.StringValidators;
import org.jenkinsci.plugins.spoontrigger.validation.Validator;
import org.jenkinsci.plugins.spoontrigger.validation.Validators;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;

public class PushPublisher extends SpoonBasePublisher {

    @Getter private final RemoteImageStrategy remoteImageStrategy;

    @Nullable
    @Getter private final String remoteImageName;

    @DataBoundConstructor
    public PushPublisher(@Nullable RemoteImageStrategy remoteImageStrategy, @Nullable String remoteImageName) {
        this.remoteImageStrategy = (remoteImageStrategy == null) ? RemoteImageStrategy.DO_NOT_USE : remoteImageStrategy;
        this.remoteImageName = Util.fixEmptyAndTrim(remoteImageName);
    }

    @Override
    public void beforePublish(SpoonBuild build, BuildListener listener) {
        super.beforePublish(build, listener);

        this.remoteImageStrategy.validate(this, build);
    }

    @Override
    public void publish(AbstractBuild<?, ?> abstractBuild, Launcher launcher, BuildListener listener) throws IllegalStateException {
        SpoonBuild build = (SpoonBuild) abstractBuild;
        SpoonClient client = super.createClient(build, launcher, listener);
        PushCommand pushCmd = this.createPushCommand(build);
        pushCmd.run(client);
    }

    private PushCommand createPushCommand(SpoonBuild spoonBuild) {
        PushCommand.CommandBuilder cmdBuilder = PushCommand.builder().image(super.getImageName().get());

        Optional<String> remoteImage = this.remoteImageStrategy.tryGetRemoteImage(this, spoonBuild);
        if (remoteImage.isPresent()) {
            cmdBuilder.remoteImage(remoteImage.get());
        }

        return cmdBuilder.build();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private static final Validator<String> REMOTE_IMAGE_NAME_VALIDATOR;

        static {
            REMOTE_IMAGE_NAME_VALIDATOR = Validators.chain(
                StringValidators.isNotNull("Parameter is required in the build", Level.ERROR),
                StringValidators.isSingleWord("Name of the image must be a single word", Level.ERROR));
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            try {
                JSONObject pushJSON = formData.getJSONObject("remoteImageStrategy");

                if (pushJSON == null || pushJSON.isNullObject()) {
                    new PushPublisher(RemoteImageStrategy.DO_NOT_USE, null);
                }

                String remoteImageStrategyName = pushJSON.getString("value");
                RemoteImageStrategy remoteImageStrategy = RemoteImageStrategy.valueOf(remoteImageStrategyName);
                String remoteImageName = (String) pushJSON.getOrDefault("remoteImageName", null);
                return new PushPublisher(remoteImageStrategy, remoteImageName);
            }catch (JSONException ex) {
                throw new IllegalStateException("Error while parsing form data", ex);
            }
        }


        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return TypeToken.of(SpoonProject.class).isAssignableFrom(aClass);
        }

        public FormValidation doCheckRemoteImageName(@QueryParameter String value) throws IOException, ServletException {
            String imageName = Util.fixEmptyAndTrim(value);
            return Validators.validate(REMOTE_IMAGE_NAME_VALIDATOR, imageName);
        }

        @Override
        public String getDisplayName() {
            return "Push Spoon image";
        }
    }

    private enum RemoteImageStrategy {
        DO_NOT_USE {
            @Override
            public Optional<String> tryGetRemoteImage(PushPublisher publisher, SpoonBuild build) {
                return Optional.absent();
            }
        },
        GENERATE_GIT {
            @Override
            public Optional<String> tryGetRemoteImage(PushPublisher publisher, SpoonBuild build) {
                PushCause cause = build.getCause(PushCause.class);
                if(cause != null) {
                    return Optional.of(RemoteImageGenerator.fromPush(cause));

                }

                BuildData buildData = build.getAction(BuildData.class);
                if(buildData != null) {
                    return Optional.of(RemoteImageGenerator.fromPull(buildData));
                }

                return Optional.absent();
            }

            @Override
            public void validate(PushPublisher publisher, SpoonBuild build) {
                super.validate(publisher, build);

                PushCause webHookCause = build.getCause(PushCause.class);
                if(webHookCause != null) {
                    return;
                }

                BuildData pullGitCause = build.getAction(BuildData.class);
                if(pullGitCause != null) {
                    return;
                }

                throw new IllegalStateException("Build has not been caused by a web hook event or pulling SCM");
            }
        },
        FIXED {
            @Override
            public Optional<String> tryGetRemoteImage(PushPublisher publisher, SpoonBuild build) {
                return Optional.of(publisher.getRemoteImageName());
            }

            @Override
            public void validate(PushPublisher publisher, SpoonBuild build) {
                super.validate(publisher, build);

                checkState(Patterns.isNullOrSingleWord(publisher.getRemoteImageName()),
                        "remote image name '%s' must be a single word or null", publisher.getRemoteImageName());
            }
        };

        public abstract Optional<String> tryGetRemoteImage(PushPublisher publisher, SpoonBuild build);
        public void validate(PushPublisher publisher, SpoonBuild build) { }
    }
}
