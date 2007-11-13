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
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.scdl.WireDefinition;

/**
 * @version $Rev$ $Date$
 */
public class WireLoader implements StAXElementLoader<WireDefinition> {
    public WireDefinition load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {

        String source = reader.getAttributeValue(null, "source");
        String target = reader.getAttributeValue(null, "target");
        LoaderUtil.skipToEndElement(reader);

        URI sourceURI = LoaderUtil.getURI(source);
        URI targetURI = LoaderUtil.getURI(target);
        return new WireDefinition(sourceURI, targetURI);
    }
}
