package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fabric3.assembly.IAssemblyStep;
import org.fabric3.assembly.completition.AssemblyConfigCompletion;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class AssemblyConfig implements IAssemblyStep {

    private Version mVersion;

    private UpdatePolicy mUpdatePolicy = UpdatePolicy.DAILY;

    private List<Server> mServers = new ArrayList<Server>();

    private List<Runtime> mRuntimes = new ArrayList<Runtime>();

    private CompositeConfig mComposites = new CompositeConfig();

    private ProfileConfig mProfiles = new ProfileConfig();

    private boolean mAlreadyProcessed = false;

    public boolean isAlreadyProcessed() {
        return mAlreadyProcessed;
    }

    public void setUpdatePolicy(String pUpdatePolicy) {
        mUpdatePolicy = UpdatePolicy.valueOf(pUpdatePolicy);
    }

    public UpdatePolicy getUpdatePolicy() {
        if (null == mUpdatePolicy) {
            mUpdatePolicy = UpdatePolicy.DAILY;
        }

        return mUpdatePolicy;
    }

    public void addServer(Server pServer) {
        mServers.add(pServer);
    }

    public List<Server> getServers() {
        return mServers;
    }

    public void addRuntime(Runtime pRuntime) {
        mRuntimes.add(pRuntime);
    }

    public List<Runtime> getRuntimes() {
        return mRuntimes;
    }

    public void addComposite(Composite pComposite) {
        mComposites.addComposite(pComposite);
    }

    public List<Composite> getComposites() {
        return mComposites.getComposites();
    }

    public CompositeConfig getCompositeConfig() {
        return mComposites;
    }

    public void addProfile(Profile pProfile) {
        mProfiles.addProfile(pProfile);
    }

    public List<Profile> getProfiles() {
        return mProfiles.getProfiles();
    }

    public ProfileConfig getProfileConfig() {
        return mProfiles;
    }

    public Version getVersion() {
        if (null == mVersion) {
            throw new AssemblyException("No mVersion is specified in your configuration, please do so.");
        }

        return mVersion;
    }

    public void setVersion(Version pVersion) {
        this.mVersion = pVersion;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("version", mVersion).
                append("updatePolicy", mUpdatePolicy).
                toString();
    }

    public void process() {
        LoggerUtils.log("assembling started at ''{0,time}''", System.currentTimeMillis());

        mAlreadyProcessed = true;
        new AssemblyConfigCompletion(this).process();
    }
}
