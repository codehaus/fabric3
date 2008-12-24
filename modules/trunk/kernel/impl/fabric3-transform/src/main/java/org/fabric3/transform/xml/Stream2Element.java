/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.transform.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.transform.AbstractPushTransformer;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;

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
