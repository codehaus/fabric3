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
package org.fabric3.groovy.runtime;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.groovy.provision.GroovyInstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilder;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderException;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.reflection.ReflectiveInstanceFactoryProvider;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectionSite;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class GroovyInstanceFactoryBuilder<T>
        implements InstanceFactoryBuilder<ReflectiveInstanceFactoryProvider<T>, GroovyInstanceFactoryDefinition> {

    private final InstanceFactoryBuilderRegistry registry;
    private final InstanceFactoryBuildHelper helper;

    public GroovyInstanceFactoryBuilder(@Reference InstanceFactoryBuilderRegistry registry,
                                        @Reference InstanceFactoryBuildHelper helper) {
        this.registry = registry;
        this.helper = helper;
    }

    @Init
    public void init() {
        registry.register(GroovyInstanceFactoryDefinition.class, this);
    }

    public ReflectiveInstanceFactoryProvider<T> build(GroovyInstanceFactoryDefinition ifpd, ClassLoader cl)
            throws InstanceFactoryBuilderException {

        GroovyClassLoader gcl = new GroovyClassLoader(cl);
        try {
            Class<T> implClass = getImplClass(ifpd, gcl);
            Constructor<T> ctr = helper.getConstructor(implClass, ifpd.getConstructor());

            Map<InjectionSite, InjectableAttribute> injectionSites = ifpd.getConstruction();
            InjectableAttribute[] cdiSources = new InjectableAttribute[ctr.getParameterTypes().length];
            for (Map.Entry<InjectionSite, InjectableAttribute> entry : injectionSites.entrySet()) {
                InjectionSite site = entry.getKey();
                InjectableAttribute attribute = entry.getValue();
                ConstructorInjectionSite constructorSite = (ConstructorInjectionSite) site;
                cdiSources[constructorSite.getParam()] = attribute;
            }
            for (int i = 0; i < cdiSources.length; i++) {
                if (cdiSources[i] == null) {
                    throw new InstanceFactoryBuilderException("No source for constructor parameter " + i, ctr.getName());
                }
            }

            Method initMethod = helper.getMethod(implClass, ifpd.getInitMethod());
            Method destroyMethod = helper.getMethod(implClass, ifpd.getDestroyMethod());
            boolean reinjectable = ifpd.isReinjectable();
            return new ReflectiveInstanceFactoryProvider<T>(ctr,
                                                            Arrays.asList(cdiSources),
                                                            ifpd.getPostConstruction(),
                                                            initMethod,
                                                            destroyMethod,
                                                            reinjectable,
                                                            cl);
        } catch (ClassNotFoundException e) {
            throw new InstanceFactoryBuilderException(e);
        } catch (NoSuchMethodException ex) {
            throw new InstanceFactoryBuilderException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> getImplClass(GroovyInstanceFactoryDefinition ifpd, GroovyClassLoader gcl)
            throws ClassNotFoundException, InstanceFactoryBuilderException {
        if (ifpd.getImplementationClass() != null) {
            try {
                return (Class<T>) helper.loadClass(gcl, ifpd.getImplementationClass());
            } catch (ClassNotFoundException e) {
                throw new InstanceFactoryBuilderException(e);
            }
        } else if (ifpd.getScriptName() != null) {
            try {
                URL script = gcl.getResource(ifpd.getScriptName());
                GroovyCodeSource source = new GroovyCodeSource(script);
                return gcl.parseClass(source);
            } catch (IOException e) {
                throw new InstanceFactoryBuilderException(e.getMessage(), ifpd.getScriptName(), e);
            }
        } else {
            throw new AssertionError();
        }
    }
}
