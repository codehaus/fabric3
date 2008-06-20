package org.fabric3.xstream.factory;

import java.net.URI;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;

/**
 * @version $Revision$ $Date$
 */
public class ClassLoaderAwareConverter extends ReflectionConverter {
    private ClassLoaderRegistry registry;

    public ClassLoaderAwareConverter(Mapper mapper, ReflectionProvider reflectionProvider, ClassLoaderRegistry registry) {
        super(mapper, reflectionProvider);
        this.registry = registry;
    }

    public void marshal(Object original, HierarchicalStreamWriter writer, MarshallingContext context) {
        ClassLoader cl = original.getClass().getClassLoader();
        if (cl instanceof MultiParentClassLoader) {
            writer.addAttribute("f3-classloader", ((MultiParentClassLoader) cl).getName().toString());
        }
        super.marshal(original, writer, context);
    }

    protected Object instantiateNewInstance(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String readResolveValue = reader.getAttribute(mapper.aliasForAttribute("resolves-to"));
        Object currentObject = context.currentObject();
        if (currentObject != null) {
            return currentObject;
        }
        String attr = reader.getAttribute("f3-classloader");
        ClassLoader cl;
        if (attr != null) {
            URI id = URI.create(attr);
            cl = registry.getClassLoader(id);
            if (cl == null) {
                // programming error
                throw new AssertionError("Classloader not found: " + id);
            }
        } else {
            cl = getClass().getClassLoader();
        }
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            if (readResolveValue != null) {
                return reflectionProvider.newInstance(mapper.realClass(readResolveValue));
            } else {
                return reflectionProvider.newInstance(context.getRequiredType());
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public boolean canConvert(Class type) {
        return type.getClassLoader() instanceof MultiParentClassLoader;
    }
}
