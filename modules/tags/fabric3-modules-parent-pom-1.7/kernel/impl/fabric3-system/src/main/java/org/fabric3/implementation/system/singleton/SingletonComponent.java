/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.system.singleton;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitializationException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.objectfactory.SingletonObjectFactory;

/**
 * Wraps an object intended to serve as a system component provided to the Fabric3 runtime by the host environment.
 *
 * @version $$Rev$$ $$Date$$
 */
public class SingletonComponent implements AtomicComponent {
    private final URI uri;
    private Object instance;
    private Map<Member, Injectable> sites;
    private InstanceWrapper wrapper;
    private Map<ObjectFactory, Injectable> reinjectionMappings;
    private URI classLoaderId;
    private MonitorLevel level = MonitorLevel.INFO;

    public SingletonComponent(URI componentId, Object instance, Map<InjectionSite, Injectable> mappings) {
        this.uri = componentId;
        this.instance = instance;
        this.wrapper = new SingletonWrapper(instance);
        this.reinjectionMappings = new HashMap<ObjectFactory, Injectable>();
        initializeInjectionSites(instance, mappings);
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

    public String getKey() {
        return null;
    }

    public URI getUri() {
        return uri;
    }

    public void start() {
    }

    public void stop() {
    }

    public QName getDeployable() {
        return null;
    }

    public boolean isEagerInit() {
        return true;
    }

    public long getMaxIdleTime() {
        return -1;
    }

    public long getMaxAge() {
        return -1;
    }

    public InstanceWrapper createInstanceWrapper(WorkContext workContext) throws ObjectCreationException {
        return wrapper;
    }

    public ObjectFactory<Object> createObjectFactory() {
        return new SingletonObjectFactory<Object>(instance);
    }

    public String getName() {
        return uri.toString();
    }

    public MonitorLevel getLevel() {
        return level;
    }

    public void setLevel(MonitorLevel level) {
        this.level = level;
    }

    /**
     * Adds an ObjectFactory to be reinjected
     *
     * @param attribute    the InjectableAttribute describing the site to reinject
     * @param paramFactory the object factory responsible for supplying a value to reinject
     */
    public void addObjectFactory(Injectable attribute, ObjectFactory paramFactory) {
        reinjectionMappings.put(paramFactory, attribute);
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }

    /**
     * Obtain the fields and methods for injection sites associated with the instance
     *
     * @param instance the instance this component wraps
     * @param mappings the mappings of injection sites
     */
    private void initializeInjectionSites(Object instance, Map<InjectionSite, Injectable> mappings) {
        this.sites = new HashMap<Member, Injectable>();
        for (Map.Entry<InjectionSite, Injectable> entry : mappings.entrySet()) {
            InjectionSite site = entry.getKey();
            if (site instanceof FieldInjectionSite) {
                try {
                    Field field = getField(((FieldInjectionSite) site).getName());
                    sites.put(field, entry.getValue());
                } catch (NoSuchFieldException e) {
                    // programming error
                    throw new AssertionError(e);
                }
            } else if (site instanceof MethodInjectionSite) {
                MethodInjectionSite methodInjectionSite = (MethodInjectionSite) site;
                try {
                    Method method = methodInjectionSite.getSignature().getMethod(instance.getClass());
                    sites.put(method, entry.getValue());
                } catch (ClassNotFoundException e) {
                    // programming error
                    throw new AssertionError(e);
                } catch (NoSuchMethodException e) {
                    // programming error
                    throw new AssertionError(e);
                }

            } else {
                // ignore other injection sites
            }
        }
    }

    private Field getField(String name) throws NoSuchFieldException {
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private class SingletonWrapper implements InstanceWrapper {

        private final Object instance;

        private SingletonWrapper(Object instance) {
            this.instance = instance;
        }

        public Object getInstance() {
            return instance;
        }

        public boolean isStarted() {
            return true;
        }

        public void start(WorkContext workContext) throws InstanceInitializationException {
        }

        public void stop(WorkContext workContext) throws InstanceDestructionException {
        }

        public void reinject() {
            for (Map.Entry<ObjectFactory, Injectable> entry : reinjectionMappings.entrySet()) {
                try {
                    inject(entry.getValue(), entry.getKey());
                } catch (ObjectCreationException e) {
                    throw new AssertionError(e);
                }
            }
            reinjectionMappings.clear();
        }

        /**
         * Injects a new value on a field or method of the instance.
         *
         * @param attribute the InjectableAttribute defining the field or method
         * @param factory   the ObjectFactory that returns the value to inject
         * @throws ObjectCreationException if an error occurs during injection
         */
        private void inject(Injectable attribute, ObjectFactory factory) throws ObjectCreationException {
            for (Map.Entry<Member, Injectable> entry : sites.entrySet()) {
                if (entry.getValue().equals(attribute)) {
                    Member member = entry.getKey();
                    if (member instanceof Field) {
                        try {
                            Object param = factory.getInstance();
                            ((Field) member).set(instance, param);
                        } catch (IllegalAccessException e) {
                            // should not happen as accessibility is already set
                            throw new ObjectCreationException(e);
                        }
                    } else if (member instanceof Method) {
                        try {
                            Object param = factory.getInstance();
                            Method method = (Method) member;
                            Class<?> type = method.getParameterTypes()[0];
                            if (Set.class.equals(type)) {
                                param = Collections.singleton(param);
                            } else if (List.class.equals(type)) {
                                param = Collections.singletonList(param);
                            }
                            method.invoke(instance, param);
                        } catch (IllegalAccessException e) {
                            // should not happen as accessibility is already set
                            throw new ObjectCreationException(e);
                        } catch (InvocationTargetException e) {
                            throw new ObjectCreationException(e);
                        }
                    } else {
                        // programming error
                        throw new ObjectCreationException("Unsupported member type" + member);
                    }
                }
            }
        }

        public void updated(String referenceName) {
            // no-op
        }

        public void removed(String referenceName) {
            // no-op
        }

    }
}
