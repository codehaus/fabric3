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
package org.fabric3.implementation.system.introspection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.Namespaces;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.implementation.system.model.SystemImplementation;

/**
 * @version $Rev$ $Date$
 */
public class SystemImplementationLoaderTestCase extends TestCase {

    public static final QName SYSTEM_IMPLEMENTATION = new QName(Namespaces.IMPLEMENTATION, "implementation.system");
    private IntrospectionContext context;
    private XMLStreamReader reader;
    private ImplementationProcessor<SystemImplementation> implementationProcessor;
    private SystemImplementationLoader loader;

    public void testLoad() throws Exception {
        implementationProcessor.introspect(EasyMock.isA(SystemImplementation.class), EasyMock.eq(context));
        EasyMock.replay(implementationProcessor);

        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getName()).andReturn(SYSTEM_IMPLEMENTATION);
        EasyMock.expect(reader.getAttributeValue(null, "class")).andReturn(getClass().getName());
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(reader);

        SystemImplementation impl = loader.load(reader, context);
        assertEquals(getClass().getName(), impl.getImplementationClass());
        EasyMock.verify(reader);
        EasyMock.verify(context);
        EasyMock.verify(implementationProcessor);
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        implementationProcessor = EasyMock.createMock(ImplementationProcessor.class);

        context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.replay(context);

        reader = EasyMock.createMock(XMLStreamReader.class);

        loader = new SystemImplementationLoader(implementationProcessor);
    }
}
