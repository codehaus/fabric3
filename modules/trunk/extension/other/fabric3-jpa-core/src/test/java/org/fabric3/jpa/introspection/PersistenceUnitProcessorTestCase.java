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
package org.fabric3.jpa.introspection;

import javax.persistence.PersistenceUnit;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.helper.IntrospectionHelper;
import org.fabric3.jpa.PersistenceUnitResource;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitProcessorTestCase extends TestCase {

    private PersistenceUnitProcessor processor;
    private PersistenceUnit annotation;

    public void testCreateDefinition() {

        PersistenceUnitResource definition = processor.createDefinition(annotation);
        assertEquals("name", definition.getName());
        assertEquals("unitName", definition.getUnitName());
    }

    protected void setUp() throws Exception {
        super.setUp();

        annotation = EasyMock.createMock(PersistenceUnit.class);
        EasyMock.expect(annotation.name()).andReturn("name");
        EasyMock.expect(annotation.unitName()).andReturn("unitName");
        EasyMock.replay(annotation);

        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        ContractProcessor contractProcessor = new DefaultContractProcessor(helper);
        processor = new PersistenceUnitProcessor(contractProcessor);
    }
}
