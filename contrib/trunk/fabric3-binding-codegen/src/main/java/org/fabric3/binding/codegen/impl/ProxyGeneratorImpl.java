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
package org.fabric3.binding.codegen.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.jws.WebService;
import javax.jws.WebMethod;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;

import org.fabric3.binding.codegen.ProxyGenerator;

public class ProxyGeneratorImpl implements ProxyGenerator {

    private static final String PROXY_SUFFIX = "_Fabric3SCAProxy";

    private static final String INTERFACE_SUFFIX = "_Fabric3SCAGenInterface";

    public Object getWrapper(Class clazz, Object delegate)
            throws ClassNotFoundException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
        return getWrapper(clazz, delegate, null, null, null, null);
    }

    public Object getWrapper(Class clazz,
                             Object delegate,
                             String targetNamespace,
                             String wsdlLocation,
                             String serviceName,
                             String portName)
            throws ClassNotFoundException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
         assert clazz.isInterface();
         Class generated = getWrapperClass(clazz, targetNamespace, wsdlLocation, serviceName, portName);
         return generated.getConstructors()[0].newInstance(delegate);

    }

    private static CodeGenClassLoader getCodeGenClassLoader(Class clazz) {
         CodeGenClassLoader target;
         ClassLoader cl = clazz.getClassLoader();
         if (cl instanceof CodeGenClassLoader) {
             target = (CodeGenClassLoader) cl;
         } else {
             target = new CodeGenClassLoader(null, cl);
         }
         return target;
     }


    public Class getWrapperClass(Class clazz,
                                String targetNamespace,
                                String wsdlLocation,
                                String serviceName,
                                String portName) throws ClassNotFoundException {
         String interfaceName = clazz.getName();
         String internalInterfaceName = interfaceName.replace('.', '/');
         String internalClassName = internalInterfaceName + PROXY_SUFFIX;
         int index = internalInterfaceName.lastIndexOf('/');
         String className = interfaceName + PROXY_SUFFIX;
         String fieldName;
         fieldName = index > 0 ? internalInterfaceName.substring(index + 1).toLowerCase() :
                 internalInterfaceName.toLowerCase();
         byte[] b = new ServiceProxy(
                 internalClassName, internalInterfaceName, fieldName, clazz,
                 targetNamespace, wsdlLocation, serviceName, portName).generateByteCode();
        CodeGenClassLoader cl = getCodeGenClassLoader(clazz);
         return cl.defineClass(className, b);
     }


    public Class getWrapperInterface(Class clazz,
                                    String targetNamespace,
                                    String wsdlLocation,
                                    String serviceName,
                                    String portName) throws ClassNotFoundException {
        String interfaceName = clazz.getName();
        String internalInterfaceName = interfaceName.replace('.', '/');
        String internalClassName = internalInterfaceName + INTERFACE_SUFFIX;
        String className = interfaceName + INTERFACE_SUFFIX;
        byte[] b = new InterfaceGenerator(internalClassName, clazz,
                                          targetNamespace, wsdlLocation, serviceName, portName).generateByteCode();
        CodeGenClassLoader cl = getCodeGenClassLoader(clazz);
        return cl.defineClass(className, b);
    }

    private static class InterfaceGenerator implements Opcodes {
        private final ClassWriter cw = new ClassWriter(0);

        private InterfaceGenerator(String className, Class clazz,
                               String targetNamespace,
                               String wsdlLocation,
                               String serviceName,
                               String portName) {
            cw.visit(V1_5, ACC_INTERFACE | ACC_PUBLIC, className, null, "java/rmi/Remote", null);
            AnnotationVisitor av = cw.visitAnnotation(getSignature(WebService.class), true);
            if (targetNamespace != null)
              av.visit("targetNamespace", targetNamespace);
            if (wsdlLocation != null)
              av.visit("wsdlLocation", wsdlLocation);
            if (serviceName != null)
              av.visit("serviceName", serviceName);
            if (portName != null)
              av.visit("portName", portName);
            av.visitEnd();
            Method[] methods = clazz.getMethods();
            for (Method m : methods) {
                addMethodCode(m);
            }
            cw.visitEnd();
        }


        private void addMethodCode(Method m) {
            MethodVisitor mv;
            mv = cw.visitMethod(ACC_PUBLIC | ACC_FINAL, m.getName(),
                                computeSignature(m), null, null);
            mv.visitAnnotation(getSignature(WebMethod.class), true);
            mv.visitEnd();
        }



        private String computeSignature(Method m) {
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
            if (clazz == Void.TYPE) return "V";
            if (clazz == Byte.TYPE) return "B";
            else if (clazz == Character.TYPE) return "C";
            else if (clazz == Double.TYPE) return "D";
            else if (clazz == Float.TYPE) return "F";
            else if (clazz == Integer.TYPE) return "I";
            else if (clazz == Long.TYPE) return "J";
            else if (clazz == Short.TYPE) return "S";
            else if (clazz == Boolean.TYPE) return "Z";
            else {
                if (!clazz.getName().startsWith("[")) {
                    return "L" + clazz.getName().replace('.', '/') + ";";
                } else {
                    return "[" + clazz.getName().replace('.', '/') + ";";
                }
            }
        }

        private byte[] generateByteCode() {
            return cw.toByteArray();
        }

    }

     private static class ServiceProxy implements Opcodes {

         private final ClassWriter cw = new ClassWriter(0);

         private ServiceProxy(String className, String interfaceName,
                              String fieldName, Class clazz,
                                String targetNamespace,
                                String wsdlLocation,
                                String serviceName,
                                String portName) {
             cw.visit(V1_5, ACC_PUBLIC | ACC_FINAL, className, null, "java/lang/Object",
                      new String[]{"java/rmi/Remote", interfaceName});
             AnnotationVisitor av = cw.visitAnnotation(getSignature(WebService.class), true);
             if (targetNamespace != null)
               av.visit("targetNamespace", targetNamespace);
             if (wsdlLocation != null)
               av.visit("wsdlLocation", wsdlLocation);
             if (serviceName != null)
               av.visit("serviceName", serviceName);
             if (portName != null)
               av.visit("portName", portName);
             av.visitEnd();

             String fieldType = getSignature(clazz);
             cw.visitField(ACC_PRIVATE, fieldName, fieldType, null, null).visitEnd();
             addConstructor(className, fieldName, fieldType);
             Method[] methods = clazz.getMethods();
             for (Method m : methods) {
                 addMethodCode(className, fieldName, fieldType, m, interfaceName);
             }
             cw.visitEnd();
         }

         private void addConstructor(String className, String fieldName, String fieldType) {
             MethodVisitor mv = cw.visitMethod(ACC_PUBLIC,
                                               "<init>", "(" + fieldType + ")V", null, null);
             mv.visitCode();
             mv.visitIntInsn(ALOAD, 0);
             mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
             mv.visitIntInsn(ALOAD, 0);
             mv.visitIntInsn(ALOAD, 1);
             mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);
             mv.visitInsn(RETURN);
             mv.visitMaxs(2, 2);
             mv.visitEnd();
         }

         private void addMethodCode(String className, String fieldName,
                                    String type, Method m, String interfaceName) {
             MethodVisitor mv;
             String signature = computeSignature(m);
             mv = cw.visitMethod(ACC_PUBLIC | ACC_FINAL, m.getName(),
                                 computeSignature(m), null, null);
             mv.visitCode();
             mv.visitIntInsn(ALOAD, 0); //Load this
             mv.visitFieldInsn(GETFIELD, className, fieldName, type);
             Class[] parameters = m.getParameterTypes();
             int maxCount = 1;
             int nextInstruction = 0;
             boolean skipTwo = false;
             for (Class parameter : parameters) {
                 int code = getLoadCode(parameter);
                 if (code == LLOAD || code == DLOAD) {
                     maxCount += 2;
                     skipTwo = true;
                 } else {
                     maxCount++;
                 }
                 mv.visitIntInsn(code, ++nextInstruction);
                 if (skipTwo) nextInstruction++;
             }
             mv.visitMethodInsn(INVOKEINTERFACE, interfaceName, m.getName(), signature);
             mv.visitInsn(getReturnCode(m.getReturnType()));
             mv.visitMaxs(Math.max(1, maxCount), maxCount);
             mv.visitEnd();
         }

         private int getLoadCode(Class clazz) {
             if (clazz == Integer.TYPE || clazz == Byte.TYPE ||
                     clazz == Character.TYPE || clazz == Short.TYPE ||
                     clazz == Boolean.TYPE) {
                 if (clazz.isArray()) {
                     return IALOAD;
                 }
                 return ILOAD;
             } else if (clazz == Double.TYPE) {
                 if (clazz.isArray()) {
                     return DALOAD;
                 }
                 return DLOAD;
             } else if (clazz == Float.TYPE) {
                 if (clazz.isArray()) {
                     return FALOAD;
                 }
                 return FLOAD;
             } else if (clazz == Long.TYPE) {
                 if (clazz.isArray()) {
                     return LALOAD;
                 }
                 return LLOAD;
             }
             if (clazz.isArray()) {
                 return AALOAD;
             }
             return ALOAD;
         }

         private int getReturnCode(Class clazz) {
             if (clazz == Void.TYPE) {
                 return RETURN;
             } else if (clazz == Integer.TYPE || clazz == Byte.TYPE ||
                     clazz == Character.TYPE || clazz == Short.TYPE ||
                     clazz == Boolean.TYPE) {
                 return IRETURN;
             } else if (clazz == Double.TYPE) {
                 return DRETURN;
             } else if (clazz == Float.TYPE) {
                 return FRETURN;
             } else if (clazz == Long.TYPE) {
                 return LRETURN;
             }
             return ARETURN;

         }

         private String computeSignature(Method m) {
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
             if (clazz == Void.TYPE) return "V";
             if (clazz == Byte.TYPE) return "B";
             else if (clazz == Character.TYPE) return "C";
             else if (clazz == Double.TYPE) return "D";
             else if (clazz == Float.TYPE) return "F";
             else if (clazz == Integer.TYPE) return "I";
             else if (clazz == Long.TYPE) return "J";
             else if (clazz == Short.TYPE) return "S";
             else if (clazz == Boolean.TYPE) return "Z";
             else {
                 if (!clazz.getName().startsWith("[")) {
                     return "L" + clazz.getName().replace('.', '/') + ";";
                 } else {
                     return "[" + clazz.getName().replace('.', '/') + ";";
                 }
             }
         }

         private byte[] generateByteCode() {
             return cw.toByteArray();
         }

     }

}
