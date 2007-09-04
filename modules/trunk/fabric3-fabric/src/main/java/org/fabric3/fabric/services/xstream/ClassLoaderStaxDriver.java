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
package org.fabric3.fabric.services.xstream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Overrides the default behavior of StaxDriver to use a specific classloader when searching for the default STaX
 * implementation.
 *
 * @version $Rev$ $Date$
 */
public class ClassLoaderStaxDriver extends StaxDriver {
    private ClassLoader classLoader;
    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;


    public ClassLoaderStaxDriver(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public XMLInputFactory getInputFactory() {
        if (inputFactory == null) {
            inputFactory = XMLInputFactory.newInstance("javax.xml.stream.XMLInputFactory", classLoader);
        }
        return inputFactory;
    }

    public XMLOutputFactory getOutputFactory() {
        if (outputFactory == null) {
            outputFactory = XMLOutputFactory.newInstance("javax.xml.stream.XMLOutputFactory", classLoader);
        }
        return outputFactory;
    }
}
