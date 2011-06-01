package org.fabric3.assembly.modifier;

import org.fabric3.assembly.assembly.AssemblyComposite;
import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Composite;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.factory.AssemblyConfigBuilder;
import org.fabric3.assembly.utils.ConfigUtils;

import java.io.File;

/**
 * @author Michal Capo
 */
public class AssemblyModifierCompositeBuilder extends AssemblyConfigBuilder {

    private Composite mComposite;

    private AssemblyComposite mAssemblyComposite;

    public AssemblyModifierCompositeBuilder(AssemblyConfig pConfig, String pName) {
        super(pConfig);
        mComposite = ConfigUtils.findCompositeByName(pConfig, pName);
        mAssemblyComposite = new AssemblyComposite(pConfig);
    }

    public AssemblyModifierCompositeBuilder deployToServer(String pServerName) {
        Server server = ConfigUtils.findServerByName(mConfig, pServerName);
        if (!server.getComposites().contains(mComposite)) {
            server.addComposite(mComposite.getName());
        }

        mAssemblyComposite.doAssembly(mComposite);

        return this;
    }

    public AssemblyModifierCompositeBuilder undeploy() {
        File fileLocation = ConfigUtils.computeDeployPath(mConfig, mComposite);
        if (fileLocation.exists()) {
            if (!fileLocation.delete()) {
                throw new AssemblyException("Cannot removeFromServer composite: {0}", fileLocation.getAbsolutePath());
            }
        }

        return this;
    }

    public AssemblyModifierCompositeBuilder redeploy() {
        try {
            undeploy();
            deployToServer(ConfigUtils.findServerByComposite(mConfig, mComposite).getServerName());
        } catch (AssemblyException e) {
            throw new AssemblyException("Composite ''{0}'' is not deployed. You cannot redeploy it.", mComposite.getName());
        }

        return this;
    }

}
