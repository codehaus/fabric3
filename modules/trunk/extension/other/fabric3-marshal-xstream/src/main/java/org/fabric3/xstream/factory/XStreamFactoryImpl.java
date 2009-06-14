  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.mapper.ArrayMapper;
import com.thoughtworks.xstream.mapper.AttributeAliasingMapper;
import com.thoughtworks.xstream.mapper.AttributeMapper;
import com.thoughtworks.xstream.mapper.CachingMapper;
import com.thoughtworks.xstream.mapper.ClassAliasingMapper;
import com.thoughtworks.xstream.mapper.DefaultImplementationsMapper;
import com.thoughtworks.xstream.mapper.DynamicProxyMapper;
import com.thoughtworks.xstream.mapper.EnumMapper;
import com.thoughtworks.xstream.mapper.FieldAliasingMapper;
import com.thoughtworks.xstream.mapper.ImmutableTypesMapper;
import com.thoughtworks.xstream.mapper.ImplicitCollectionMapper;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.OuterClassMapper;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.classloader.ClassLoaderRegistry;

/**
 * Default implemenation of XStreamFactory. The factory may be configured with custom converters and drivers.
 *
 * @version $Rev$ $Date$
 */
public class XStreamFactoryImpl implements XStreamFactory {
    private ClassLoaderRegistry registry;
    private ReflectionProvider reflectionProvider;

    public XStreamFactoryImpl(@Reference ClassLoaderRegistry registry) {
        this.registry = registry;
        JVM jvm = new JVM();
        reflectionProvider = jvm.bestReflectionProvider();
    }

    public XStream createInstance() {
        ClassLoader cl = XStreamFactoryImpl.class.getClassLoader();
        ClassLoaderStaxDriver driver = new ClassLoaderStaxDriver(cl);
        Mapper mapper = buildMapper(cl);
        return new XStream(reflectionProvider, mapper, driver);
    }


    private Mapper buildMapper(ClassLoader cl) {
        // method exists to replace the default Mapper with the ClassLoaderMapper
        Mapper mapper = new ClassLoaderMapper(registry, cl);
        // note do not use  XStream11XmlFriendlyMapper
        mapper = new ClassAliasingMapper(mapper);
        mapper = new FieldAliasingMapper(mapper);
        mapper = new AttributeAliasingMapper(mapper);
        mapper = new AttributeMapper(mapper);
        mapper = new ImplicitCollectionMapper(mapper);
        mapper = new DynamicProxyMapper(mapper);
        if (JVM.is15()) {
            mapper = new EnumMapper(mapper);
        }
        mapper = new OuterClassMapper(mapper);
        mapper = new ArrayMapper(mapper);
        mapper = new DefaultImplementationsMapper(mapper);
        mapper = new ImmutableTypesMapper(mapper);
        mapper = new CachingMapper(mapper);
        return mapper;
    }


}