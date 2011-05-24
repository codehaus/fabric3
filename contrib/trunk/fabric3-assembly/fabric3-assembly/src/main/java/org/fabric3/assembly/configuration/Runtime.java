package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fabric3.assembly.dependency.UpdatePolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michal Capo
 */
public class Runtime {

    public static final String RUNTIME_DEFAULT_NAME = "vm";

    private String mServerName;

    private File mServerPath;

    private String mRuntimeName;

    private RuntimeMode mRuntimeMode;

    private File mSystemConfig;

    private List<Profile> mProfiles = new ArrayList<Profile>();

    private List<Composite> mComposites = new ArrayList<Composite>();

    private UpdatePolicy mUpdatePolicy;

    public Runtime(String pServerName, String pRuntimeName, RuntimeMode pRuntimeMode, UpdatePolicy pUpdatePolicy, File pSystemConfig, Profile... pProfiles) {
        if (null == pServerName) {
            mServerName = Server.SERVER_DEFAULT_NAME;
        } else {
            mServerName = pServerName;
        }
        if (null == pRuntimeName) {
            mRuntimeName = Runtime.RUNTIME_DEFAULT_NAME;
        } else {
            mRuntimeName = pRuntimeName;
        }
        mRuntimeMode = pRuntimeMode;
        mSystemConfig = pSystemConfig;
        mUpdatePolicy = pUpdatePolicy;
        if (null != pProfiles) {
            mProfiles = Arrays.asList(pProfiles);
        }
    }

    public void addComposite(Composite pComposite) {
        mComposites.add(pComposite);
    }

    public List<Composite> getComposites() {
        return mComposites;
    }

    public File getServerPath() {
        return mServerPath;
    }

    public void setServerPath(File pServerPath) {
        mServerPath = pServerPath;
    }

    public void setSystemConfig(File pSystemConfig) {
        mSystemConfig = pSystemConfig;
    }

    public String getServerName() {
        return mServerName;
    }

    public String getRuntimeName() {
        return mRuntimeName;
    }

    public RuntimeMode getRuntimeMode() {
        return mRuntimeMode;
    }

    public File getSystemConfig() {
        return mSystemConfig;
    }

    public List<Profile> getProfiles() {
        return mProfiles;
    }

    public UpdatePolicy getUpdatePolicy() {
        return mUpdatePolicy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("serverName", mServerName).
                append("runtimeName", mRuntimeName).
                append("runtimeMode", mRuntimeMode).
                append("systemConfig", mSystemConfig).
                append("profiles", mProfiles).
                append("updatePolicy", mUpdatePolicy).
                toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Runtime runtime = (Runtime) o;

        if (mRuntimeName != null ? !mRuntimeName.equals(runtime.mRuntimeName) : runtime.mRuntimeName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mRuntimeName != null ? mRuntimeName.hashCode() : 0;
    }
}
