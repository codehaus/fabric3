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
package org.fabric3.transform;

import junit.framework.TestCase;
import org.w3c.dom.Node;

import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.transform.dom2java.String2Integer;

/**
 * @version $Rev$ $Date$
 */
public class DefaultTransformerRegistryTestCase extends TestCase {
    private TransformerRegistry<PullTransformer<?,?>> registry;

    public void testRegistration() {
        PullTransformer<?,?> transformer = new String2Integer();
        registry.register(transformer);
        XSDSimpleType source = new XSDSimpleType(Node.class, XSDSimpleType.STRING);
        JavaClass<Integer> target = new JavaClass<Integer>(Integer.class);
        assertSame(transformer, registry.getTransformer(source, target));
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = new DefaultTransformerRegistry<PullTransformer<?,?>>();
    }
}
