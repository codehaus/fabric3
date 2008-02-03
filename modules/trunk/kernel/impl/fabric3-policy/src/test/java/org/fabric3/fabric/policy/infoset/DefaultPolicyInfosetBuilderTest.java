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
package org.fabric3.fabric.policy.infoset;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class DefaultPolicyInfosetBuilderTest extends TestCase {

    public void testBuildInfoSetLogicalBinding() {
        
        Element element = new DefaultPolicyInfosetBuilder().buildInfoSet(getTestBinding());
        
        assertEquals("service", element.getNodeName());
        assertEquals("testService", element.getAttribute("name"));
        assertEquals("binding.test", element.getFirstChild().getNodeName());
        assertEquals("component", element.getParentNode().getNodeName());
        assertEquals("testComponent", element.getParentNode().getAttributes().getNamedItem("name").getNodeValue());
        
    }

    public void testBuildInfoSetLogicalComponent() {
        
        Element element = new DefaultPolicyInfosetBuilder().buildInfoSet(getTestComponent());
        
        assertEquals("component", element.getNodeName());
        assertEquals("testComponent", element.getAttribute("name"));
        assertEquals("implementation.test", element.getFirstChild().getNodeName());
        
    }
    
    static LogicalComponent<?> getTestComponent() {
        
        TestComponentType componentType = new TestComponentType();
        TestImplementation implementation = new TestImplementation(componentType);
        ComponentDefinition<TestImplementation> componentDefinition = new ComponentDefinition<TestImplementation>("testComponent", implementation);
        LogicalComponent<TestImplementation> logicalComponent = new LogicalComponent<TestImplementation>(null, null, componentDefinition, null);
        
        return logicalComponent;
    }
    
    static LogicalBinding<?> getTestBinding() {
        
        TestComponentType componentType = new TestComponentType();
        TestImplementation implementation = new TestImplementation(componentType);
        ComponentDefinition<TestImplementation> componentDefinition = new ComponentDefinition<TestImplementation>("testComponent", implementation);
        LogicalComponent<TestImplementation> logicalComponent = new LogicalComponent<TestImplementation>(null, null, componentDefinition, null);
        
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("testService", null);
        LogicalReference logicalReference = new LogicalReference(null, referenceDefinition, logicalComponent);
        
        TestBinidingDefinition testBinidingDefinition = new TestBinidingDefinition();
        LogicalBinding<TestBinidingDefinition> logicalBinding = new LogicalBinding<TestBinidingDefinition>(testBinidingDefinition, logicalReference);
        logicalReference.addBinding(logicalBinding);
        
        return logicalBinding;
        
    }
    
    private static class TestBinidingDefinition extends BindingDefinition {
        private TestBinidingDefinition() {
            super(new QName("binding.test"));
        }
    }
    
    private static class TestComponentType extends AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property<?>, ResourceDefinition> {
        
    }
    
    private static class TestImplementation extends Implementation<TestComponentType> {
        
        private TestImplementation(TestComponentType componentType) {
            super(componentType);
        }
        
        @Override
        public QName getType() {
            return new QName("implementation.test");
        }
    }

}
