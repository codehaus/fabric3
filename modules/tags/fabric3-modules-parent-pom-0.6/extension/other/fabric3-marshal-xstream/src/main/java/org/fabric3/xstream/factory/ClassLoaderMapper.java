/*
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
package org.fabric3.xstream.factory;

import java.net.URI;

import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.mapper.DefaultMapper;

import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;

/**
 * Encodes classnames and the classloader they are to be loaded in. This Mapper is used by the XStreamFactory so classes loaded in extension
 * classloaders can be deserialized properly, e.g. a a set of commands provisioned to a runtime node.
 *
 * @version $Revision$ $Date$
 */
public class ClassLoaderMapper extends DefaultMapper {
    private ClassLoaderRegistry registry;
    private ClassLoader defaultClassLoader;

    public ClassLoaderMapper(ClassLoaderRegistry registry, ClassLoader defaultClassLoader) {
        super(null);
        this.registry = registry;
        this.defaultClassLoader = defaultClassLoader;
    }

    public String serializedClass(Class type) {
        ClassLoader cl = type.getClassLoader();
        if (cl instanceof MultiParentClassLoader) {
            return type.getName() + "_f3_" + encode(((MultiParentClassLoader) cl).getName().toString());
        }
        return super.serializedClass(type);
    }

    public Class realClass(String elementName) {
        String[] elements = elementName.split("_f3_");
        ClassLoader cl;
        if (elements.length < 1) {
            // programming error
            throw new AssertionError("Illegal classname");
        }
        if (elements.length != 2) {
            cl = defaultClassLoader;
        } else {
            String classLoaderId = decode(elements[1]);
            cl = registry.getClassLoader(URI.create(classLoaderId));
            if (cl == null) {
                // programming error
                throw new AssertionError("Classloader not found for deserializaion: " + classLoaderId);
            }
        }
        try {
            return cl.loadClass(elements[0]);
        } catch (ClassNotFoundException e) {
            throw new CannotResolveClassException(elements[0] + " : " + e);
        }
    }

    /**
     * Encodes a classname and classloader id combination, escaping illegal XML characters.
     *
     * @param name the string to encode
     * @return the encoded string
     */
    private String encode(String name) {
        return name.replace("/", "_f3slash").replace(":", "_f3colon");
    }

    /**
     * Decodes a classname and classloader id combination.
     *
     * @param name the string to decode
     * @return the decoded string
     */
    private String decode(String name) {
        return name.replace("_f3slash", "/").replace("_f3colon", ":");
    }


}
