/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ñLicenseî), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an ñas isî basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.AbstractPullTransformer;
import org.fabric3.spi.transform.TransformContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class String2Jaxb extends AbstractPullTransformer<Node, Object> {
	
    private static final JavaClass<Object> TARGET = new JavaClass<Object>(Object.class);
    private Set<Class<?>> jaxbClasses = new HashSet<Class<?>>();

	@Override
	public boolean canTransform(DataType<?> targetType) {
		Class<?> clazz = (Class<?>) targetType.getPhysical();
		if (clazz.isAnnotationPresent(XmlRootElement.class)) {
			jaxbClasses.add(clazz);
			return true;
		} else {
			return false;
		}
	}

	public DataType<?> getTargetType() {
		return TARGET;
	}

	public Object transform(Node source, TransformContext context) throws TransformationException {
		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		
		try {
			Class<?>[] classes = new Class<?>[jaxbClasses.size()];
			classes = jaxbClasses.toArray(classes);
			JAXBContext jaxbContext = JAXBContext.newInstance(classes);
			
			Thread.currentThread().setContextClassLoader(context.getTargetClassLoader());
			NodeList children = source.getChildNodes();
			for (int i = 0;i < children.getLength();i++) {
				if (children.item(i) instanceof Element) {
					return jaxbContext.createUnmarshaller().unmarshal(children.item(i));
				}
			}
			throw new TransformationException("Unexpected content");
			
		} catch (JAXBException e) {
			throw new TransformationException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(cl);
		}
		
	}

}
