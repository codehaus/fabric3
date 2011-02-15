package org.fabric3.runtime.embedded.service;

import org.fabric3.host.Names;
import org.fabric3.host.contribution.*;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.runtime.ant.api.TestRunner;
import org.fabric3.runtime.embedded.EmbeddedCompositeImpl;
import org.fabric3.runtime.embedded.api.EmbeddedComposite;
import org.fabric3.runtime.embedded.api.EmbeddedRuntime;
import org.fabric3.runtime.embedded.api.service.EmbeddedLogger;
import org.fabric3.runtime.embedded.api.service.EmbeddedRuntimeManager;
import org.fabric3.runtime.embedded.api.service.EmbeddedSetup;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;
import org.fabric3.runtime.embedded.util.FileSystem;
import org.fabric3.runtime.embedded.util.IncreasableCountDownLatch;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal Capo
 */
public class EmbeddedRuntimeManagerImpl implements EmbeddedRuntimeManager {

    /**
     * Runtimes thread group.
     */
    private ThreadGroup mRuntimesGroup = new ThreadGroup("Embedded-fabric3-threads");

    /**
     * Controller/VM runtime.
     */
    private EmbeddedRuntime controller;

    /**
     * Participants runtime.
     */
    private Map<String, EmbeddedRuntime> runtimes = new HashMap<String, EmbeddedRuntime>();

    /**
     * Holds composites count.
     */
    private IncreasableCountDownLatch mCompositesLatch = new IncreasableCountDownLatch(0);

    /**
     * Logger.
     */
    private EmbeddedLogger mLogger;

    /**
     * Servers setup.
     */
    private EmbeddedSetup mSetup;

    public EmbeddedRuntimeManagerImpl(EmbeddedLogger pLogger, EmbeddedSetup pSetup) {
        mLogger = pLogger;
        mSetup = pSetup;
    }

    public void addRuntime(EmbeddedRuntime runtime) {
        if (null == runtime) {
            throw new EmbeddedFabric3SetupException("Runtime cannot be null.");
        }

        switch (runtime.getRuntimeMode()) {
            case VM:
            case CONTROLLER:
                // only one controller/vm is supported for domain
                if (null != controller) {
                    throw new EmbeddedFabric3SetupException("Server already contains CONTROLLER/VM runtime.");
                }
                controller = runtime;
                break;
            case PARTICIPANT:
                if (runtimes.containsKey(runtime.getName())) {
                    throw new EmbeddedFabric3SetupException("Server already contains runtime with name: " + runtime.getName());
                }
                runtimes.put(runtime.getName(), runtime);
                break;
            default:
                throw new EmbeddedFabric3SetupException(String.format("Unknown runtime mode: %s", runtime.getRuntimeMode()));
        }
    }

    public EmbeddedRuntime getController() {
        if (null == controller) {
            throw new EmbeddedFabric3SetupException("Controller runtime couldn't be found. Do you have some?");
        }

        return controller;
    }

    public Collection<EmbeddedRuntime> getParticipants() {
        return runtimes.values();
    }

    public int getRuntimesCount() {
        return runtimes.size() + (null == controller ? 0 : 1);
    }

    public EmbeddedRuntime getRuntimeByName(String name) {
        EmbeddedRuntime embeddedRuntime = runtimes.get(name);
        if (null == embeddedRuntime) {
            throw new EmbeddedFabric3SetupException(String.format("Runtime '%s' not found.", name));
        }

        return embeddedRuntime;
    }

