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
package org.fabric3.jaxb.transform;

import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.transform.AbstractPullTransformer;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;

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
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            JAXBContext jaxbContext = JAXBContext.newInstance(classes);

            Thread.currentThread().setContextClassLoader(context.getTargetClassLoader());
            NodeList children = source.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
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
