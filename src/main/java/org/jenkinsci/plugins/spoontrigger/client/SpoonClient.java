package org.jenkinsci.plugins.spoontrigger.client;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import org.jenkinsci.plugins.spoontrigger.SpoonBuild;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import static com.google.common.base.Preconditions.checkState;

public final class SpoonClient {

    private static final int NO_ERROR = 0;

    private EnvVars env;
    private FilePath pwd;
    private TaskListener listener;
    private Launcher launcher;

    @Getter(AccessLevel.PACKAGE)
    private Charset charset;

    public static ClientBuilder builder() {
        return new ClientBuilder();
    }

    public static ClientBuilder builder(SpoonBuild build) {
        return new ClientBuilder()
                .charset(build.getCharset())
                .env(build.getEnv().get())
                .pwd(build.getScript().get().getParent());
    }

    void launch(ArgumentListBuilder argumentList) throws IllegalStateException {
        this.launch(argumentList, this.getLogger());
    }

    void launch(ArgumentListBuilder argumentList, OutputStream out) throws IllegalStateException {
        int errorCode;
        try {
            errorCode = this.createLauncher().cmds(argumentList).stdout(out).join();
        } catch (IOException ex) {
            throw onLaunchFailure(argumentList, ex);
        } catch (InterruptedException ex) {
            throw onLaunchFailure(argumentList, ex);
        }

        if (errorCode != NO_ERROR) {
            throw new IllegalStateException("Process returned error code " + errorCode);
        }
    }

    PrintStream getLogger() {
        return this.listener.getLogger();
    }

    private IllegalStateException onLaunchFailure(ArgumentListBuilder args, Exception ex) {
        return new IllegalStateException("Execution of command: '" + args.toString() + "' failed", ex);
    }

    private Launcher.ProcStarter createLauncher() {
        return this.launcher.launch().pwd(this.pwd).envs(this.env);
    }

    public static class ClientBuilder {

        private final SpoonClient client;

        ClientBuilder() {
            this.client = new SpoonClient();
        }

        public ClientBuilder env(EnvVars environment) {
            this.client.env = environment;
            return this;
        }

        public ClientBuilder pwd(FilePath filePath) {
            this.client.pwd = filePath;
            return this;
        }

        public ClientBuilder listener(TaskListener listener) {
            this.client.listener = listener;
            return this;
        }

        public ClientBuilder launcher(Launcher launcher) {
            this.client.launcher = launcher;
            return this;
        }

        public ClientBuilder charset(Charset charset) {
            this.client.charset = charset;
            return this;
        }

        public SpoonClient build() {
            checkState(this.client.env != null, "env must be set");
            checkState(this.client.pwd != null, "pwd must be set");
            checkState(this.client.launcher != null, "launcher must be set");
            checkState(this.client.listener != null, "listener must be set");

            if (this.client.charset == null) {
                this.client.charset = Charset.defaultCharset();
            }

            return this.client;
        }
    }
}
