package org.fabric3.itest.implementation.junit;

import java.beans.IntrospectionException;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.pojo.reflection.ReflectiveInstanceFactoryProvider;
import org.fabric3.fabric.component.instancefactory.IFProviderBuilderException;
import org.fabric3.fabric.component.instancefactory.impl.AbstractIFProviderBuilder;
import org.fabric3.fabric.component.instancefactory.impl.UnknownInjectionSiteException;
import org.fabric3.pojo.reflection.definition.InjectionSiteMapping;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.pojo.reflection.definition.MemberSite;
import org.fabric3.fabric.util.JavaIntrospectionHelper;

/**
 * Builds a reflection-based instance factory provider.
 *
 * @version $Date$ $Revision$
 */
@EagerInit
public class JnitIFProviderBuilder<T> extends
        AbstractIFProviderBuilder<ReflectiveInstanceFactoryProvider<T>, JUnitIFProviderDefinition> {

    @Override
    protected Class<JUnitIFProviderDefinition> getIfpdClass() {
        return JUnitIFProviderDefinition.class;
    }

    @SuppressWarnings("unchecked")
    public ReflectiveInstanceFactoryProvider<T> build(JUnitIFProviderDefinition ifpd, ClassLoader cl)
            throws IFProviderBuilderException {

        try {
            Class implClass = cl.loadClass(ifpd.getImplementationClass());
            Constructor ctr = getConstructor(ifpd, cl, implClass);
            Method initMethod = getCallBackMethod(implClass, ifpd.getInitMethod());
            Method destroyMethod = getCallBackMethod(implClass, ifpd.getDestroyMethod());
            List<ValueSource> ctrInjectSites = ifpd.getCdiSources();
            Map<ValueSource, Member> injectionSites = getInjectionSites(ifpd, implClass);

            return new ReflectiveInstanceFactoryProvider<T>(ctr,
                                                            ctrInjectSites,
                                                            injectionSites,
                                                            initMethod,
                                                            destroyMethod);

        } catch (ClassNotFoundException ex) {
            throw new IFProviderBuilderException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IFProviderBuilderException(ex);
        } catch (NoSuchFieldException ex) {
            throw new IFProviderBuilderException(ex);
        } catch (IntrospectionException ex) {
            throw new IFProviderBuilderException(ex);
        }
    }

    /*
     * Get injection sites.
     */
    private Map<ValueSource, Member> getInjectionSites(JUnitIFProviderDefinition ifpd, Class implClass)
            throws NoSuchFieldException, IntrospectionException, IFProviderBuilderException {

        Map<ValueSource, Member> injectionSites = new HashMap<ValueSource, Member>();
        for (InjectionSiteMapping injectionSite : ifpd.getInjectionSites()) {

            ValueSource source = injectionSite.getSource();
            MemberSite memberSite = injectionSite.getSite();
            ElementType elementType = memberSite.getElementType();
            String name = memberSite.getName();

            Member member = null;
            if (memberSite.getElementType() == ElementType.FIELD) {
                member = implClass.getDeclaredField(name);
            } else if (elementType == ElementType.METHOD) {
                // FIXME look up directly based on signature sent in RIFPD
                Method[] methods = implClass.getMethods();
                for (Method method : methods) {
                    if (name.equals(method.getName())) {
                        member = method;
                        break;
                    }
                }
            }
            if (member == null) {
                throw new UnknownInjectionSiteException(name);
            }
            injectionSites.put(source, member);
        }
        return injectionSites;
    }

    private Method getCallBackMethod(Class<?> implClass, String name) throws NoSuchMethodException {
        if ("setUp".equals(name)) {
            Set<Method> methods = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(implClass);
            for (Method method : methods) {
                if (method.getName().equals("setUp")) {
                    return method;
                }
            }
            throw new NoSuchMethodException("setUp()");
        } else if ("tearDown".equals(name)){
            Set<Method> methods = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(implClass);
            for (Method method : methods) {
                if (method.getName().equals("tearDown")) {
                    return method;
                }
            }
            throw new NoSuchMethodException("tearDown()");
        }
        // JFM FIXME this needs to handle overloaded methods
        return name == null ? null : implClass.getMethod(name);
    }

    /*
     * Gets the matching constructor.
     */
    private Constructor getConstructor(JUnitIFProviderDefinition ifpd, ClassLoader cl, Class implClass)
            throws ClassNotFoundException, NoSuchMethodException {
        List<String> argNames = ifpd.getConstructorArguments();
        Class[] ctrArgs = new Class[argNames.size()];
        for (int i = 0; i < ctrArgs.length; i++) {
            ctrArgs[i] = getArgType(argNames.get(i), cl);
        }
        return implClass.getDeclaredConstructor(ctrArgs);
    }

    // xcv test this
    private Class<?> getArgType(String name, ClassLoader cl) throws ClassNotFoundException {
        if ("int".equals(name)) {
            return Integer.TYPE;
        } else if ("short".equals(name)) {
            return Short.TYPE;
        } else if ("byte".equals(name)) {
            return Byte.TYPE;
        } else if ("char".equals(name)) {
            return Character.TYPE;
        } else if ("long".equals(name)) {
            return Long.TYPE;
        } else if ("float".equals(name)) {
            return Float.TYPE;
        } else if ("double".equals(name)) {
            return Double.TYPE;
        } else if ("boolean".equals(name)) {
            return Boolean.TYPE;
        }
        return cl.loadClass(name);
    }


}
