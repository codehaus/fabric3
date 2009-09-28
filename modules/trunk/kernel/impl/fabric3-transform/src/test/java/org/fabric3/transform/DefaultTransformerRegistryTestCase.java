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
package org.fabric3.transform;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.w3c.dom.Node;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.xsd.XSDSimpleType;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerFactory;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.transform.dom2java.Node2IntegerTransformer;

/**
 * @version $Rev$ $Date$
 */
public class DefaultTransformerRegistryTestCase extends TestCase {
    private DefaultTransformerRegistry registry;

    public void testTransformerRegistration() throws Exception {
        PullTransformer<?, ?> transformer = new Node2IntegerTransformer();
        List<PullTransformer<?, ?>> transformers = new ArrayList<PullTransformer<?, ?>>();
        transformers.add(transformer);
        registry.setTransformers(transformers);
        XSDSimpleType source = new XSDSimpleType(Node.class, XSDSimpleType.STRING);
        JavaClass<Integer> target = new JavaClass<Integer>(Integer.class);
        assertSame(transformer, registry.getTransformer(source, target));
    }

    public void testTransformerFactoryRegistration() throws Exception {
        List<TransformerFactory<?, ?>> factories = new ArrayList<TransformerFactory<?, ?>>();
        factories.add(new MockFactory());
        registry.setFactories(factories);
        XSDSimpleType source = new XSDSimpleType(Node.class, XSDSimpleType.STRING);
        JavaClass<Integer> target = new JavaClass<Integer>(Integer.class);
        assertNotNull(registry.getTransformer(source, target));
    }

    private class MockFactory implements TransformerFactory<Object, Object> {

        public boolean canTransform(DataType<?> source, DataType<?> target) {
            return true;
        }

        public Transformer<Object, Object> create(Class<?>... classes) throws TransformationException {
            return new MockTransformer();
        }
    }

    private class MockTransformer implements Transformer<Object, Object> {

        public Object transform(Object o, TransformContext context) throws TransformationException {
            return null;
        }

        public DataType<?> getSourceType() {
            return null;
        }

        public DataType<?> getTargetType() {
            return null;
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = new DefaultTransformerRegistry();
    }
}
