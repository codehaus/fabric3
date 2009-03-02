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
package org.fabric3.junit.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.junit.scdl.JUnitImplementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.java.ConstructorInjectionSite;
import org.fabric3.model.type.java.FieldInjectionSite;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectableAttributeType;
import org.fabric3.model.type.java.InjectionSite;
import org.fabric3.model.type.java.MethodInjectionSite;
import org.fabric3.model.type.java.Signature;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.AmbiguousConstructor;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.NoConstructorFound;
import org.fabric3.spi.introspection.java.PolicyAnnotationProcessor;
import org.fabric3.spi.introspection.java.UnknownInjectionType;

/**
 * @version $Rev$ $Date$
 */
public class JUnitHeuristic implements HeuristicProcessor<JUnitImplementation> {

    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;

    private final HeuristicProcessor<JUnitImplementation> serviceHeuristic;
    private PolicyAnnotationProcessor policyProcessor;

    public JUnitHeuristic(@Reference IntrospectionHelper helper,
                          @Reference ContractProcessor contractProcessor,
                          @Reference(name = "service") HeuristicProcessor<JUnitImplementation> serviceHeuristic) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
        this.serviceHeuristic = serviceHeuristic;
    }

    @Reference
    public void setPolicyProcessor(PolicyAnnotationProcessor processor) {
        this.policyProcessor = processor;
    }

    public void applyHeuristics(JUnitImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        PojoComponentType componentType = implementation.getComponentType();

        serviceHeuristic.applyHeuristics(implementation, implClass, context);

        if (componentType.getConstructor() == null) {
            Signature ctor = findConstructor(implClass, context);
            componentType.setConstructor(ctor);
        }

        if (componentType.getProperties().isEmpty() && componentType.getReferences().isEmpty() && componentType.getResources().isEmpty()) {
            evaluateConstructor(implementation, implClass, context);
            evaluateSetters(implementation, implClass, context);
            evaluateFields(implementation, implClass, context);
        }

        if (componentType.getInitMethod() == null) {
            componentType.setInitMethod(getCallback(implClass, "setUp"));
        }
        if (componentType.getDestroyMethod() == null) {
            componentType.setDestroyMethod(getCallback(implClass, "tearDown"));
        }
    }

    private Signature getCallback(Class<?> implClass, String name) {
        while (Object.class != implClass) {
            try {
                Method callback = implClass.getDeclaredMethod(name);
                return new Signature(callback);
            } catch (NoSuchMethodException e) {
                implClass = implClass.getSuperclass();
                continue;
            }
        }
        return null;
    }

    private Signature findConstructor(Class<?> implClass, IntrospectionContext context) {
        Constructor<?>[] constructors = implClass.getDeclaredConstructors();
        Constructor<?> selected = null;
        if (constructors.length == 1) {
            selected = constructors[0];
        } else {
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(org.osoa.sca.annotations.Constructor.class)) {
                    if (selected != null) {
                        context.addError(new AmbiguousConstructor(implClass));
                        return null;
                    }
                    selected = constructor;
                }
            }
            if (selected == null) {
                context.addError(new NoConstructorFound(implClass));
                return null;
            }
        }
        return new Signature(selected);
    }

    private void evaluateConstructor(JUnitImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        PojoComponentType componentType = implementation.getComponentType();
        Map<InjectionSite, InjectableAttribute> sites = componentType.getInjectionSites();
        Constructor<?> constructor;
        try {
            if (componentType.getConstructor() == null) {
                // there was an error with the constructor previously, just return
                return;
            }
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
            Annotation[] annotations = constructor.getParameterAnnotations()[i];
            processSite(componentType, typeMapping, name, parameterType, site, annotations, context);
        }
    }

    void evaluateSetters(JUnitImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        PojoComponentType componentType = implementation.getComponentType();
        Map<InjectionSite, InjectableAttribute> sites = componentType.getInjectionSites();
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
            Annotation[] annotations = setter.getAnnotations();
            processSite(componentType, typeMapping, name, parameterType, site, annotations, context);
        }
    }

    void evaluateFields(JUnitImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        PojoComponentType componentType = implementation.getComponentType();
        Map<InjectionSite, InjectableAttribute> sites = componentType.getInjectionSites();
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
            Annotation[] annotations = field.getAnnotations();
            processSite(componentType, typeMapping, name, parameterType, site, annotations, context);
        }
    }


    private void processSite(PojoComponentType componentType,
                             TypeMapping typeMapping,
                             String name,
                             Type parameterType,
                             InjectionSite site,
                             Annotation[] annotations,
                             IntrospectionContext context) {
        InjectableAttributeType type = helper.inferType(parameterType, typeMapping);
        switch (type) {
        case PROPERTY:
            addProperty(componentType, typeMapping, name, parameterType, site);
            break;
        case REFERENCE:
            addReference(componentType, typeMapping, name, parameterType, site, annotations, context);
            break;
        case CALLBACK:
            // ignore
            break;
        default:
            context.addError(new UnknownInjectionType(site, type, componentType.getImplClass()));
            break;
        }
    }

    private void addProperty(PojoComponentType componentType, TypeMapping typeMapping, String name, Type parameterType, InjectionSite site) {
        Property property = new Property(name, null);
        property.setMany(helper.isManyValued(typeMapping, parameterType));
        componentType.add(property, site);
    }

    private void addReference(PojoComponentType componentType,
                              TypeMapping typeMapping,
                              String name,
                              Type parameterType,
                              InjectionSite site,
                              Annotation[] annotations,
                              IntrospectionContext context) {
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, parameterType, context);
        Multiplicity multiplicity = helper.isManyValued(typeMapping, parameterType) ? Multiplicity.ONE_N : Multiplicity.ONE_ONE;
        ReferenceDefinition reference = new ReferenceDefinition(name, contract, multiplicity);
        if (policyProcessor != null) {
            for (Annotation annotation : annotations) {
                policyProcessor.process(annotation, reference, context);
            }
        }
        componentType.add(reference, site);
    }
}
