/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.web.generator;

import java.net.URI;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.binding.web.common.OperationsAllowed;
import org.fabric3.binding.web.model.WebBindingDefinition;
import org.fabric3.binding.web.provision.WebChannelBindingDefinition;
import org.fabric3.binding.web.provision.WebConnectionSourceDefinition;
import org.fabric3.binding.web.provision.WebConnectionTargetDefinition;
import org.fabric3.spi.generator.ConnectionBindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;

/**
 * Generates a {@link PhysicalConnectionSourceDefinition} for attaching a channel to a websocket or comet connection.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class WebConnectionBindingGenerator implements ConnectionBindingGenerator<WebBindingDefinition> {

    public PhysicalChannelBindingDefinition generateChannelBinding(LogicalBinding<WebBindingDefinition> binding) throws GenerationException {
        OperationsAllowed allowed = binding.getDefinition().getAllowed();
        return new WebChannelBindingDefinition(allowed);
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalConsumer consumer, LogicalBinding<WebBindingDefinition> binding) {
        URI channelUri = binding.getParent().getUri();
        return new WebConnectionSourceDefinition(consumer.getUri(), channelUri);
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalProducer producer, LogicalBinding<WebBindingDefinition> binding) {
        WebConnectionTargetDefinition definition = new WebConnectionTargetDefinition();
        URI channelUri = binding.getParent().getUri();
        definition.setTargetUri(channelUri);
        return definition;
    }

}