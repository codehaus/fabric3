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
package org.fabric3.jaxb.runtime;

import javax.xml.bind.JAXBContext;

import junit.framework.TestCase;

import org.fabric3.jaxb.runtime.impl.Xml2JAXBTransformer;

/**
 * @version $Revision$ $Date$
 */
public class Xml2JAXBTransformerTestCase extends TestCase {
    private Xml2JAXBTransformer transformer;

    public void testMarshall() throws Exception {
        Object result = transformer.transform("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foo/>", null);
        assertTrue(result instanceof Foo);
    }

    protected void setUp() throws Exception {
        super.setUp();
        JAXBContext context = JAXBContext.newInstance(Foo.class);
        transformer = new Xml2JAXBTransformer(context);
    }
}