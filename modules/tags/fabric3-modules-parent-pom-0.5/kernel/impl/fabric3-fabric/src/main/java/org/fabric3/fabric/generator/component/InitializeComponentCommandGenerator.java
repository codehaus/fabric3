/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.generator.component;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;

import org.fabric3.fabric.command.ComponentInitializationUri;
import org.fabric3.fabric.command.InitializeComponentCommand;
import org.fabric3.spi.generator.AddCommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Generates a command to initialize an atomic component marked to eagerly initialize.
 *
 * @version $Rev: 2767 $ $Date: 2008-02-15 13:29:02 +0000 (Fri, 15 Feb 2008) $
 */
@EagerInit
public class InitializeComponentCommandGenerator implements AddCommandGenerator {
    private final int order;

    public InitializeComponentCommandGenerator(@Property(name = "order")int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    @SuppressWarnings("unchecked")
    public InitializeComponentCommand generate(LogicalComponent<?> component) throws GenerationException {
        if (!(component instanceof LogicalCompositeComponent) && !component.isProvisioned() && component.isEagerInit()) {
            URI groupId = URI.create(component.getParent().getUri().toString() + "/");
            ComponentInitializationUri uri = new ComponentInitializationUri(groupId, component.getUri());
            return new InitializeComponentCommand(order, uri);
        }
        return null;
    }

}
