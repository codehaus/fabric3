package org.fabric3.assembly.dependency;

/**
 * Version of profile or server.
 *
 * @author Michal Capo
 */
public class Version {

    private String mVersion;

    public Version(String pVersion) {
        this.mVersion = pVersion;
    }

    @Override
    public String toString() {
        return mVersion;
    }
}
