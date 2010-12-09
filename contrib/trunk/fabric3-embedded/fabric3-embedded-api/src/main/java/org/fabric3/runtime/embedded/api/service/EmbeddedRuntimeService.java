package org.fabric3.runtime.embedded.api.service;

import org.fabric3.runtime.embedded.api.EmbeddedRuntime;

import java.util.List;

/**
 * @author Michal Capo
 */
public interface EmbeddedRuntimeService {

    ThreadGroup getRuntimesGroup();

    void initialize();

    void addRuntime(EmbeddedRuntime runtime);

    EmbeddedRuntime getDeploymentRuntime();

    List<EmbeddedRuntime> getRuntimes();

}
