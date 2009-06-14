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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.model.type.component;

import java.net.URI;
import java.util.Collection;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.model.type.java.InjectingComponentType;

/**
 * @version $Rev$ $Date$
 */
public class CompositeTestCase extends TestCase {
    private QName name;
    private TestServiceContract autowireContract;

    public void testAutowireTargets() {
        InjectingComponentType ct1 = new InjectingComponentType();
        ct1.add(new ServiceDefinition("service1", autowireContract));
        TestImplementation impl1 = new TestImplementation();
        impl1.setComponentType(ct1);
        ComponentDefinition<TestImplementation> component1 = new ComponentDefinition<TestImplementation>("component1", impl1);

        Composite composite = new Composite(name);
        composite.add(component1);

        Collection<URI> targets = composite.getTargets(autowireContract);
        assertEquals(1, targets.size());
        assertTrue(targets.contains(URI.create("component1#service1")));
    }

    protected void setUp() throws Exception {
        super.setUp();
        name = new QName("name");
        autowireContract = new TestServiceContract(AutowireContract.class);
    }

    private static interface AutowireContract {
    }
}
