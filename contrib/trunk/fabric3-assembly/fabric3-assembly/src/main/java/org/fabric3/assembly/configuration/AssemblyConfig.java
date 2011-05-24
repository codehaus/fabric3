package org.fabric3.assembly.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fabric3.assembly.assembly.Assembly;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.exception.AssemblyException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class AssemblyConfig {

    private Version mVersion;

    private UpdatePolicy mUpdatePolicy = UpdatePolicy.DAILY;

    private List<Server> mServers = new ArrayList<Server>();

    private List<Runtime> mRuntimes = new ArrayList<Runtime>();

    private List<Composite> mComposites = new ArrayList<Composite>();

    private ConfigurationHelper mConfigurationHelper = new ConfigurationHelper() {
        @Override
        public List<Server> getServerConfigurations() {
            return mServers;
        }

        @Override
        public List<Runtime> getRuntimeConfigurations() {
            return mRuntimes;
        }

        @Override
        public Version getConfigurationVersion() {
            return AssemblyConfig.this.getVersion();
        }

        @Override
        public UpdatePolicy getConfigurationUpdatePolicy() {
            return AssemblyConfig.this.getUpdatePolicy();
        }
    };

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
        mComposites.add(pComposite);
    }

    public List<Composite> getComposites() {
        return mComposites;
    }

    public ConfigurationHelper getConfigurationHelper() {
        return mConfigurationHelper;
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

    public void doAssembly() {
        //TODO <capo> compute missing versions

        //TODO <capo> compute missing update policy

        //TODO <capo> validate configuration, servers, runtimes, composites and profiles
        AssemblyConfigValidator.validate(this);

        // do assembly
        new Assembly().doAssembly(this);
    }
}
