package org.fabric3.runtime.standalone.host.implementation.launched;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.command.CommandSet;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.type.Implementation;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class RunCommandGenerator implements CommandGenerator {
    private GeneratorRegistry registry;

    public RunCommandGenerator(@Reference GeneratorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public void generate(LogicalComponent<?> component, GeneratorContext context) throws GenerationException {
        RunCommand command = generateCommand(component, null);
        if (command != null) {
            CommandSet set = context.getCommandSet();
            assert set != null;
            set.add(CommandSet.Phase.LAST, command);
        }
    }

    /**
     * Performs a depth-first traversal of the component hierarchy to determine launched implementations.
     *
     * @param component the top-most component
     * @param command   the current run command or null
     * @return the updated run command or null if no launched implementations were found
     */
    private RunCommand generateCommand(LogicalComponent<?> component, RunCommand command) {
        // perform a depth-first traversal
        for (LogicalComponent<?> child : component.getComponents()) {
            command = generateCommand(child, command);
        }
        if (isLaunched(component)) {
            if (command == null) {
                command = new RunCommand();
            }
            command.addComponentUri(component.getUri());
        }
        return command;
    }

    private boolean isLaunched(LogicalComponent<?> component) {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        return Launched.class.isInstance(implementation);
    }

}
