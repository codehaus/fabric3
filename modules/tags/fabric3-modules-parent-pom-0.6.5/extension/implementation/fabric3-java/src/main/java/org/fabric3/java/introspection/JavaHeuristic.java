/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.java.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.introspection.java.UnknownInjectionType;
import org.fabric3.introspection.java.NoConstructorFound;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.validation.AmbiguousConstructor;

/**
 * @version $Rev$ $Date$
 */
public class JavaHeuristic implements HeuristicProcessor<JavaImplementation> {

    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;

    private final HeuristicProcessor<JavaImplementation> serviceHeuristic;
    private final HeuristicProcessor<JavaImplementation> dataTypeHeuristic;

    public JavaHeuristic(@Reference IntrospectionHelper helper,
                         @Reference ContractProcessor contractProcessor,
                         @Reference(name = "service")HeuristicProcessor<JavaImplementation> serviceHeuristic,
                         @Reference(name = "dataType")HeuristicProcessor<JavaImplementation> dataTypeHeuristic) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
        this.serviceHeuristic = serviceHeuristic;
        this.dataTypeHeuristic = dataTypeHeuristic;
    }

    public void applyHeuristics(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) {

        PojoComponentType componentType = implementation.getComponentType();

        // apply service heuristic
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

        // apply data type mapping heuristic
        dataTypeHeuristic.applyHeuristics(implementation, implClass, context);
    }

    Signature findConstructor(Class<?> implClass, IntrospectionContext context) {
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

    void evaluateConstructor(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        PojoComponentType componentType = implementation.getComponentType();
        Map<InjectionSite, InjectableAttribute> sites = componentType.getInjectionSites();
        Constructor<?> constructor;
        try {
            Signature ctor = componentType.getConstructor();
            if (ctor == null) {
                // there could have been an error evaluating the constructor, in which case no signature will be present
                return;
            }
            constructor = ctor.getConstructor(implClass);
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
            processSite(componentType, typeMapping, name, parameterType, site, context);
        }
    }

    void evaluateSetters(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) {
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
            processSite(componentType, typeMapping, name, parameterType, site, context);
        }
    }

    void evaluateFields(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) {
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
            processSite(componentType, typeMapping, name, parameterType, site, context);
        }
    }


    private void processSite(PojoComponentType componentType,
                             TypeMapping typeMapping,
                             String name,
                             Type parameterType,
                             InjectionSite site,
                             IntrospectionContext context) {
        InjectableAttributeType type = helper.inferType(parameterType, typeMapping);
        switch (type) {
        case PROPERTY:
            addProperty(componentType, typeMapping, name, parameterType, site);
            break;
        case REFERENCE:
            addReference(componentType, typeMapping, name, parameterType, site, context);
            break;
        case CALLBACK:
            context.addError(new UnknownInjectionType(site, type, componentType.getImplClass()));
            break;
        default:
            context.addError(new UnknownInjectionType(site, type, componentType.getImplClass()));
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
                              IntrospectionContext context) {
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, parameterType, context);
        Multiplicity multiplicity = helper.isManyValued(typeMapping, parameterType) ? Multiplicity.ONE_N : Multiplicity.ONE_ONE;
        ReferenceDefinition reference = new ReferenceDefinition(name, contract, multiplicity);
        componentType.add(reference, site);
    }
}
