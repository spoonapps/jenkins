package org.jenkinsci.plugins.spoontrigger.utils;

import org.apache.commons.codec.binary.Base64;
import org.jenkinsci.main.modules.instance_identity.InstanceIdentity;

import javax.annotation.Nullable;
import java.security.interfaces.RSAPublicKey;

public class Identity {
    static final String DEFAULT = "NO_IDENTITY_KEY";

    public static boolean isDefault(@Nullable String identity) {
        return DEFAULT.equals(identity);
    }

    public static String getDefault() {
        return DEFAULT;
    }

    public static String getValueOrDefault(@Nullable InstanceIdentity identity) {
        try {
            RSAPublicKey key = identity.getPublic();
            byte[] encodedKey = Base64.encodeBase64(key.getEncoded());
            return new String(encodedKey);
        } catch (NullPointerException ex) {
            return DEFAULT;
        }
    }
}
