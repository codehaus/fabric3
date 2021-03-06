/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.implementation.drools.introspection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.model.type.component.ComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class RulesIntrospectorImplTestCase extends TestCase {
    private RulesIntrospectorImpl rulesIntrospector;
    private JavaContractProcessor contractProcessor;
    private IntrospectionContext context;
    private HashMap<String, Class<?>> services;

    public void testIntrospectPrimitiveProperty() throws Exception {
        ComponentType type = rulesIntrospector.introspect(services, Collections.<String, Class<?>>singletonMap("property", int.class), context);
        assertFalse(context.hasErrors());
        assertTrue(type.getProperties().containsKey("property"));
    }

    public void testIntrospectStringProperty() throws Exception {
        ComponentType type = rulesIntrospector.introspect(services, Collections.<String, Class<?>>singletonMap("property", String.class), context);
        assertFalse(context.hasErrors());
        assertTrue(type.getProperties().containsKey("property"));
    }

    public void testIntrospectCollectionProperty() throws Exception {
        ComponentType type = rulesIntrospector.introspect(services, Collections.<String, Class<?>>singletonMap("property", Map.class), context);
        assertFalse(context.hasErrors());
        assertTrue(type.getProperties().containsKey("property"));
    }

    public void testIntrospectArrayProperty() throws Exception {
        ComponentType type = rulesIntrospector.introspect(services, Collections.<String, Class<?>>singletonMap("property", String[].class), context);
        assertFalse(context.hasErrors());
        assertTrue(type.getProperties().containsKey("property"));
    }

    public void testIntrospectReference() throws Exception {
        EasyMock.expect(contractProcessor.introspect(EasyMock.eq(SomeService.class), EasyMock.eq(context))).andReturn(new JavaServiceContract());
        EasyMock.replay(contractProcessor);
        Map<String, Class<?>> globals = Collections.<String, Class<?>>singletonMap("reference", SomeService.class);
        ComponentType type = rulesIntrospector.introspect(services, globals, context);
        assertFalse(context.hasErrors());
        assertTrue(type.getReferences().containsKey("reference"));
        EasyMock.verify(contractProcessor);
    }

    public void testIntrospectArrayReference() throws Exception {
        EasyMock.expect(contractProcessor.introspect(EasyMock.eq(SomeService.class), EasyMock.eq(context))).andReturn(new JavaServiceContract());
        EasyMock.replay(contractProcessor);
        Map<String, Class<?>> globals = Collections.<String, Class<?>>singletonMap("reference", SomeService[].class);
        ComponentType type = rulesIntrospector.introspect(services, globals, context);
        assertFalse(context.hasErrors());
        assertTrue(type.getReferences().containsKey("reference"));
        EasyMock.verify(contractProcessor);
    }

    public void testIntrospectServices() throws Exception {
        services.put("service", SomeService.class);
        ComponentType type = rulesIntrospector.introspect(services, Collections.<String, Class<?>>emptyMap(), context);
        assertFalse(context.hasErrors());
        assertTrue(type.getServices().containsKey("service"));
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        contractProcessor = EasyMock.createMock(JavaContractProcessor.class);
        rulesIntrospector = new RulesIntrospectorImpl(contractProcessor);
        context = new DefaultIntrospectionContext();
        services = new HashMap<String, Class<?>>();
    }

    private interface SomeService {
        String invoke();
    }
}
