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
package org.fabric3.fabric.services.instancefactory;

import java.util.Map;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.InjectionSite;
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
        Map<InjectableAttribute,InjectionSite> mapping = providerDefinition.getInjectionSites();
        assertEquals(intSite, mapping.get(intProp));
        assertEquals(stringSite, mapping.get(stringProp));
    }

    protected void setUp() throws Exception {
        super.setUp();

        helper = new GenerationHelperImpl();
        componentType = new PojoComponentType(null);
        implementation = new MockImplementation(componentType);
        componentDefinition = new ComponentDefinition<MockImplementation>("mock", implementation);
        logicalComponent = new LogicalComponent<MockImplementation>(null, null, componentDefinition, null);
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
