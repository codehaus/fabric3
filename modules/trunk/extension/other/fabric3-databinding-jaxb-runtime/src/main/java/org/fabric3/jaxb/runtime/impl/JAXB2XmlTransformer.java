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
package org.fabric3.jaxb.runtime.impl;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.transform.AbstractPullTransformer;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.TransformationException;

/**
 * Transforms a JAXB object to an XML string representation.
 *
 * @version $Revision$ $Date$
 */
public class JAXB2XmlTransformer extends AbstractPullTransformer<Object, String> {
    private static final JavaClass<String> TARGET = new JavaClass<String>(String.class);

    private final JAXBContext jaxbContext;

    public JAXB2XmlTransformer(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public String transform(Object source, TransformContext context) throws TransformationException {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.marshal(source, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new TransformationException(e);
        }

    }

    public DataType<?> getTargetType() {
        return TARGET;
    }
}