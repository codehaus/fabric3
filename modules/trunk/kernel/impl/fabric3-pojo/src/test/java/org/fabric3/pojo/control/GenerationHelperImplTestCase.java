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
package org.fabric3.pojo.control;

import java.util.Map;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.java.ConstructorInjectionSite;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectableAttributeType;
import org.fabric3.model.type.java.InjectionSite;
import org.fabric3.model.type.java.Signature;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Rev$ $Date$
 */
public class GenerationHelperImplTestCase extends TestCase {

    private InstanceFactoryGenerationHelper helper;
    private InstanceFactoryDefinition providerDefinition;
    private LogicalComponent<MockImplementation> logicalComponent;
    private ComponentDefinition<MockImplementation> componentDefinition;
    private MockImplementation implementation;
    private PojoComponentType componentType;
    private InjectableAttribute intProp;
    private InjectableAttribute stringProp;

    public void testSimpleConstructor() {
        Signature constructor = new Signature("Test", "int", "String");
        ConstructorInjectionSite intSite = new ConstructorInjectionSite(constructor, 0);
        ConstructorInjectionSite stringSite = new ConstructorInjectionSite(constructor, 1);
        componentType.setConstructor(constructor);
        componentType.addInjectionSite(intProp, intSite);
        componentType.addInjectionSite(stringProp, stringSite);
        helper.processInjectionSites(logicalComponent, providerDefinition);
        Map<InjectionSite, InjectableAttribute> mapping = providerDefinition.getConstruction();
        assertEquals(intProp, mapping.get(intSite));
        assertEquals(stringProp, mapping.get(stringSite));
        assertTrue(providerDefinition.getPostConstruction().isEmpty());
        assertTrue(providerDefinition.getReinjection().isEmpty());
    }

    protected void setUp() throws Exception {
        super.setUp();

        helper = new GenerationHelperImpl();
        componentType = new PojoComponentType(null);
        implementation = new MockImplementation(componentType);
        componentDefinition = new ComponentDefinition<MockImplementation>("mock", implementation);
        logicalComponent = new LogicalComponent<MockImplementation>(null, componentDefinition, null);
        providerDefinition = new InstanceFactoryDefinition();

        intProp = new InjectableAttribute(InjectableAttributeType.PROPERTY, "intProp");
        stringProp = new InjectableAttribute(InjectableAttributeType.PROPERTY, "stringProp");
    }

    private static class MockImplementation extends Implementation<PojoComponentType> {
        private MockImplementation(PojoComponentType componentType) {
            super(componentType);
        }

        public QName getType() {
            return null;
        }
    }
}
