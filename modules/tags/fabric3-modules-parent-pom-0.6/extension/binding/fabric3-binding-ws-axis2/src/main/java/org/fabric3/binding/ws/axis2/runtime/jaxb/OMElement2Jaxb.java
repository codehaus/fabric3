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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.AbstractPullTransformer;

/**
 * @version $Revision$ $Date$
 */
public class OMElement2Jaxb extends AbstractPullTransformer<OMElement, Object> {

    private static final JavaClass<Object> TARGET = new JavaClass<Object>(Object.class);

    private final JAXBContext jaxbContext;

    public OMElement2Jaxb(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public Object transform(OMElement source, TransformContext context) {
        // Assume doc-lit wrapped and the service contract accepts only one argument
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            XMLStreamReader reader = source.getXMLStreamReader();
            return unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new AssertionError(e);
        }

    }

    public DataType<?> getTargetType() {
        return TARGET;
    }

}
