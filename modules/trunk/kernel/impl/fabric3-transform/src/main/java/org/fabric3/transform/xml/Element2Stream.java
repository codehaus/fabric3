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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.transform.AbstractPullTransformer;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.xml.XMLFactory;

/**
 * @version $Rev$ $Date$
 */
public class Element2Stream extends AbstractPullTransformer<Element, XMLStreamReader> {
    private static final JavaClass<XMLStreamReader> TARGET = new JavaClass<XMLStreamReader>(XMLStreamReader.class);

    private final XMLInputFactory xmlFactory;

    public Element2Stream(@Reference XMLFactory xmlFactory) {
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    public DataType<?> getTargetType() {
        return TARGET;
    }

    public XMLStreamReader transform(Element element, TransformContext context) throws TransformationException {
        DOMSource source = new DOMSource(element);
        try {
            return xmlFactory.createXMLStreamReader(source);
        } catch (XMLStreamException e) {
            throw new TransformationException(e);
        }
    }
}
