package org.fabric3.assembly.configuration;

import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class ProfileConfig {

    private Version mVersion;

    private UpdatePolicy mUpdatePolicy;

    private List<Profile> mProfiles = new ArrayList<Profile>();

    public Version getVersion() {
        return mVersion;
    }

    public void setVersion(Version pVersion) {
        mVersion = pVersion;
    }

    public UpdatePolicy getUpdatePolicy() {
        return mUpdatePolicy;
    }

    public void setUpdatePolicy(UpdatePolicy pUpdatePolicy) {
        mUpdatePolicy = pUpdatePolicy;
    }

    public void addProfile(Profile pProfile) {
        mProfiles.add(pProfile);
    }

    public List<Profile> getProfiles() {
        return mProfiles;
    }
}
