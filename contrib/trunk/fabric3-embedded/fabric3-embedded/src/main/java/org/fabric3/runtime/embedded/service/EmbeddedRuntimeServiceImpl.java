package org.fabric3.runtime.embedded.service;

import org.fabric3.host.RuntimeMode;
import org.fabric3.runtime.embedded.api.EmbeddedRuntime;
import org.fabric3.runtime.embedded.api.service.EmbeddedRuntimeService;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class EmbeddedRuntimeServiceImpl implements EmbeddedRuntimeService {

    private ThreadGroup mRuntimesGroup;

    private List<EmbeddedRuntime> runtimes = new ArrayList<EmbeddedRuntime>();

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

        // there should be only one controller runtime in server
        if (RuntimeMode.CONTROLLER == runtime.getRuntimeMode()) {
            for (EmbeddedRuntime r : runtimes) {
                if (RuntimeMode.CONTROLLER == r.getRuntimeMode()) {
                    throw new EmbeddedFabric3SetupException("Server already contains CONTROLLER runtime.");
                }
            }
        }

        for (EmbeddedRuntime r : runtimes) {
            if (runtime.getName().equals(r.getName())) {
                throw new EmbeddedFabric3SetupException("Server already contains runtime with name: " + r.getName());
            }
        }

        runtimes.add(runtime);
    }

    public EmbeddedRuntime getDeploymentRuntime() {
        if (0 == runtimes.size()) {
            throw new EmbeddedFabric3SetupException("You have to specify at least one runtime to get deployment runtime.");
        }

        return runtimes.get(0);
    }

    public List<EmbeddedRuntime> getRuntimes() {
        return runtimes;
    }

}
