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
package org.fabric3.fabric.generator.resource;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.command.DisposeResourcesCommand;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.spi.generator.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalResourceDefinition;

/**
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class DisposeResourceCommandGeneratorTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testIncrementalBuild() throws Exception {
        ResourceGenerator<MockDefinition> resourceGenerator = EasyMock.createMock(ResourceGenerator.class);
        EasyMock.expect(resourceGenerator.generateResource(EasyMock.isA(LogicalResource.class))).andReturn(new MockPhysicalDefinition());
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(registry.getResourceGenerator(EasyMock.eq(MockDefinition.class))).andReturn(resourceGenerator);
        EasyMock.replay(registry, resourceGenerator);

        DisposeResourceCommandGenerator generator = new DisposeResourceCommandGenerator(registry, 0);

        LogicalCompositeComponent composite = new LogicalCompositeComponent(URI.create("component"), null, null);
        composite.setState(LogicalState.MARKED);
        LogicalResource resource = new LogicalResource(new MockDefinition(), composite);
        composite.addResource(resource);

        DisposeResourcesCommand command = generator.generate(composite, true);
        assertFalse(command.getDefinitions().isEmpty());

        EasyMock.verify(registry, resourceGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testIncrementalNoBuild() throws Exception {
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(registry);

        DisposeResourceCommandGenerator generator = new DisposeResourceCommandGenerator(registry, 0);

        LogicalCompositeComponent composite = new LogicalCompositeComponent(URI.create("component"), null, null);
        LogicalResource resource = new LogicalResource(new MockDefinition(), composite);
        composite.setState(LogicalState.PROVISIONED);
        composite.addResource(resource);

        assertNull(generator.generate(composite, true));

        EasyMock.verify(registry);
    }


    private class MockDefinition extends ResourceDefinition {
        private static final long serialVersionUID = -4013178193696275298L;
    }

    private class MockPhysicalDefinition extends PhysicalResourceDefinition {
        private static final long serialVersionUID = -4013178193696275298L;
    }
}
