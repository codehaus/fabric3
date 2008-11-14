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
package org.fabric3.jpa.introspection;

import javax.persistence.PersistenceUnit;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.jpa.scdl.PersistenceUnitResource;

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
