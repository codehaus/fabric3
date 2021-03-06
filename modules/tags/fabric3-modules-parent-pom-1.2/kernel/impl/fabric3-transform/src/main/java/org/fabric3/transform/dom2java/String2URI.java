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
package org.fabric3.transform.dom2java;

import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.AbstractPullTransformer;

import org.w3c.dom.Node;

/**
 * String to URI Transformer
 */
public class String2URI extends AbstractPullTransformer<Node, URI> {
    private static final JavaClass<URI> TARGET = new JavaClass<URI>(URI.class);

    /**
     * @see org.fabric3.spi.transform.Transformer#getTargetType()
     */
    public DataType<?> getTargetType() {
        return TARGET;
    }

    /**
     * Transformation for URI
     * @see org.fabric3.spi.transform.PullTransformer#transform(java.lang.Object, org.fabric3.spi.transform.TransformContext)
     */
    public URI transform(final Node node, final TransformContext context) throws TransformationException {
    	final String content = node.getTextContent();
    	final URI uri;
        try {
			uri = new URI(node.getTextContent());
		} catch (URISyntaxException ue) {
			throw new TransformationException("Unable to create URI :- " + content, ue);
		}
		return uri;
    }
}