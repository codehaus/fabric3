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
package org.fabric3.transform.dom2java;

import java.net.MalformedURLException;
import java.net.URL;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.transform.AbstractPullTransformer;

import org.w3c.dom.Node;

/**
 * String to URL Transformer
 */
public class String2URL extends AbstractPullTransformer<Node, URL> {
	
	private static final JavaClass<URL> TARGET = new JavaClass<URL>(URL.class);

	/**
	 * @see org.fabric3.spi.transform.Transformer#getTargetType()
	 */
	public DataType<?> getTargetType() {
		return TARGET;
	}

	/**
	 * Transformation for URL
	 * 
	 * @see org.fabric3.spi.transform.PullTransformer#transform(java.lang.Object, org.fabric3.spi.transform.TransformContext)
	 */
	public URL transform(final Node node, final TransformContext context) throws TransformationException {
		final String content = node.getTextContent();
		final URL url;
		try {
			url = new URL(node.getTextContent());
		} catch (MalformedURLException me) {
			throw new TransformationException("Unable to create URL :- " + content, me);
		}
		return url;
	}
}
