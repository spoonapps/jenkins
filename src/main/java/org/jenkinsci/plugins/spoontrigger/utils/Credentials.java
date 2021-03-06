package org.jenkinsci.plugins.spoontrigger.utils;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.UsernameCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.google.common.base.Optional;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import jenkins.model.Jenkins;

import java.util.List;

public final class Credentials {

    private static final DomainRequirement ANY_DOMAIN = new DomainRequirement();

    public static <T extends UsernameCredentials> Optional<T> lookupById(Class<T> aClass, String credentialId) {
        List<T> availableCredentials = lookupByItemGroup(aClass, Jenkins.getInstance());
        return Optional.fromNullable(CredentialsMatchers.firstOrNull(availableCredentials, CredentialsMatchers.withId(credentialId)));
    }

    public static <T extends UsernameCredentials> Optional<T> lookupById(Class<T> aClass, Item item, String credentialId) {
        List<T> availableCredentials = lookupByItem(aClass, item);
        return Optional.fromNullable(CredentialsMatchers.firstOrNull(availableCredentials, CredentialsMatchers.withId(credentialId)));
    }

    public static <T extends UsernameCredentials> List<T> lookupByItem(Class<T> aClass, Item item) {
        return CredentialsProvider.lookupCredentials(aClass, item, ACL.SYSTEM, ANY_DOMAIN);
    }

    private static <T extends UsernameCredentials> List<T> lookupByItemGroup(Class<T> aClass, ItemGroup itemGroup) {
        return CredentialsProvider.lookupCredentials(aClass, itemGroup, ACL.SYSTEM, ANY_DOMAIN);
    }
}
