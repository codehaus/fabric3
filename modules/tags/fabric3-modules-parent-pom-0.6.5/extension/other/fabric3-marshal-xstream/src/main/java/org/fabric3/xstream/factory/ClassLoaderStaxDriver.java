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
package org.fabric3.xstream.factory;

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
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                inputFactory = XMLInputFactory.newInstance();
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }

        }
        return inputFactory;
    }

    public XMLOutputFactory getOutputFactory() {
        // set the classloader to load STaX in on the TCCL as XMLOutputFactory.newInstance(String, ClassLoader)
        // mistakenly returns an XMLInputFactory in STaX 1.0 and Sun has decided not to fix it in JDK 6.
        if (outputFactory == null) {
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                outputFactory = XMLOutputFactory.newInstance();
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
        return outputFactory;
    }
}