/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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
