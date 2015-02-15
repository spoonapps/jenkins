package org.jenkinsci.plugins.spoontrigger;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import lombok.Getter;
import org.jenkinsci.plugins.spoontrigger.client.ExportCommand;
import org.jenkinsci.plugins.spoontrigger.client.SpoonClient;
import org.jenkinsci.plugins.spoontrigger.utils.AutoCompletion;
import org.jenkinsci.plugins.spoontrigger.utils.FileResolver;
import org.jenkinsci.plugins.spoontrigger.validation.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;
import static org.jenkinsci.plugins.spoontrigger.Messages.*;

public class ExportPublisher extends SpoonBasePublisher {

    @Getter
    private final String outputDirectory;
    @Nullable
    private transient FilePath runtimeOutputDirectory;

    @DataBoundConstructor
    public ExportPublisher(String outputDirectory) {
        this.outputDirectory = Util.fixEmptyAndTrim(outputDirectory);
    }

    private static IllegalStateException onFailedResolveOutputDirectory(String directoryPath, Exception ex) {
        String msg = String.format(FAILED_RESOLVE_SP, "output directory", directoryPath);
        return new IllegalStateException(msg, ex);
    }

    @Override
    public void beforePublish(SpoonBuild build, BuildListener listener) throws IllegalStateException {
        super.beforePublish(build, listener);

        this.runtimeOutputDirectory = this.resolveOutputDirectory(build, listener);
    }

    @Override
    public void publish(AbstractBuild<?, ?> abstractBuild, Launcher launcher, BuildListener listener) throws IllegalStateException {
        SpoonClient client = super.createClient(abstractBuild, launcher, listener);
        ExportCommand exportCmd = this.createExportCommand();
        exportCmd.run(client);
    }

    private FilePath resolveOutputDirectory(SpoonBuild build, TaskListener listener) throws IllegalStateException {
        checkState(this.outputDirectory != null, REQUIRE_NOT_NULL_OR_EMPTY_S, "output directory");

        Optional<EnvVars> env = build.getEnv();

        checkState(env.isPresent(), REQUIRE_PRESENT_S, "build environment variables");

        Optional<FilePath> directoryPath = FileResolver.create().env(env.get()).build(build).listener(listener).resolve(this.outputDirectory);

        checkState(directoryPath.isPresent(), DOES_NOT_EXIST_SP, "output directory", this.outputDirectory);

        try {
            checkState(directoryPath.get().isDirectory(), PATH_NOT_POINT_TO_ITEM_SPS, "output directory", this.outputDirectory, "a directory");
        } catch (IOException ex) {
            throw onFailedResolveOutputDirectory(this.outputDirectory, ex);
        } catch (InterruptedException ex) {
            throw onFailedResolveOutputDirectory(this.outputDirectory, ex);
        }

        return directoryPath.get();
    }

    private ExportCommand createExportCommand() {
        return ExportCommand.builder().outputDirectory(this.runtimeOutputDirectory).image(this.getImageName().get()).build();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private static final Validator<File> OUTPUT_DIRECTORY_FILE_VALIDATOR;
        private static final Validator<String> OUTPUT_DIRECTORY_STRING_VALIDATOR;

        static {
            OUTPUT_DIRECTORY_FILE_VALIDATOR = Validators.chain(
                    FileValidators.exists(String.format(DOES_NOT_EXIST_S, "Directory")),
                    FileValidators.isDirectory(String.format(PATH_NOT_POINT_TO_ITEM_S, "a directory")),
                    FileValidators.isPathAbsolute(PATH_SHOULD_BE_ABSOLUTE, Level.WARNING));

            OUTPUT_DIRECTORY_STRING_VALIDATOR = StringValidators.isNotNull(REQUIRED_PARAMETER, Level.ERROR);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return TypeToken.of(SpoonProject.class).isAssignableFrom(aClass);
        }

        public FormValidation doCheckOutputDirectory(@QueryParameter String value) {
            String filePath = Util.fixEmptyAndTrim(value);
            try {
                OUTPUT_DIRECTORY_STRING_VALIDATOR.validate(filePath);
                File outputDirectory = new File(filePath);
                OUTPUT_DIRECTORY_FILE_VALIDATOR.validate(outputDirectory);
                return FormValidation.ok();
            } catch (ValidationException ex) {
                return ex.getFailureMessage();
            }
        }

        public AutoCompletionCandidates doAutoCompleteOutputDirectory(@QueryParameter String value) {
            return AutoCompletion.suggestDirectories(value);
        }

        @Override
        public String getDisplayName() {
            return "Export Spoon image";
        }
    }
}
