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
package org.fabric3.transform.dom2java;

import java.net.MalformedURLException;
import java.net.URL;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.TransformationException;
import org.fabric3.transform.AbstractPullTransformer;

import org.w3c.dom.Node;

/**
 * String to URL Transformer
 */
public class String2URL extends AbstractPullTransformer<Node, URL> {
	
	private static final JavaClass<URL> TARGET = new JavaClass<URL>(URL.class);

	/**
	 * @see org.fabric3.transform.Transformer#getTargetType()
	 */
	public DataType<?> getTargetType() {
		return TARGET;
	}

	/**
	 * Transformation for URL
	 * 
	 * @see org.fabric3.transform.PullTransformer#transform(java.lang.Object, org.fabric3.transform.TransformContext)
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
