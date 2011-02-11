package org.fabric3.runtime.embedded.api.service;

import org.fabric3.runtime.embedded.api.EmbeddedComposite;
import org.fabric3.runtime.embedded.api.EmbeddedRuntime;

import java.util.Collection;

/**
 * Container for runtimes.
 *
 * @author Michal Capo
 */
public interface EmbeddedRuntimeManager {

    /**
     * Add runtime to this container.
     *
     * @param runtime to be added
     */
    void addRuntime(EmbeddedRuntime runtime);

    /**
     * Get controller holds in this manager.
     *
     * @return controller runtime
     */
    EmbeddedRuntime getController();

    /**
     * Get participants holds in this manager.
     *
     * @return collection of participants, can be empty
     */
    Collection<EmbeddedRuntime> getParticipants();

    /**
     * Count of all runtimes (Controllers and participants count).
     *
     * @return runtimes count
     */
    int getRuntimesCount();

    /**
     * Get runtime by given name.
     *
     * @param name of runtime you want to get
     * @return instance of runtime or exception if runtime was not found
     */
    EmbeddedRuntime getRuntimeByName(String name);

    /**
     * Start all runtime holds by this manager.
     */
    void startRuntimes();

    /**
     * Stop all runtimes holds by this manager.
     */
    void stopRuntimes();

    /**
     * Install composite specified by composites classpath path or his absolute path.
     *
     * @param path of composite you want to install
     * @return instance of Embedded composite which was installed
     */
    EmbeddedComposite installComposite(String path);

    /**
     * Install composite via his implementation.
     *
     * @param composite to be installed
     */
    void installComposite(EmbeddedComposite composite);

    /**
     * Uninstall composites.
     *
     * @param composite to be removed
     */
    void uninstallComposite(EmbeddedComposite composite);

    /**
     * Execute all tests on Controller runtime. This can be used only for Single mode.
     */
    void executeTests();

    /**
     * Execute all tests on specific runtime.
     *
     * @param runtimeName on which you want to run that tests
     */
    void executeTestsOnRuntime(String runtimeName);

}
