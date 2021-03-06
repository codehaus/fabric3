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
 */
package org.fabric3.introspection.xml.writer;

import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.spi.introspection.xml.TypeWriter;
import org.fabric3.spi.introspection.xml.UnrecognizedTypeException;
import org.fabric3.spi.introspection.xml.Writer;

/**
 * @version $Rev$ $Date$
 */
public class WriterImplTestCase extends TestCase {
    private Writer writer = new WriterImpl();

    @SuppressWarnings({"unchecked"})
    public void testRegisterDispatchDeregister() throws Exception {
        TypeWriter<ComponentDefinition> typeWriter = EasyMock.createMock(TypeWriter.class);
        typeWriter.write(EasyMock.isA(ComponentDefinition.class), EasyMock.isA(XMLStreamWriter.class));
        EasyMock.expectLastCall();
        EasyMock.replay(typeWriter);

        writer.register(ComponentDefinition.class, typeWriter);
        XMLStreamWriter xmlWriter = EasyMock.createMock(XMLStreamWriter.class);
        writer.write(new ComponentDefinition(null), xmlWriter);
        writer.unregister(ComponentDefinition.class);
        try {
            writer.write(new ComponentDefinition(null), xmlWriter);
            fail();
        } catch (UnrecognizedTypeException e) {
            // expected
        }
        EasyMock.verify(typeWriter);
    }
}
