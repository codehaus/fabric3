package org.fabric3.runtime.embedded.api;

import java.util.Collection;

/**
 * Server which will contain common profiles, a reference to controller, reference to participants (if any). You can
 * start and stop the server.
 *
 * @author Michal Capo
 */
public interface EmbeddedServer {

    /**
     * Get profiles same for all runtimes.
     *
     * @return common profiles for all runtimes
     */
    Collection<EmbeddedProfile> getProfiles();

    /**
     * Adding profile to server will cause adding this profile to all runtimes within this server.
     *
     * @param profile to be added to all runtimes
     */
    void addProfile(EmbeddedProfile profile);

    /**
     * Get controller runtime for this domain.
     *
     * @return controller runtime
     */
    EmbeddedRuntime getController();

    /**
     * Is this domain running in VM mode.
     *
     * @return <code>true</code> if domain have only one runtime and this is a VM, otherwise return <code>false</code>
     */
    boolean isVMMode();

    /**
     * Start the server. Will start all runtimes and installing extensions on them.
     */
    void start();

    /**
     * Will stop all runtimes and at the end it will stop the server.
     */
    void stop();

    /**
     * Install embedded composite.
     *
     * @param compositePath of composite to be installed. Can be absolute or classpath relative
     */
    void deployComposite(String compositePath);

    /**
     * Make deployed composites available to use. This is useful due to correct dependency resolving.
     */
    void activeAllDeployedComposites();

    /**
     * Install more composites at one time.
     *
     * @param compositesPaths comma separate composites classpath
     */
    void deployComposites(String... compositesPaths);

    /**
     * Get list of installed composites.
     *
     * @return installed composites classpath
     */
    String[] getDeployedComposites();

    /**
     * Uninstall composite.
     *
     * @param compositePath classpath of composite to be removed
     */
    void undeployComposite(String compositePath);

    /**
     * Uninstall given composites.
     *
     * @param paths classpath of composites to be removed
     */
    void undeployComposites(String... paths);

    void undeployAll();

    /**
     * Redeploy given composite.
     *
     * @param compositePath to be redeployed
     */
    void redeployComposite(String compositePath);

    /**
     * Redeploy given composites.
     *
     * @param paths of composites to be redeployed
     */
    void redeployComposites(String... paths);

    /**
     * Redeploy all installed composites.
     */
    void redeployAll();

    /**
     * Execute all tests on VM runtime.
     */
    void executeTests();

    /**
     * Execute all tests on specific runtime.
     *
     * @param runtimeName to run tests on
     */
    void executeTestsOnRuntime(String runtimeName);
}
