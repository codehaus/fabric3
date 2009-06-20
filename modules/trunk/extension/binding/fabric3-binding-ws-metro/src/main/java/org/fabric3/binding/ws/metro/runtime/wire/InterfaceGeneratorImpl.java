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
package org.fabric3.binding.ws.metro.runtime.wire;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import javax.jws.WebMethod;
import javax.jws.WebService;

import org.oasisopen.sca.annotation.OneWay;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Service;

/**
 * @version $Rev$ $Date$
 */
@Service(InterfaceGenerator.class)
public class InterfaceGeneratorImpl implements InterfaceGenerator, Opcodes {
    private static final String SUFFIX = "_F3Subtype";
    private Method method;

    @Init
    public void init() throws NoSuchMethodException {
        method = SecureClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE, CodeSource.class);
        method.setAccessible(true);
    }

    public Class<?> generateAnnotatedInterface(Class interfaze, String targetNamespace, String wsdlLocation, String serviceName, String portName)
            throws InterfaceGenerationException {
        if (!(interfaze.getClassLoader() instanceof SecureClassLoader)) {
            throw new InterfaceGenerationException("Classloader for " + interfaze.getName() + " must be a SecureClassLoader");
        }
        SecureClassLoader loader = (SecureClassLoader) interfaze.getClassLoader();

        String name = interfaze.getName();
        String generatedName = name + SUFFIX;
        String internalName = name.replace('.', '/');
        String generatedInternalName = internalName + SUFFIX;
        try {
            // check if the class was already generated
            return loader.loadClass(generatedName);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        ClassWriter cw = new ClassWriter(0);
        byte[] bytes = generate(cw, generatedInternalName, interfaze, targetNamespace, wsdlLocation, serviceName, portName);
        return defineClass(generatedName, bytes, loader);
    }

    private byte[] generate(ClassWriter cw,
                            String className,
                            Class clazz,
                            String targetNamespace,
                            String wsdlLocation,
                            String serviceName,
                            String portName) {
        String[] interfaces = {clazz.getName().replace('.', '/')};
        cw.visit(V1_5, ACC_INTERFACE | ACC_PUBLIC, className, null, "java/lang/Object", interfaces);
        // add @WebService
        AnnotationVisitor av = cw.visitAnnotation(getSignature(WebService.class), true);
        if (targetNamespace != null) {
            av.visit("targetNamespace", targetNamespace);
        }
        if (wsdlLocation != null) {
            av.visit("wsdlLocation", wsdlLocation);
        }
        if (serviceName != null) {
            av.visit("serviceName", serviceName);
        }
        if (portName != null) {
            av.visit("portName", portName);
        }
        av.visitEnd();

        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            generateMethod(cw, m);
        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    private void generateMethod(ClassWriter cw, Method m) {
        MethodVisitor mv;
        String signature = getSignature(m);
        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, m.getName(), signature, null, null);
        // add @WebMethod
        AnnotationVisitor av = mv.visitAnnotation(getSignature(WebMethod.class), true);
        av.visitEnd();
        if (m.isAnnotationPresent(OneWay.class) || m.isAnnotationPresent(org.osoa.sca.annotations.OneWay.class)) {
            // add the JAX-WS one-way equivalent
            AnnotationVisitor oneWay = mv.visitAnnotation(getSignature(javax.jws.Oneway.class), true);
            oneWay.visitEnd();
        }
        mv.visitEnd();
    }

    private String getSignature(Method m) {
        StringBuilder sb = new StringBuilder("(");
        Class[] parameters = m.getParameterTypes();
        for (Class parameter : parameters) {
            sb.append(getSignature(parameter));
        }
        sb.append(')');
        sb.append(getSignature(m.getReturnType()));
        return sb.toString();
    }

    private String getSignature(Class clazz) {
        if (clazz == Void.TYPE) {
            return "V";
        }
        if (clazz == Byte.TYPE) {
            return "B";
        } else if (clazz == Character.TYPE) {
            return "C";
        } else if (clazz == Double.TYPE) {
            return "D";
        } else if (clazz == Float.TYPE) {
            return "F";
        } else if (clazz == Integer.TYPE) {
            return "I";
        } else if (clazz == Long.TYPE) {
            return "J";
        } else if (clazz == Short.TYPE) {
            return "S";
        } else if (clazz == Boolean.TYPE) {
            return "Z";
        } else if (!clazz.getName().startsWith("[")) {
            // object
            return "L" + clazz.getName().replace('.', '/') + ";";
        } else {
            // array
            return clazz.getName().replace('.', '/');
        }
    }

    private Class<?> defineClass(String name, byte[] bytes, SecureClassLoader loader) throws InterfaceGenerationException {
        // Hack to load the class in the original interface's classloader
        try {
            return (Class<?>) method.invoke(loader, name, bytes, 0, bytes.length, getClass().getProtectionDomain().getCodeSource());
        } catch (IllegalAccessException e) {
            throw new InterfaceGenerationException(e);
        } catch (InvocationTargetException e) {
            throw new InterfaceGenerationException(e);
        }
    }

}
