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
package org.fabric3.xstream.factory;

import java.net.URI;

import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.mapper.DefaultMapper;

import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.classloader.ClassLoaderRegistry;

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
