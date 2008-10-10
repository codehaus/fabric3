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
 * --- Original Apache License ---
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.runtime.standalone.host.implementation.launched;

import java.net.URI;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * @version $Rev$ $Date$
 */
public class RunCommandGeneratorTestCase extends TestCase {
    private static final URI PARENT = URI.create("parent");
    private static final URI LAUNCHED1 = URI.create("parent/launched");
    private static final URI CHILD = URI.create("parent/child");
    private static final URI LAUNCHED2 = URI.create("parent/child/launched");
    private RunCommandGenerator generator;
    private LogicalCompositeComponent composite;

    public void testHierarchyGeneration() throws Exception {
    }

    protected void setUp() throws Exception {
        super.setUp();
        generator = new RunCommandGenerator(null, 1);

        ComponentDefinition<CompositeImplementation> parentDefinition = createComposite("parent");
        composite = new LogicalCompositeComponent(PARENT, parentDefinition, null);
        LogicalCompositeComponent childComposite = new LogicalCompositeComponent(CHILD, parentDefinition, composite);

        ComponentDefinition<Launched> launched2Def = createLaunched("launched2");
        LogicalComponent<Launched> launched2 =
                new LogicalComponent<Launched>(LAUNCHED2, launched2Def, childComposite);
        childComposite.addComponent(launched2);


        ComponentDefinition<Launched> launched1Def = createLaunched("launched1");
        LogicalComponent<Launched> launched1 =
                new LogicalComponent<Launched>(LAUNCHED1, launched1Def, childComposite);
        composite.addComponent(launched1);
        composite.addComponent(childComposite);
    }

    private ComponentDefinition<CompositeImplementation> createComposite(String name) {
        Composite type = new Composite(new QName(name));
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        return new ComponentDefinition<CompositeImplementation>(name, impl);
    }

    private ComponentDefinition<Launched> createLaunched(String name) {
        Launched impl = new Launched(null, null);
        return new ComponentDefinition<Launched>(name, impl);
    }

}
