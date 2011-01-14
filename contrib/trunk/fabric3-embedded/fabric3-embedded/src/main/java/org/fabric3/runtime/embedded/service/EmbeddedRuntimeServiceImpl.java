package org.fabric3.runtime.embedded.service;

import org.fabric3.runtime.embedded.api.EmbeddedRuntime;
import org.fabric3.runtime.embedded.api.service.EmbeddedRuntimeService;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michal Capo
 */
public class EmbeddedRuntimeServiceImpl implements EmbeddedRuntimeService {

    private ThreadGroup mRuntimesGroup;

    /**
     * Controller/VM runtime.
     */
    private EmbeddedRuntime controller;

    /**
     * Participants runtime.
     */
    private Map<String, EmbeddedRuntime> runtimes = new HashMap<String, EmbeddedRuntime>();

    public ThreadGroup getRuntimesGroup() {
        return mRuntimesGroup;
    }

    public void initialize() {
        mRuntimesGroup = new ThreadGroup("Embedded-fabric3-threads");
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

    public Collection<EmbeddedRuntime> getAllRuntimes() {
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
}
