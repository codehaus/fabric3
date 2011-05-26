package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michal Capo
 */
public class Server {

    public static final String SERVER_DEFAULT_NAME = "default-server";

    private String mServerName;

    private File mServerPath;

    private List<String> mProfileNames = new ArrayList<String>();

    private List<Profile> mProfiles = new ArrayList<Profile>();

    private Version mVersion;

    private UpdatePolicy mUpdatePolicy;

    public Server(String pServerName, File pServerPath, Version pVersion, UpdatePolicy pUpdatePolicy, String... pProfileNames) {
        if (null == pServerName) {
            this.mServerName = SERVER_DEFAULT_NAME;
        } else {
            this.mServerName = pServerName;
        }
        mServerPath = pServerPath;
        mVersion = pVersion;
        mUpdatePolicy = pUpdatePolicy;
        if (null != pProfileNames) {
            mProfileNames.addAll(Arrays.asList(pProfileNames));
        }
    }

    public void setServerName(String pServerName) {
        mServerName = pServerName;
    }

    public String getServerName() {
        return mServerName;
    }

    public File getServerPath() {
        return mServerPath;
    }

    public void setServerPath(File pServerPath) {
        mServerPath = pServerPath;
    }

    public void addProfile(Profile pProfile) {
        mProfiles.add(pProfile);
    }

    public void addProfiles(Profile... pProfiles) {
        if (null != mProfiles) {
            mProfiles.addAll(Arrays.asList(pProfiles));
        }
    }

    public void addProfileNames(String... pProfiles) {
        if (null != pProfiles) {
            mProfileNames.addAll(Arrays.asList(pProfiles));
        }
    }

    public List<Profile> getProfiles() {
        return mProfiles;
    }

    public List<String> getProfileNames() {
        return mProfileNames;
    }

    public void setVersion(Version pVersion) {
        mVersion = pVersion;
    }

    public Version getVersion() {
        return mVersion;
    }

    public void setUpdatePolicy(UpdatePolicy pUpdatePolicy) {
        mUpdatePolicy = pUpdatePolicy;
    }

    public UpdatePolicy getUpdatePolicy() {
        return mUpdatePolicy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("serverName", mServerName).
                append("serverPath", mServerPath).
                append("profiles", mProfiles).
                append("version", mVersion).
                append("updatePolicy", mUpdatePolicy).
                toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Server server = (Server) o;

        if (mServerName != null ? !mServerName.equals(server.mServerName) : server.mServerName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mServerName != null ? mServerName.hashCode() : 0;
    }
}
