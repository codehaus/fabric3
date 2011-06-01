package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.utils.FileUtils;

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

    private File mRuntimePath;

    private String mRuntimeName;

    private RuntimeMode mRuntimeMode;

    private File mSystemConfig;

    private List<Profile> mProfiles = new ArrayList<Profile>();

    private List<String> mProfileNames = new ArrayList<String>();

    private List<Composite> mComposites = new ArrayList<Composite>();

    private List<String> mCompositeNames = new ArrayList<String>();

    private UpdatePolicy mUpdatePolicy;

    public Runtime(String pServerName, String pRuntimeName, RuntimeMode pRuntimeMode, UpdatePolicy pUpdatePolicy, File pSystemConfig, String... pProfileNames) {
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
        if (null != pProfileNames) {
            mProfileNames.addAll(Arrays.asList(pProfileNames));
        }
    }

    public void addComposite(Composite pComposite) {
        mComposites.add(pComposite);
    }

    public void addComposites(Composite... pComposites) {
        if (null != pComposites) {
            mComposites.addAll(Arrays.asList(pComposites));
        }
    }

    public void addComposite(String pCompositeName) {
        mCompositeNames.add(pCompositeName);
    }

    public void addComposites(String... pCompositeNames) {
        if (null != pCompositeNames) {
            mCompositeNames.addAll(Arrays.asList(pCompositeNames));
        }
    }

    public List<Composite> getComposites() {
        return mComposites;
    }

    public List<String> getCompositeNames() {
        return mCompositeNames;
    }

    public File getServerPath() {
        return mServerPath;
    }

    public void setServerPath(File pServerPath) {
        mServerPath = pServerPath;
        mRuntimePath = FileUtils.file(pServerPath, "runtimes", mRuntimeName);
    }

    public void setSystemConfig(File pSystemConfig) {
        mSystemConfig = pSystemConfig;
    }

    public File getRuntimeExtensionFolder() {
        return FileUtils.file(mRuntimePath, "repository", "runtime");
    }

    public File getDeployFolder() {
        return FileUtils.file(mRuntimePath, "deploy");
    }

    public void setServerName(String pServerName) {
        mServerName = pServerName;
    }

    public String getServerName() {
        return mServerName;
    }

    public String getRuntimeName() {
        return mRuntimeName;
    }

    public void setRuntimeMode(RuntimeMode pRuntimeMode) {
        mRuntimeMode = pRuntimeMode;
    }

    public RuntimeMode getRuntimeMode() {
        return mRuntimeMode;
    }

    public File getSystemConfig() {
        return mSystemConfig;
    }

    public void addProfile(Profile pProfile) {
        mProfiles.add(pProfile);
    }

    public void addProfiles(Profile... pProfiles) {
        if (null != pProfiles) {
            mProfiles.addAll(Arrays.asList(pProfiles));
        }
    }

    public List<Profile> getProfiles() {
        return mProfiles;
    }

    public void addProfileNames(String... pProfiles) {
        if (null != pProfiles) {
            mProfileNames.addAll(Arrays.asList(pProfiles));
        }
    }

    public List<String> getProfileNames() {
        return mProfileNames;
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

        return !(mRuntimeName != null ? !mRuntimeName.equals(runtime.mRuntimeName) : runtime.mRuntimeName != null);
    }

    @Override
    public int hashCode() {
        return mRuntimeName != null ? mRuntimeName.hashCode() : 0;
    }
}
