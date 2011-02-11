/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.runtime.embedded;

import org.fabric3.host.RuntimeMode;
import org.fabric3.runtime.embedded.api.EmbeddedComposite;
import org.fabric3.runtime.embedded.api.EmbeddedProfile;
import org.fabric3.runtime.embedded.api.EmbeddedRuntime;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.api.service.EmbeddedLogger;
import org.fabric3.runtime.embedded.api.service.EmbeddedRuntimeManager;
import org.fabric3.runtime.embedded.api.service.EmbeddedSetup;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;
import org.fabric3.runtime.embedded.util.FileSystem;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmbeddedServerImpl implements EmbeddedServer {

    /**
     * Runtimes manager.
     */
    private EmbeddedRuntimeManager mRuntimeManager;

    /**
     * Logger;
     */
    private EmbeddedLogger mLogger;

    /**
     * Servers setup.
     */
    private EmbeddedSetup mSetup;

    /**
     * List of installed composites.
     */
    private Map<String, EmbeddedComposite> mInstalledComposites = new HashMap<String, EmbeddedComposite>();

    /**
     * Servers profiles.
     */
    private Map<String, EmbeddedProfile> mProfiles = new ConcurrentHashMap<String, EmbeddedProfile>();

    public EmbeddedServerImpl(EmbeddedRuntimeManager pRuntimeManager, EmbeddedLogger pLogger, EmbeddedSetup pSetup) {
        mRuntimeManager = pRuntimeManager;
        mLogger = pLogger;
        mSetup = pSetup;

        // add TEST profile to all runtimes
        mProfiles.put(Profile.TEST.getName(), Profile.TEST);
    }

    public Collection<EmbeddedProfile> getProfiles() {
        return mProfiles.values();
    }

    public void addProfile(EmbeddedProfile profile) {
        mProfiles.put(profile.getName(), profile);
    }

    public EmbeddedRuntime getController() {
        return mRuntimeManager.getController();
    }

    public boolean isVMMode() {
        return 1 == mRuntimeManager.getRuntimesCount() && null != mRuntimeManager.getController() && RuntimeMode.VM == mRuntimeManager.getController().getRuntimeMode();
    }

    public void start() {
        mLogger.log("starting in folder - " + mSetup.getServerFolder());

        if (mSetup.getServerFolder().exists()) {
            // cleanup existing directory structure
            mLogger.log("cleaning up - " + mSetup.getServerFolder().getAbsolutePath());

            for (File file : FileSystem.filesIn(FileSystem.folder(mSetup.getServerFolder(), "runtimes"))) {
                FileSystem.delete(FileSystem.filesIn(FileSystem.folder(file, "tmp")));
                FileSystem.delete(FileSystem.filesIn(FileSystem.folder(file, "data")));
                FileSystem.delete(FileSystem.filesIn(FileSystem.folder(file, "deploy")));
                FileSystem.delete(FileSystem.filesIn(FileSystem.folder(file, "repository/user")));
            }
        } else {
            // create runtimes folder
            FileSystem.createFolder(FileSystem.folder(mSetup.getServerFolder(), "runtimes"));
        }

        mRuntimeManager.startRuntimes();
    }

    public void stop() {
        mRuntimeManager.stopRuntimes();
    }

    public void deployComposite(String compositePath) {
        EmbeddedComposite composite = mRuntimeManager.installComposite(compositePath);
        mInstalledComposites.put(compositePath, composite);
    }

    public void deployComposites(String... compositesPaths) {
        for (String path : compositesPaths) {
            deployComposite(path);
        }
    }

    public void undeployComposite(String compositePath) {
        EmbeddedComposite composite = mInstalledComposites.get(compositePath);
        if (null == composite) {
            throw new EmbeddedFabric3StartupException(MessageFormat.format("Path ''{0}'' is not bound to any installed composite.", compositePath));
        }

        mRuntimeManager.uninstallComposite(composite);
    }

    public void undeployComposites(String... paths) {
        for (String path : paths) {
            undeployComposite(path);
        }
    }

    public String[] getDeployedComposites() {
        return mInstalledComposites.keySet().toArray(new String[mInstalledComposites.size()]);
    }

    public void undeployAll() {
        for (EmbeddedComposite composite : mInstalledComposites.values()) {
            mRuntimeManager.uninstallComposite(composite);
        }
    }

    public void redeployComposite(String compositePath) {
        undeployComposite(compositePath);
        deployComposite(compositePath);
    }

    public void redeployComposites(String... paths) {
        for (String path : paths) {
            deployComposite(path);
        }
    }

    public void redeployAll() {
        redeployComposites(mInstalledComposites.keySet().toArray(new String[1]));
    }

    public void executeTests() {
        mRuntimeManager.executeTests();
    }

    public void executeTestsOnRuntime(String runtimeName) {
        mRuntimeManager.executeTestsOnRuntime(runtimeName);
    }
}
