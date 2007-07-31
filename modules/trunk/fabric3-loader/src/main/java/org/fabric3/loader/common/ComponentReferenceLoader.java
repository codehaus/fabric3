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
package org.fabric3.loader.common;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.scdl.ComponentReference;
import org.fabric3.spi.loader.InvalidReferenceException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * Loads a reference from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ComponentReferenceLoader implements StAXElementLoader<ComponentReference> {

    public ComponentReference load(XMLStreamReader reader, LoaderContext context)
        throws XMLStreamException, LoaderException {

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            throw new InvalidReferenceException("No name specified");
        }
        boolean autowire = Boolean.parseBoolean(reader.getAttributeValue(null, "autowire"));
        String target = reader.getAttributeValue(null, "target");

        List<URI> uris = new ArrayList<URI>();
        if (target != null) {
            StringTokenizer tokenizer = new StringTokenizer(target);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                uris.add(LoaderUtil.getURI(token));
            }
        }

        ComponentReference reference = new ComponentReference(name);
        reference.setAutowire(autowire);
        reference.getTargets().addAll(uris);
        
        LoaderUtil.skipToEndElement(reader);
        
        return reference;
        
    }
}
