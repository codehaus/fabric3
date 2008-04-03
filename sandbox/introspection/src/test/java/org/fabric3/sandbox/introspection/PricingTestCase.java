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
package org.fabric3.sandbox.introspection;

import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.sandbox.introspection.impl.IntrospectionFactoryImpl;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.java.ImplementationProcessor;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeService;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.Property;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.binding.ws.scdl.WsBindingDefinition;
import org.fabric3.spi.assembly.ActivateException;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * @version $Rev$ $Date$
 */
public class PricingTestCase extends TestCase {
    private IntrospectionFactory factory;

    public void testLoadComposite() throws LoaderException {
        URL pricingComposite = getClass().getResource("/pricing/pricing.composite");
        IntrospectionContext context = new DefaultIntrospectionContext(getClass().getClassLoader(), null, pricingComposite);
        Loader loader = factory.getLoader();
        Composite composite = loader.load(pricingComposite, Composite.class, context);
        assertEquals(new QName("PricingSystem"), composite.getName());
        CompositeService pricingService = composite.getServices().get("PricingService");
        assertNotNull(pricingService);
        List<BindingDefinition> bindings = pricingService.getBindings();
        assertEquals(1, bindings.size());
        WsBindingDefinition binding = (WsBindingDefinition) bindings.get(0);
        assertEquals("http://localhost/pricing", binding.getTargetUri().toString());
    }

    public void testIntrospectJava() throws IntrospectionException {

        IntrospectionContext context = new DefaultIntrospectionContext(getClass().getClassLoader(), null, null);
        ImplementationProcessor<JavaImplementation> processor = factory.getImplementationProcessor(JavaImplementation.class);

        JavaImplementation impl = new JavaImplementation();
        impl.setImplementationClass("com.example.pricing.PricingServiceImpl");
        processor.introspect(impl, context);
        PojoComponentType componentType = impl.getComponentType();
        assertTrue(componentType.getServices().containsKey("PricingService"));
        Map<String,Property> properties = componentType.getProperties();
        Property name = properties.get("name");
        assertEquals(new QName(W3C_XML_SCHEMA_NS_URI, "string"), name.getXmlType());
        Property value = properties.get("value");
        assertEquals(new QName(W3C_XML_SCHEMA_NS_URI, "int"), value.getXmlType());
    }

    public void testValidate() throws LoaderException, ActivateException {
        // load the pricing composite
        URL pricingComposite = getClass().getResource("/pricing/pricing.composite");
        IntrospectionContext context = new DefaultIntrospectionContext(getClass().getClassLoader(), null, pricingComposite);
        Loader loader = factory.getLoader();
        Composite composite = loader.load(pricingComposite, Composite.class, context);

        factory.initializeContext(null);
        factory.validate(composite);
    }

    protected void setUp() throws Exception {
        super.setUp();

        factory = new IntrospectionFactoryImpl();
    }
}
