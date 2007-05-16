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
package org.fabric3.transform;

import junit.framework.TestCase;
import org.w3c.dom.Node;

import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.transform.dom2java.Node2Int;

/**
 * @version $Rev$ $Date$
 */
public class DefaultTransformerRegistryTestCase extends TestCase {
    private TransformerRegistry<PullTransformer<?,?>> registry;

    public void testRegistration() {
        PullTransformer<?,?> transformer = new Node2Int();
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