    public void startRuntimes() {
        long startTime = System.currentTimeMillis();

        try {
            if (0 == getRuntimesCount()) {
                throw new EmbeddedFabric3StartupException("Please specify at least one runtime. Cannot start empty server.");
            }

            final CountDownLatch latch = new CountDownLatch(getRuntimesCount());

            // start controller/VM
            getController().startRuntime();
            latch.countDown();

            // start participants
            Collection<EmbeddedRuntime> participants = getParticipants();
            if (null != participants && 0 != participants.size()) {
                for (final EmbeddedRuntime runtime : participants) {
                    new Thread(mRuntimesGroup, runtime.getName()) {
                        @Override
                        public void run() {
                            try {
                                runtime.startRuntime();
                                latch.countDown();
                            } catch (InitializationException e) {
                                mLogger.log(String.format("Cannot start runtime %1$s", runtime.getName()), e);
                            }
                        }
                    }.start();
                }
            }

            latch.await();
            mLogger.log(MessageFormat.format("started in {0} seconds...", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)));
        } catch (Exception e) {
            throw new EmbeddedFabric3SetupException("Could not start embedded server.", e);
        }

    }

    public void stopRuntimes() {
        final CountDownLatch latch = new CountDownLatch(getRuntimesCount());

        // loop over all participant runtimes and shut them down
        for (EmbeddedRuntime runtime : getParticipants()) {
            try {
                runtime.shutdownRuntime();
                latch.countDown();
            } catch (Exception e) {
                mLogger.log("Exception on runtime shutdown.", e);
            }
        }

        // stop controller
        try {
            getController().shutdownRuntime();
            latch.countDown();
        } catch (Exception e) {
            mLogger.log("Exception on runtime shutdown.", e);
        }

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            mLogger.log("Cannot stop server.", e);
        }

        if (0 != mRuntimesGroup.activeCount()) {
            mLogger.log("Some of the embedded fabrics thread didn't stop. Will force stop them.");

            // force stop
            mRuntimesGroup.stop();
        }

        // if asked delete server folder
        if (mSetup.shouldDeleteAtStop()) {
            FileSystem.delete(mSetup.getServerFolder());
            mLogger.log("Deleting - " + mSetup.getServerFolder().getAbsolutePath());
        }
    }

    public EmbeddedComposite installComposite(String path) {
        try {
            EmbeddedCompositeImpl composite = new EmbeddedCompositeImpl(path);
            installComposite(composite);

            return composite;
        } catch (MalformedURLException e) {
            throw new EmbeddedFabric3StartupException("Cannot install composite", e);
        } catch (URISyntaxException e) {
            throw new EmbeddedFabric3StartupException("Cannot install composite", e);
        }

    }

    public void installComposite(EmbeddedComposite composite) {
        mCompositesLatch.increase();

        try {
            ContributionService contributionService = getController().getComponent(ContributionService.class, Names.CONTRIBUTION_SERVICE_URI);
            Domain domain = getController().getComponent(Domain.class, Names.APPLICATION_DOMAIN_URI);
            URI uri = contributionService.store(composite);
            contributionService.install(uri);

            // activate the deployable composite in the domain
            domain.include(Arrays.asList(uri));
        } catch (StoreException e) {
            throw new EmbeddedFabric3StartupException("Cannot install composite", e);
        } catch (InstallException e) {
            throw new EmbeddedFabric3StartupException("Cannot install composite", e);
        } catch (ContributionNotFoundException e) {
            throw new EmbeddedFabric3StartupException("Cannot install composite", e);
        } catch (DeploymentException e) {
            throw new EmbeddedFabric3StartupException("Cannot install composite", e);
        }

        mCompositesLatch.countDown();
    }

    public void uninstallComposite(EmbeddedComposite composite) {
        try {
            ContributionService contributionService = getController().getComponent(ContributionService.class, Names.CONTRIBUTION_SERVICE_URI);
            Domain domain = getController().getComponent(Domain.class, Names.APPLICATION_DOMAIN_URI);

            if (contributionService.exists(composite.getUri())) {
                domain.undeploy(composite.getUri(), false);
                contributionService.uninstall(composite.getUri());
                contributionService.remove(composite.getUri());
            }
        } catch (ContributionNotFoundException e) {
            throw new EmbeddedFabric3StartupException("Cannot uninstall composite", e);
        } catch (DeploymentException e) {
            throw new EmbeddedFabric3StartupException("Cannot uninstall composite", e);
        } catch (UninstallException e) {
            throw new EmbeddedFabric3StartupException("Cannot uninstall composite", e);
        } catch (RemoveException e) {
            throw new EmbeddedFabric3StartupException("Cannot uninstall composite", e);
        }

    }

    public void executeTests() {
        runTestsOnRuntime(getController());
    }

    public void executeTestsOnRuntime(String runtimeName) {
        runTestsOnRuntime(getRuntimeByName(runtimeName));
    }

    private void runTestsOnRuntime(EmbeddedRuntime runtime) {
        mCompositesLatch.await();

        mLogger.log("Starting tests ...");
        runtime.getComponent(TestRunner.class, TestRunner.TEST_RUNNER_URI).executeTests();
    }

}
