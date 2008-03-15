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
package org.fabric3.system.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.introspection.helper.IntrospectionHelper;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.helper.TypeMapping;
import org.fabric3.introspection.contract.InvalidServiceContractException;
import org.fabric3.introspection.java.UnsupportedTypeException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.system.scdl.SystemImplementation;

/**
 * Heuristic processor that locates unannotated Property and Reference dependencies.
 *
 * @version $Rev$ $Date$
 */
public class SystemUnannotatedHeuristic implements HeuristicProcessor<SystemImplementation> {

    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;

    public SystemUnannotatedHeuristic(@Reference IntrospectionHelper helper,
                                      @Reference ContractProcessor contractProcessor) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    public void applyHeuristics(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {
        PojoComponentType componentType = implementation.getComponentType();

        // if any properties, references or resources have been defined already assume that was what the user intended and return
        if (!(componentType.getProperties().isEmpty() && componentType.getReferences().isEmpty() && componentType.getResources().isEmpty())) {
            return;
        }

        evaluateConstructor(implementation, implClass, context);
        evaluateSetters(implementation, implClass, context);
        evaluateFields(implementation, implClass, context);
    }

    void evaluateConstructor(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {
        PojoComponentType componentType = implementation.getComponentType();
        Map<InjectionSite,InjectableAttribute> sites = componentType.getInjectionSites();
        Constructor<?> constructor;
        try {
            constructor = componentType.getConstructor().getConstructor(implClass);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }

        TypeMapping typeMapping = context.getTypeMapping();
        Type[] parameterTypes = constructor.getGenericParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            InjectionSite site = new ConstructorInjectionSite(constructor, i);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            Type parameterType = parameterTypes[i];
            String name = helper.getSiteName(constructor, i, null);
            processSite(componentType, typeMapping, name, parameterType, site);
        }
    }

    void evaluateSetters(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {
        PojoComponentType componentType = implementation.getComponentType();
        Map<InjectionSite,InjectableAttribute> sites = componentType.getInjectionSites();
        TypeMapping typeMapping = context.getTypeMapping();
        Set<Method> setters = helper.getInjectionMethods(implClass, componentType.getServices().values());
        for (Method setter : setters) {
            InjectionSite site = new MethodInjectionSite(setter, 0);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            String name = helper.getSiteName(setter, null);
            Type parameterType = setter.getGenericParameterTypes()[0];
            processSite(componentType, typeMapping, name, parameterType, site);
        }
    }

    void evaluateFields(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {
        PojoComponentType componentType = implementation.getComponentType();
        Map<InjectionSite,InjectableAttribute> sites = componentType.getInjectionSites();
        TypeMapping typeMapping = context.getTypeMapping();
        Set<Field> fields = helper.getInjectionFields(implClass);
        for (Field field : fields) {
            InjectionSite site = new FieldInjectionSite(field);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            String name = helper.getSiteName(field, null);
            Type parameterType = field.getGenericType();
            processSite(componentType, typeMapping, name, parameterType, site);
        }
    }


    void processSite(PojoComponentType componentType, TypeMapping typeMapping, String name, Type parameterType, InjectionSite site) throws IntrospectionException {
        switch (helper.inferType(parameterType, typeMapping)) {
        case PROPERTY:
            addProperty(componentType, typeMapping, name, parameterType, site);
            break;
        case REFERENCE:
            addReference(componentType, typeMapping, name, parameterType, site);
            break;
        default:
            throw new UnsupportedTypeException(site.toString());
        }
    }

    void addProperty(PojoComponentType componentType, TypeMapping typeMapping, String name, Type parameterType, InjectionSite site) {
        Property property = new Property(name, null);
        property.setMany(helper.isManyValued(typeMapping, parameterType));
        componentType.add(property, site);
    }

    void addReference(PojoComponentType componentType, TypeMapping typeMapping, String name, Type parameterType, InjectionSite site) throws
            InvalidServiceContractException {
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, parameterType);
        Multiplicity multiplicity = helper.isManyValued(typeMapping, parameterType) ? Multiplicity.ONE_N : Multiplicity.ONE_ONE;
        ReferenceDefinition reference = new ReferenceDefinition(name, contract, multiplicity);
        componentType.add(reference, site);
    }
}
