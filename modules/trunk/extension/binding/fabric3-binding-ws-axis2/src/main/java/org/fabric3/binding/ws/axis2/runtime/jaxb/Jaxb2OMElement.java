  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
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

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.AbstractPullTransformer;

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
