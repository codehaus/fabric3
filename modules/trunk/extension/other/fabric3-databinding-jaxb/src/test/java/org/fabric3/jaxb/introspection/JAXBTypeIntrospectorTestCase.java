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
*/
package org.fabric3.jaxb.introspection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

import junit.framework.TestCase;

import org.fabric3.model.type.DataType;
import org.fabric3.model.type.contract.Operation;

/**
 * @version $Rev$ $Date$
 */
public class JAXBTypeIntrospectorTestCase extends TestCase {
    private JAXBTypeIntrospector introspector;

    public void testJAXBIntrospection() throws Exception {
        Operation jaxbOperation = createOperation("jaxbMethod", Param.class);
        introspector.introspect(jaxbOperation, Contract.class.getMethod("jaxbMethod", Param.class), null);
        assertEquals("jaxb", jaxbOperation.getDatabinding());
    }

    public void testNoJAXBIntrospection() throws Exception {
        Operation nonJaxbOperation = createOperation("nonJaxbMethod", String.class);
        introspector.introspect(nonJaxbOperation, Contract.class.getMethod("nonJaxbMethod", String.class), null);
        assertNull(nonJaxbOperation.getDatabinding());
    }

    protected void setUp() throws Exception {
        super.setUp();
        introspector = new JAXBTypeIntrospector();
    }

    private Operation createOperation(String name, Class<?> paramType) {
        DataType<Type> type = new DataType<Type>(paramType, paramType);
        List<DataType<?>> in = new ArrayList<DataType<?>>();
        in.add(type);
        return new Operation(name, in, null, null);
    }

    private class Contract {
        public void jaxbMethod(Param param) {

        }

        public void nonJaxbMethod(String param) {

        }
    }

    @XmlRootElement
    private class Param {

    }

}
