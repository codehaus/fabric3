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
package org.fabric3.loader.composite;

import java.net.URI;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.scdl.WireDefinition;

/**
 * @version $Rev$ $Date$
 */
public class WireLoader implements TypeLoader<WireDefinition> {
    private final LoaderHelper helper;

    public WireLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public WireDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        String source = reader.getAttributeValue(null, "source");
        String target = reader.getAttributeValue(null, "target");
        LoaderUtil.skipToEndElement(reader);

        URI sourceURI = helper.getURI(source);
        URI targetURI = helper.getURI(target);
        return new WireDefinition(sourceURI, targetURI);
    }
}
