/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.generator.channel;

import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;

import org.fabric3.fabric.command.DisposeChannelsCommand;
import org.fabric3.fabric.generator.CommandGenerator;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 * Creates a command to remove channels defined in a composite from a runtime.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DisposeChannelCommandGenerator implements CommandGenerator {
    private int order;

    public DisposeChannelCommandGenerator(@Property(name = "order") int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public CompensatableCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (!(component instanceof LogicalCompositeComponent)) {
            return null;
        }
        LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
        List<PhysicalChannelDefinition> definitions = createDefinitions(composite);
        if (definitions.isEmpty()) {
            return null;
        }
        return new DisposeChannelsCommand(definitions);
    }

    private List<PhysicalChannelDefinition> createDefinitions(LogicalCompositeComponent composite) {
        List<PhysicalChannelDefinition> definitions = new ArrayList<PhysicalChannelDefinition>();
        for (LogicalChannel channel : composite.getChannels()) {
            if (channel.getState() == LogicalState.MARKED) {
                boolean sync = channel.getDefinition().getIntents().contains(ChannelIntents.SYNC_INTENT);
                PhysicalChannelDefinition definition = new PhysicalChannelDefinition(channel.getUri(), channel.getDeployable(), sync);
                definitions.add(definition);
            }
        }
        return definitions;
    }

}