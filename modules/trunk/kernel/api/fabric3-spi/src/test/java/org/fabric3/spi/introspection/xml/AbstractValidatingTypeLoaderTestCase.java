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
*/
package org.fabric3.spi.introspection.xml;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Rev$ $Date$
 */
public class AbstractValidatingTypeLoaderTestCase extends TestCase {
    private AbstractValidatingTypeLoader<Object> loader;
    private XMLInputFactory factory;
    private DefaultIntrospectionContext context;

    public void testInvalidAttribute() throws Exception {
        String xml = "<composite badAttribute='error'></composite>";
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xml.getBytes()));
        reader.nextTag();

        loader.validateAttributes(reader, context);

        assertFalse(context.getErrors().isEmpty());
        assertTrue(context.getErrors().get(0) instanceof UnrecognizedAttribute);
    }

    public void testValidAttribute() throws Exception {
        String xml = "<composite name='error'></composite>";
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xml.getBytes()));
        reader.nextTag();

        loader.validateAttributes(reader, context);

        assertTrue(context.getErrors().isEmpty());
    }

    protected void setUp() throws Exception {
        super.setUp();

        loader = new AbstractValidatingTypeLoader<Object>() {

            public Object load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
                return null;
            }
        };

        loader.attributes.add("name");
        factory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext();
    }
}
