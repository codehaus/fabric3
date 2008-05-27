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
package org.fabric3.transform.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;

import org.fabric3.scdl.DataType;
import org.fabric3.transform.AbstractPushTransformer;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.TransformationException;

/**
 * @version $Rev$ $Date$
 */
public class Stream2Element extends AbstractPushTransformer<XMLStreamReader, Element> {
    private final Stream2Stream streamTransformer;


    public Stream2Element(Stream2Stream streamTransformer) {
        this.streamTransformer = streamTransformer;
    }

    public DataType<?> getSourceType() {
        return null;
    }

    public DataType<?> getTargetType() {
        return null;
    }

    public void transform(XMLStreamReader reader, Element element, TransformContext context) throws TransformationException {
        XMLStreamWriter writer = new DOMStreamWriter(element.getOwnerDocument(), element);
        try {
            streamTransformer.transform(reader, writer, null);
        } finally {
            try {
                writer.close();
            } catch (XMLStreamException e) {
                // ignore
            }
        }
    }
}
