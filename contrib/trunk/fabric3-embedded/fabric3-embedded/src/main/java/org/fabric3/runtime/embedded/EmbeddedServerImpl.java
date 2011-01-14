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

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ScanException;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.runtime.ant.api.TestRunner;
import org.fabric3.runtime.embedded.api.EmbeddedComposite;
import org.fabric3.runtime.embedded.api.EmbeddedRuntime;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.api.service.*;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;
import org.fabric3.runtime.embedded.service.*;
import org.fabric3.runtime.embedded.util.FileSystem;
import org.fabric3.runtime.embedded.util.IncreasableCountDownLatch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class EmbeddedServerImpl implements EmbeddedServer {

    private EmbeddedLoggerService mLoggerService;
    private EmbeddedUpdatePolicyService mUpdatePolicyService;
    private EmbeddedSharedFoldersService mSharedFoldersService;
    private EmbeddedProfileService mProfileService;
    private EmbeddedSetupService mSetupService;
    private EmbeddedRuntimeService mRuntimeService;

    private IncreasableCountDownLatch mCompositesLatch = new IncreasableCountDownLatch(0);

    public EmbeddedServerImpl() throws IOException, ScanException, EmbeddedFabric3StartupException {
        mLoggerService = new EmbeddedLoggerServiceImpl();
        mUpdatePolicyService = new EmbeddedUpdatePolicyServiceImpl();
        mSetupService = new EmbeddedSetupServiceImpl(mLoggerService);
        mRuntimeService = new EmbeddedRuntimeServiceImpl();
        mSharedFoldersService = new EmbeddedSharedFoldersServiceImpl(new MavenDependencyResolver(), mUpdatePolicyService, mLoggerService);
        mProfileService = new EmbeddedProfileServiceImpl(mSharedFoldersService);
    }

    public EmbeddedProfileService getProfileService() {
        return mProfileService;
    }

    public EmbeddedSharedFoldersService getSharedFoldersService() {
        return mSharedFoldersService;
    }

    public EmbeddedSetupService getSetupService() {
        return mSetupService;
    }

    public EmbeddedUpdatePolicyService getUpdatePolicyService() {
        return mUpdatePolicyService;
    }

    public EmbeddedRuntimeService getRuntimeService() {
        return mRuntimeService;
    }

    public EmbeddedLoggerService getLoggerService() {
        return mLoggerService;
    }

    public void initialize() {
        try {
            // initialing services
            mProfileService.initialize();
            mUpdatePolicyService.initialize();
            mSetupService.initialize();
            mRuntimeService.initialize();
            mProfileService.initialize();
            mSharedFoldersService.initialize();
        } catch (Exception e) {
            throw new EmbeddedFabric3SetupException("Could not initialize embedded server.", e);
        }
    }

    public void start() {
        long startTime = System.currentTimeMillis();

        try {
            ThreadGroup tGroup = mRuntimeService.getRuntimesGroup();

            if (0 == mRuntimeService.getRuntimesCount()) {
                throw new EmbeddedFabric3StartupException("Please specify at least one runtime. Cannot start empty server.");
            }

            final CountDownLatch latch = new CountDownLatch(mRuntimeService.getRuntimesCount());

            // start controller/VM
            mRuntimeService.getController().startRuntime();
            latch.countDown();

            // start participants
            Collection<EmbeddedRuntime> participants = mRuntimeService.getParticipants();
            if (null != participants && 0 != participants.size()) {
                for (final EmbeddedRuntime runtime : participants) {
                    new Thread(tGroup, runtime.getName()) {
                        @Override
                        public void run() {
                            try {
                                runtime.startRuntime();
                                latch.countDown();
                            } catch (IOException e) {
                                mLoggerService.log(String.format("Cannot start runtime %1$s", runtime.getName()), e);
                            } catch (InitializationException e) {
                                mLoggerService.log(String.format("Cannot start runtime %1$s", runtime.getName()), e);
                            }
                        }
                    }.start();
                }
            }

            latch.await();
            mLoggerService.log(MessageFormat.format("started in {0} seconds...", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)));
        } catch (Exception e) {
            throw new EmbeddedFabric3SetupException("Could not start embedded server.", e);
        }
    }

    public void stop() {
        final CountDownLatch latch = new CountDownLatch(mRuntimeService.getAllRuntimes().size());

        // loop over all available runtimes and shut them down
        for (EmbeddedRuntime runtime : mRuntimeService.getAllRuntimes()) {
            try {
                runtime.stopRuntime();
                latch.countDown();
            } catch (ShutdownException e) {
                mLoggerService.log("Exception on runtime shutdown.", e);
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            mLoggerService.log("Cannot stop server.", e);
        }

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(3));
        } catch (InterruptedException e) {
            mLoggerService.log("Cannot wait for server stopping.", e);
        }

        if (0 != mRuntimeService.getRuntimesGroup().activeCount()) {
            // force kill
            mRuntimeService.getRuntimesGroup().stop();
        }

        // if asked delete server folder
        if (mSetupService.shouldDeleteAtStop()) {
            FileSystem.delete(mSetupService.getServerFolder());
            mLoggerService.log("Deleting - " + mSetupService.getServerFolder().getAbsolutePath());
        }
    }

    public void installComposite(String path) {
        try {
            installComposite(new EmbeddedCompositeImpl(path));
        } catch (MalformedURLException e) {
            throw new EmbeddedFabric3StartupException("Cannot install composite", e);
        } catch (URISyntaxException e) {
            throw new EmbeddedFabric3StartupException("Cannot install composite", e);
        }
    }

    public void installComposite(EmbeddedComposite composite) {
        mCompositesLatch.increase();
        try {
            mRuntimeService.getController().installComposite(mCompositesLatch, composite);
        } catch (ContributionException e) {
            throw new EmbeddedFabric3StartupException("Cannot install composite", e);
        } catch (DeploymentException e) {
            throw new EmbeddedFabric3StartupException("Cannot install composite", e);
        }
    }

    public void executeTests() {
        runTestsOnRuntime(mRuntimeService.getController());
    }

    public void executeTestsOnRuntime(String runtimeName) {
        runTestsOnRuntime(mRuntimeService.getRuntimeByName(runtimeName));
    }

    private void runTestsOnRuntime(EmbeddedRuntime runtime) {
        mCompositesLatch.await();

        mLoggerService.log("Starting tests ...");
        runtime.getComponent(TestRunner.class, TestRunner.TEST_RUNNER_URI).executeTests();
    }
}
