package org.fabric3.host.runtime;

/**
 * Interface for mechanisms that are able to bootstrap a runtime.
 *
 * @version $Rev$ $Date$
 */
public interface Bootstrapper {
    /**
     * Bootstrap the supplied runtime.
     *
     * @param runtime         the runtime to boot
     * @param bootClassLoader the bootstrap classloader
     * @throws InitializationException if there was a problem bootstrapping the runtime
     */
    void bootstrap(Fabric3Runtime<?> runtime, ClassLoader bootClassLoader) throws InitializationException;
}
