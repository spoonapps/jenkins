package org.jenkinsci.plugins.spoontrigger.client;

import com.google.common.base.Optional;
import hudson.Util;
import hudson.util.ArgumentListBuilder;
import hudson.util.Secret;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public final class LoginCommand extends VoidCommand {

    LoginCommand(ArgumentListBuilder argumentList) {
        super(argumentList);
    }

    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    public static final class CommandBuilder {

        private Optional<String> login = Optional.absent();
        private Optional<Secret> password = Optional.absent();

        public CommandBuilder login(String login) {
            checkArgument(Util.fixEmptyAndTrim(login) != null, "login '%s' must be a nonempty string", login);

            this.login = Optional.of(login.trim());
            return this;
        }

        public CommandBuilder password(Secret password) {
            checkArgument(password != null, "password must not be null");

            this.password = Optional.of(password);
            return this;
        }

        public LoginCommand build() {
            checkState(this.login.isPresent(), "login must be set");
            checkState(this.password.isPresent(), "password must be set");

            ArgumentListBuilder loginArgs = new ArgumentListBuilder(CONSOLE_APP, "login");
            loginArgs.add(this.login.get());
            loginArgs.addMasked(this.password.get());
            return new LoginCommand(loginArgs);
        }
    }
}
