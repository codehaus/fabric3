/*
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

package org.fabric3.fabric.implementation.composite;

import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.spi.implementation.java.PojoComponentType;
import org.fabric3.spi.implementation.java.JavaMappedService;
import org.fabric3.spi.implementation.java.JavaMappedReference;
import org.fabric3.spi.loader.InvalidWireException;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ServiceDefinition;
import org.fabric3.spi.model.type.WireDefinition;

import junit.framework.TestCase;
import org.fabric3.fabric.implementation.java.JavaImplementation;

/**
 * This class tests the wire resolution function of the composite loader
 *
 * @version $Rev$ $Date$
 */
public class CompositeLoaderWireResolutionTestCase extends TestCase {
    private CompositeComponentType componentType;
    private CompositeLoader compositeLoader = new CompositeLoader(null, null);

    public void testCompositeSvc2CompositeReferenceWire() throws Exception {
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("compositeService1"));
        wireDefn.setTarget(new URI("compositeReference"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testCompositeSvc2ComponentValid() throws Exception {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("compositeService1"));
        wireDefn.setTarget(new URI("Component1"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testCompositeSvc2ComponentQualifiedValid() throws Exception {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("compositeService1"));
        wireDefn.setTarget(new URI("Component2#pojoSvc3"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testCompositeSvc2ComponentQualifiedInvalid() throws URISyntaxException {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("compositeService1"));
        wireDefn.setTarget(new URI("Component2#pojoSvc5"));
        componentType.add(wireDefn);
        try {
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }
    }

    public void testCompositeSvc2ComponentUnQualifiedInvalid() throws URISyntaxException {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("compositeService1"));
        wireDefn.setTarget(new URI("Component2"));
        componentType.add(wireDefn);
        try {
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }
    }

    public void testComponent2CompositeReferenceValid() throws Exception {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("Component1#pojoRef1"));
        wireDefn.setTarget(new URI("compositeReference"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testComponent2CompositeReferenceQualifiedValid() throws Exception {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("Component2#pojoRef3"));
        wireDefn.setTarget(new URI("compositeReference"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testComponent2CompositeReferenceUnQualifiedInvalid() throws URISyntaxException {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("Component2"));
        wireDefn.setTarget(new URI("compositeReference"));
        componentType.add(wireDefn);

        try {
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }
    }

    public void testComponent2ComponentQualifedValid() throws Exception {
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("Component1#pojoRef1"));
        wireDefn.setTarget(new URI("Component2#pojoSvc3"));
        componentType.add(wireDefn);
        compositeLoader.resolveWires(componentType);
    }

    public void testComponent2ComponentUnQualifedInvalid() throws URISyntaxException {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("Component1"));
        wireDefn.setTarget(new URI("Component2"));
        componentType.add(wireDefn);
        try {
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }
    }

    public void testInvalidWireDefinitions() throws URISyntaxException {
        //undefined source and targets
        WireDefinition wireDefn = new WireDefinition();
        wireDefn.setSource(new URI("undefinedSource"));
        wireDefn.setTarget(new URI("compositeReference"));
        componentType.add(wireDefn);

        try {
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }

        try {
            wireDefn.setSource(new URI("compositeService1"));
            wireDefn.setTarget(new URI("undefinedTarget"));
            componentType.add(wireDefn);
            compositeLoader.resolveWires(componentType);
            fail();
        } catch (InvalidWireException e) {
            // expected
        }
    }

    public void setUp() throws Exception {
        componentType = new CompositeComponentType();

        //add a service to the composite
        ServiceDefinition serviceDefn = new ServiceDefinition(URI.create("#compositeService1"), null, true);
        ServiceDefinition boundSvcDefn = new ServiceDefinition(URI.create("#boundSvc"), null, true, null);
        ServiceDefinition boundSvcDefnWithTarget =
            new ServiceDefinition(URI.create("#boundSvcWithTarget"), null, true);
        boundSvcDefnWithTarget.setTarget(new URI("orgTarget"));
        componentType.add(serviceDefn);
        componentType.add(boundSvcDefn);
        componentType.add(boundSvcDefnWithTarget);

        ReferenceDefinition compositeReference = new ReferenceDefinition(URI.create("#compositeReference"), null);
        componentType.add(compositeReference);

        PojoComponentType pojoComponentType1 =
            new PojoComponentType();
        JavaMappedService pojoSvc1 = new JavaMappedService(URI.create("#pojoSvc1"), null, false);
        pojoComponentType1.add(pojoSvc1);
        JavaMappedReference pojoRef1 = new JavaMappedReference(URI.create("#pojoRef1"), null, null);
        pojoComponentType1.add(pojoRef1);
        JavaImplementation pojoImpl1 = new JavaImplementation(null, pojoComponentType1);

        ComponentDefinition<JavaImplementation> component1 =
            new ComponentDefinition<JavaImplementation>("Component1", pojoImpl1);
        componentType.add(component1);

        PojoComponentType pojoComponentType2 =
            new PojoComponentType();
        JavaMappedService pojoSvc2 = new JavaMappedService(URI.create("#pojoSvc2"), null, false);
        pojoComponentType2.add(pojoSvc2);
        JavaMappedService pojoSvc3 = new JavaMappedService(URI.create("#pojoSvc3"), null, false);
        pojoComponentType2.add(pojoSvc3);
        JavaMappedReference pojoRef2 = new JavaMappedReference(URI.create("#pojoRef2"), null, null);
        pojoComponentType2.add(pojoRef2);
        JavaMappedReference pojoRef3 = new JavaMappedReference(URI.create("#pojoRef3"), null, null);
        pojoComponentType2.add(pojoRef3);
        JavaImplementation pojoImpl2 = new JavaImplementation(null, pojoComponentType2);

        ComponentDefinition<JavaImplementation> component2 =
            new ComponentDefinition<JavaImplementation>("Component2", pojoImpl2);
        componentType.add(component2);
    }




}
