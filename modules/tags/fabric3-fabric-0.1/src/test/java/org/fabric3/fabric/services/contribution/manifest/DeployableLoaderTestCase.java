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
package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.services.contribution.Deployable;

/**
 * @version $Rev$ $Date$
 */
public class DeployableLoaderTestCase extends TestCase {
    private static final QName QNAME = new QName("namespace", "composite");
    private DeployableLoader loader = new DeployableLoader(null);
    private XMLStreamReader reader;

    public void testRead() throws Exception {
        Deployable deployable = loader.load(reader, null);
        assertEquals(QNAME, deployable.getName());
    }


    protected void setUp() throws Exception {
        super.setUp();
        reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeValue(null, "composite")).andReturn("composite");
        EasyMock.expect(reader.getNamespaceURI()).andReturn("namespace");
        EasyMock.replay(reader);
    }
}
