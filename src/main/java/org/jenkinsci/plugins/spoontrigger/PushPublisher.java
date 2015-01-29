package org.jenkinsci.plugins.spoontrigger;

import com.google.common.reflect.TypeToken;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import lombok.Data;
import org.jenkinsci.plugins.spoontrigger.client.PushCommand;
import org.jenkinsci.plugins.spoontrigger.client.SpoonClient;
import org.jenkinsci.plugins.spoontrigger.utils.Patterns;
import org.jenkinsci.plugins.spoontrigger.validation.Level;
import org.jenkinsci.plugins.spoontrigger.validation.StringValidators;
import org.jenkinsci.plugins.spoontrigger.validation.Validator;
import org.jenkinsci.plugins.spoontrigger.validation.Validators;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;

@Data
public class PushPublisher extends SpoonBasePublisher {

    @Nullable
    private final String remoteImageName;

    @DataBoundConstructor
    public PushPublisher(String remoteImageName) {
        this.remoteImageName = Util.fixEmptyAndTrim(remoteImageName);
    }

    @Override
    public void beforePublish(SpoonBuild build, BuildListener listener) {
        super.beforePublish(build, listener);

        checkState(Patterns.isNullOrSingleWord(this.remoteImageName),
                "remote image name '%s' must be a single word or null", this.remoteImageName);
    }

    @Override
    public void publish(AbstractBuild<?, ?> abstractBuild, Launcher launcher, BuildListener listener) throws IllegalStateException {
        SpoonClient client = super.createClient(abstractBuild, launcher, listener);
        PushCommand pushCmd = this.createPushCommand();
        pushCmd.run(client);
    }

    private PushCommand createPushCommand() {
        PushCommand.CommandBuilder cmdBuilder = PushCommand.builder().image(super.getImageName().get());

        if (this.remoteImageName != null) {
            cmdBuilder.remoteImage(this.remoteImageName);
        }

        return cmdBuilder.build();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private static final Validator<String> REMOTE_IMAGE_NAME_VALIDATOR;

        static {
            REMOTE_IMAGE_NAME_VALIDATOR = Validators.chain(
                    StringValidators.isNotNull("Parameter will be ignored in the build", Level.WARNING),
                    StringValidators.isSingleWord("Name of the image must be a single word", Level.ERROR)
            );
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
}
