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
package org.fabric3.binding.test;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.Constants;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.MissingAttributeException;

/**
 * Parses <code>binding.test</code> for services and references. A uri to bind the service to or target a reference must be provided as an attribute.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class TestBindingLoader implements TypeLoader<TestBindingDefinition> {

    public static final QName BINDING_QNAME = new QName(Constants.FABRIC3_NS, "binding.test");

    public TestBindingDefinition load(XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException, LoaderException {

        TestBindingDefinition definition;
        try {
            String uri = reader.getAttributeValue(null, "uri");
            if (uri == null) {
                throw new MissingAttributeException("The uri attribute is not specified", reader);
            }
            definition = new TestBindingDefinition(new URI(uri));
        } catch (URISyntaxException ex) {
            throw new LoaderException(reader, ex);
        }
        LoaderUtil.skipToEndElement(reader);
        return definition;

    }

}
