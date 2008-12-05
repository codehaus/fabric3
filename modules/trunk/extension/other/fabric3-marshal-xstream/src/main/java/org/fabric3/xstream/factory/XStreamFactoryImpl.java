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