package org.fabric3.assembly.factory;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Composite;
import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.RuntimeMode;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.dependency.Dependency;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.Closure;
import org.fabric3.assembly.utils.ClosureUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class ConfigurationBuilder {

    protected AssemblyConfig mConfig;

    protected ConfigurationBuilder(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    public static ConfigurationBuilder getBuilder() {
        return new ConfigurationBuilder(new AssemblyConfig());
    }

    public AssemblyConfig createConfiguration() {
        final List<String> serverNames = new ArrayList<String>();

        // collect all server names
        ClosureUtils.each(mConfig.getServers(), new Closure<Server>() {
            @Override
            public void exec(Server pParam) {
                serverNames.add(pParam.getServerName());
            }
        });

        // remove all bound server names
        ClosureUtils.each(mConfig.getRuntimes(), new Closure<Runtime>() {
            @Override
            public void exec(Runtime pParam) {
                serverNames.remove(pParam.getServerName());
            }
        });

        if (!serverNames.isEmpty()) {
            throw new ValidationException("At least one runtime is needed per server. These servers doesn't have any:" + serverNames);
        }

        return mConfig;
    }

    /*
    *
    *
    * Server
    *
    *
    */

    public ServerBuilder addServer(String pPath) {
        return addServer(Server.SERVER_DEFAULT_NAME, pPath);
    }

    public ServerBuilder addServer(String pName, String pPath) {
        return addServer(pName, pPath, null);
    }

    public ServerBuilder addServer(String pName, String pPath, Version pVersion) {
        return addServer(pName, pPath, pVersion, null);
    }

    public ServerBuilder addServer(String pName, String pPath, Version pVersion, UpdatePolicy pUpdatePolicy) {
        return new ServerBuilder(mConfig, new Server(pName, new File(pPath), pVersion, pUpdatePolicy));
    }

    public static class ServerBuilder extends ConfigurationBuilder {

        private Server mServer;

        public ServerBuilder(AssemblyConfig pConfig, Server pServer) {
            super(pConfig);
            mServer = pServer;

            mConfig.addServer(pServer);
        }

        public ServerBuilder withProfiles(String... pProfiles) {
            mServer.addProfileNames(pProfiles);
            return this;
        }

        public ServerBuilder withProfiles(Profile... pProfiles) {
            mServer.addProfiles(pProfiles);
            return this;
        }
    }

    /*
    *
    *
    * Runtime
    *
    *
    */

    public RuntimeBuilder addRuntime() {
        return addRuntime(RuntimeMode.VM, null);
    }

    public RuntimeBuilder addRuntime(String pRuntimeName) {
        return addRuntime(Server.SERVER_DEFAULT_NAME, pRuntimeName, RuntimeMode.VM, null, null);
    }

    public RuntimeBuilder addRuntime(RuntimeMode pMode) {
        return addRuntime(pMode, null);
    }

    public RuntimeBuilder addRuntime(RuntimeMode pMode, String pConfigFile) {
        return addRuntime(Runtime.RUNTIME_DEFAULT_NAME, pMode, pConfigFile);
    }

    public RuntimeBuilder addRuntime(String pRuntimeName, RuntimeMode pMode) {
        return addRuntime(Server.SERVER_DEFAULT_NAME, pRuntimeName, pMode, null, null);
    }

    public RuntimeBuilder addRuntime(String pRuntimeName, RuntimeMode pMode, String pConfigFile) {
        return addRuntime(Server.SERVER_DEFAULT_NAME, pRuntimeName, pMode, null, pConfigFile);
    }

    public RuntimeBuilder addRuntime(String pServerName, String pRuntimeName) {
        return addRuntime(pServerName, pRuntimeName, RuntimeMode.VM);
    }

    public RuntimeBuilder addRuntime(String pServerName, String pRuntimeName, RuntimeMode pMode) {
        return addRuntime(pServerName, pRuntimeName, pMode, null, null);
    }

    public RuntimeBuilder addRuntime(String pServerName, String pRuntimeName, RuntimeMode pMode, UpdatePolicy pUpdatePolicy, String pConfigFile) {
        return new RuntimeBuilder(mConfig, new Runtime(pServerName, pRuntimeName, pMode, pUpdatePolicy, null == pConfigFile ? null : new File(pConfigFile)));
    }

    public static class RuntimeBuilder extends ConfigurationBuilder {

        private Runtime mRuntime;

        public RuntimeBuilder(AssemblyConfig pConfig, Runtime pRuntime) {
            super(pConfig);
            mRuntime = pRuntime;

            mConfig.addRuntime(pRuntime);
        }

        public RuntimeBuilder withProfiles(String... pProfiles) {
            mRuntime.addProfileNames(pProfiles);
            return this;
        }

        public RuntimeBuilder withProfiles(Profile... pProfiles) {
            mRuntime.addProfiles(pProfiles);
            return this;
        }

        public RuntimeBuilder asType(RuntimeMode pRuntimeMode) {
            mRuntime.setRuntimeMode(pRuntimeMode);
            return this;
        }

        public RuntimeBuilder asType(String pRuntimeMode) {
            mRuntime.setRuntimeMode(RuntimeMode.valueOf(pRuntimeMode.toUpperCase()));
            return this;
        }

        public RuntimeBuilder configFile(File pConfigFile) {
            mRuntime.setSystemConfig(pConfigFile);
            return this;
        }

        public RuntimeBuilder configFile(String pConfigFile) {
            mRuntime.setSystemConfig(new File(pConfigFile));
            return this;
        }

        public RuntimeBuilder deployComposite(String pComposite) {
            mRuntime.addComposite(pComposite);
            return this;
        }

        public RuntimeBuilder deployComposite(String... pComposites) {
            mRuntime.addComposites(pComposites);
            return this;
        }

        public RuntimeBuilder deployComposite(Composite pComposite) {
            mRuntime.addComposite(pComposite);
            return this;
        }

        public RuntimeBuilder deployComposite(Composite... pComposites) {
            mRuntime.addComposites(pComposites);
            return this;
        }

    }

    /*
    *
    *
    * Update policy
    *
    *
    */

    public ConfigurationBuilder setUpdatePolicy(UpdatePolicy pPolicy) {
        mConfig.setUpdatePolicy(pPolicy.name());
        return this;
    }

    public ConfigurationBuilder setUpdatePolicy(String pPolicy) {
        mConfig.setUpdatePolicy(pPolicy);
        return this;
    }

    /*
     *
     *
     * Version
     *
     *
     */

    public ConfigurationBuilder setVersion(String pVersion) {
        mConfig.setVersion(new Version(pVersion));
        return this;
    }

    public ConfigurationBuilder setVersion(Version pVersion) {
        mConfig.setVersion(pVersion);
        return this;
    }

    /*
     *
     *
     * Profile
     *
     *
     */

    public ConfigurationBuilder setProfilesUpdatePolicy(UpdatePolicy pPolicy) {
        mConfig.getProfileConfig().setUpdatePolicy(pPolicy);
        return this;
    }

    public ConfigurationBuilder setProfilesUpdatePolicy(String pPolicy) {
        mConfig.getProfileConfig().setUpdatePolicy(UpdatePolicy.valueOf(pPolicy));
        return this;
    }

    public ConfigurationBuilder setProfilesVersion(String pVersion) {
        mConfig.getProfileConfig().setVersion(new Version(pVersion));
        return this;
    }

    public ConfigurationBuilder setProfilesVersion(Version pVersion) {
        mConfig.getProfileConfig().setVersion(pVersion);
        return this;
    }


    public ProfileBuilder addProfile(String pName) {
        return new ProfileBuilder(mConfig, new Profile(pName, null, null));
    }

    public ProfileBuilder addProfile(String pName, String... pAlternativeNames) {
        return new ProfileBuilder(mConfig, new Profile(pName, null, null, pAlternativeNames));
    }

    public ProfileBuilder addProfile(String pName, UpdatePolicy pUpdatePolicy, String... pAlternativeNames) {
        return new ProfileBuilder(mConfig, new Profile(pName, pUpdatePolicy, null, pAlternativeNames));
    }

    public ProfileBuilder addProfile(String pName, Version pVersion, String... pAlternativeNames) {
        return new ProfileBuilder(mConfig, new Profile(pName, null, pVersion, pAlternativeNames));
    }

    public ProfileBuilder addProfile(String pName, UpdatePolicy pUpdatePolicy, Version pVersion, String... pAlternativeNames) {
        return new ProfileBuilder(mConfig, new Profile(pName, pUpdatePolicy, pVersion, pAlternativeNames));
    }

    public static class ProfileBuilder extends ConfigurationBuilder {

        private Profile mProfile;

        public ProfileBuilder(AssemblyConfig pConfig, Profile pProfile) {
            super(pConfig);
            mProfile = pProfile;

            mConfig.addProfile(pProfile);
        }


        public ProfileBuilder dependency(String pDependency) {
            mProfile.addDependency(pDependency);
            return this;
        }

        public ProfileBuilder dependency(Dependency pDependency) {
            mProfile.addDependency(pDependency);
            return this;
        }

        public ProfileBuilder path(String pDependencyPath) {
            mProfile.addPath(pDependencyPath);
            return this;
        }

        public ProfileBuilder path(File pDependencyPath) {
            mProfile.addPath(pDependencyPath);
            return this;
        }
    }

    /*
     *
     *
     * Composites
     *
     *
     */

    public ConfigurationBuilder setCompositesUpdatePolicy(UpdatePolicy pUpdatePolicy) {
        mConfig.getCompositeConfig().setUpdatePolicy(pUpdatePolicy);
        return this;
    }

    public ConfigurationBuilder setCompositesUpdatePolicy(String pUpdatePolicy) {
        mConfig.getCompositeConfig().setUpdatePolicy(UpdatePolicy.valueOf(pUpdatePolicy));
        return this;
    }

    public CompositeBuilder addComposite(String pName, String pDependency) {
        return new CompositeBuilder(mConfig, new Composite(pName, pDependency, null));
    }

    public CompositeBuilder addComposite(String pName, File pFile) {
        return new CompositeBuilder(mConfig, new Composite(pName, pFile, null));
    }

    public static class CompositeBuilder extends ConfigurationBuilder {

        private Composite mComposite;

        public CompositeBuilder(AssemblyConfig pConfig, Composite pComposite) {
            super(pConfig);
            mComposite = pComposite;
        }

        public CompositeBuilder withUpdatePolicy(UpdatePolicy pUpdatePolicy) {
            mComposite.setUpdatePolicy(pUpdatePolicy);
            return this;
        }

        public CompositeBuilder withUpdatePolicy(String pUpdatePolicy) {
            mComposite.setUpdatePolicy(UpdatePolicy.valueOf(pUpdatePolicy));
            return this;
        }
    }

}
