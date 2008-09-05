package org.fabric3.spi.services.synthesize;

/**
 * Synthesizes and registers a component from an existing object instance in the runtime domain.
 *
 * @version $Revision$ $Date$
 */
public interface ComponentSynthesizer {

    /**
     * Synthesizes and registers a component from an existing object instance.
     *
     * @param name       the component name
     * @param type       the service contract type
     * @param instance   the implementation instance
     * @param introspect true if the SCA componentType should be introspected from the instance
     * @throws ComponentRegistrationException if an error occurs synthesizing the component
     */
    public <S, I extends S> void registerComponent(String name, Class<S> type, I instance, boolean introspect) throws ComponentRegistrationException;


}
