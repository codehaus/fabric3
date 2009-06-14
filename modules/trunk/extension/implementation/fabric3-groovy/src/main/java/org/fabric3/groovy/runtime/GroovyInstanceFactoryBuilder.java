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
import org.fabric3.model.type.java.ConstructorInjectionSite;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectionSite;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilder;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderException;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.reflection.ReflectiveInstanceFactoryProvider;

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
