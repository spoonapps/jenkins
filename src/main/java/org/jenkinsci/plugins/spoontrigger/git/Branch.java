package org.jenkinsci.plugins.spoontrigger.git;

import hudson.Util;
import lombok.Data;

import static com.google.common.base.Preconditions.checkArgument;

@Data
public final class Branch {
    static final int HEAD_LENGTH = 40;
    static final int HEAD_CHUNK_LENGTH = 6;

    private final String name;
    private final String head;

    public Branch(String name, String head) {
        checkArgument(Util.fixEmptyAndTrim(name) != null, "name must not be null or empty");
        checkArgument(Util.fixEmptyAndTrim(head) != null, "head must not be null or empty");
        checkArgument(head.length() == HEAD_LENGTH, "head must be %d characters long", HEAD_LENGTH);

        this.name = name;
        this.head = head;
    }

    public Branch(hudson.plugins.git.Branch branch) {
        this(branch.getName(), branch.getSHA1String());
    }

    public String getShortName() {
        int shortNameStart = this.name.lastIndexOf('/') + 1;
        if(shortNameStart == 0 || shortNameStart == this.name.length()) {
            return this.name;
        }
        return this.name.substring(shortNameStart);
    }

    public String getHeadChunk() {
        return this.head.substring(0, HEAD_CHUNK_LENGTH);
    }
}
