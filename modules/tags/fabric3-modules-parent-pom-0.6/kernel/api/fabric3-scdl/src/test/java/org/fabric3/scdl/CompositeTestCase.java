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
package org.fabric3.scdl;

import java.util.Collection;
import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class CompositeTestCase extends TestCase {
    private QName name;
    private TestServiceContract autowireContract;

    public void testAutowireTargets() {
        InjectingComponentType ct1 = new InjectingComponentType();
        ct1.add(new ServiceDefinition("service1", autowireContract));
        TestImplementation impl1 = new TestImplementation();
        impl1.setComponentType(ct1);
        ComponentDefinition<TestImplementation> component1 = new ComponentDefinition<TestImplementation>("component1", impl1);

        Composite composite = new Composite(name);
        composite.add(component1);

        Collection<URI> targets = composite.getTargets(autowireContract);
        assertEquals(1, targets.size());
        assertTrue(targets.contains(URI.create("component1#service1")));
    }

    protected void setUp() throws Exception {
        super.setUp();
        name = new QName("name");
        autowireContract = new TestServiceContract(AutowireContract.class);
    }

    private static interface AutowireContract {
    }
}
