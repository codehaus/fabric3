  /*
   * Fabric3
   * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.transform.xml;

import java.io.ByteArrayOutputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.transform.AbstractPullTransformer;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;

/**
 * Serializes an element.
 *
 * @version $Rev$ $Date$
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
