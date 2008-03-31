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
package org.fabric3.binding.ws.axis2.runtime.jaxb;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.output.ByteArrayOutputStream;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.AbstractPullTransformer;

/**
 * @version $Revision$ $Date$
 */
public class Jaxb2OMElement extends AbstractPullTransformer<Object, OMElement> {

    private static final JavaClass<OMElement> TARGET = new JavaClass<OMElement>(OMElement.class);

    private final JAXBContext jaxbContext;

    public Jaxb2OMElement(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public OMElement transform(Object source, TransformContext context) {

        try {
            Marshaller marshaller = jaxbContext.createMarshaller();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            marshaller.marshal(source, out);

            byte[] data = out.toByteArray();
            InputStream in = new ByteArrayInputStream(data);

            StAXOMBuilder builder = new StAXOMBuilder(in);
            return builder.getDocumentElement();

        } catch (JAXBException e) {
            throw new AssertionError(e);
        } catch (XMLStreamException e) {
            throw new AssertionError(e);
        }

    }

    public DataType<?> getTargetType() {
        return TARGET;
    }

}
