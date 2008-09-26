/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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

        return new LogicalComponent<TestImplementation>(null, null, componentDefinition, null);
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
    
    @SuppressWarnings({"serial"})
    private static class TestBinidingDefinition extends BindingDefinition {
        private TestBinidingDefinition() {
            super(new QName("binding.test"));
        }
    }
    
    @SuppressWarnings({"serial"})
    private static class TestComponentType extends AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property, ResourceDefinition> {
        
    }
    
    @SuppressWarnings({"serial"})
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
