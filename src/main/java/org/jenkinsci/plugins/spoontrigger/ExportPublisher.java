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
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;

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
        String msg = String.format("Failed to resolve output directory '%s'", directoryPath);
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
        checkState(this.outputDirectory != null, "output directory must not be null or empty");

        Optional<EnvVars> env = build.getEnv();

        checkState(env.isPresent(), "build environment variables must be provided");

        Optional<FilePath> directoryPath = FileResolver.create().env(env.get()).build(build).listener(listener).resolve(this.outputDirectory);

        checkState(directoryPath.isPresent(), "output directory '%s' does not exist", this.outputDirectory);

        try {
            checkState(directoryPath.get().isDirectory(), "output directory '%s' is not a path to a directory", this.outputDirectory);
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
                    FileValidators.exists("Specified directory does not exist", Level.ERROR),
                    FileValidators.isDirectory("Specified path does not point to a directory", Level.ERROR),
                    FileValidators.isPathAbsolute("Specified path should be absolute if the build will be executed on a remote machine", Level.WARNING));

            OUTPUT_DIRECTORY_STRING_VALIDATOR = StringValidators.isNotNull("Output directory is required", Level.ERROR);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return TypeToken.of(SpoonProject.class).isAssignableFrom(aClass);
        }

        public FormValidation doCheckOutputDirectory(@QueryParameter String value) throws IOException, ServletException {
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
