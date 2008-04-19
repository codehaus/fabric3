package org.fabric3.runtime.standalone.host.implementation.launched;

import org.fabric3.scdl.Implementation;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Property;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class RunCommandGenerator implements CommandGenerator {
    private GeneratorRegistry registry;
    private int order;

    public RunCommandGenerator(@Reference GeneratorRegistry registry, @Property(name = "order")int order) {
        this.registry = registry;
        this.order = order;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public RunCommand generate(LogicalComponent<?> component) throws GenerationException {

        return generateCommand(component, null);

    }

    /**
     * Performs a depth-first traversal of the component hierarchy to determine launched implementations.
     *
     * @param component the top-most component
     * @param command   the current run command or null
     * @return the updated run command or null if no launched implementations were found
     */
    private RunCommand generateCommand(LogicalComponent<?> component, RunCommand command) {

        if (component instanceof LogicalCompositeComponent) {
//            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
//            // perform a depth-first traversal
//            for (LogicalComponent<?> child : composite.getComponents()) {
//                command = generateCommand(child, command);
//            }
        }

        if (isLaunched(component)) {
            if (command == null) {
                command = new RunCommand(0);
            }
            command.addComponentUri(component.getUri());
        }
        return command;
    }

    private boolean isLaunched(LogicalComponent<?> component) {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        return Launched.class.isInstance(implementation);
    }

    public int getOrder() {
        return order;
    }

}

