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

import java.io.ByteArrayOutputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import org.fabric3.model.type.service.DataType;
import org.fabric3.transform.AbstractPullTransformer;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;

/**
 * Serializes an element.
 *
 * @version $Revision$ $Date$
 */
public class Node2String extends AbstractPullTransformer<Node, String> {

    public String transform(Node source, TransformContext context) throws TransformationException {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(source), new StreamResult(out));

            return new String(out.toByteArray());
        } catch (TransformerException e) {
            throw new TransformationException(e);
        }

    }

    public DataType<?> getTargetType() {
        // TODO Auto-generated method stub
        return null;
    }

}
