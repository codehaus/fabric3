/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.binding.rmi.wire;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/* package */ final class WireProxyGenerator implements Opcodes {

    private static final String PROXY_SUFFIX = "_Fabric3SCAProxy";

    private static URL codebaseURL;

    static {

        CodeSource cs = WireProxyGenerator.class.getProtectionDomain().getCodeSource();
        if (cs != null) {
            codebaseURL = cs.getLocation();
        } else {
            // this happens when weblogic classes are in the bootclasspath.
            // revert to the old behavior then.
            File lib = new File("lib");
            try {
                codebaseURL = lib.toURL();
            } catch (Exception mue) {
                codebaseURL = null;
            }
        }
    }

    private static final class SingletonMaker {
        private static final WireProxyGenerator THE_ONE =
                new WireProxyGenerator();
    }

    private WireProxyGenerator() {
    }

    public static WireProxyGenerator getInstance() {
        return SingletonMaker.THE_ONE;
    }

    /*
    */
/**
 * @param clazz    Interface to be wrapped
 * @param delegate
 * @return
 */

    public Object generateRemoteWrapper(Class clazz, Object delegate)
            throws ClassNotFoundException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
        assert clazz.isInterface();
        CodeGenClassLoader target = null;
        ClassLoader cl = clazz.getClassLoader();
        if (cl instanceof CodeGenClassLoader) {
            target = (CodeGenClassLoader) cl;
        } else {
            target = new CodeGenClassLoader(null, cl);
        }
        Class generated = generateProxy(clazz, target);
        return generated.getConstructors()[0].newInstance(delegate);
    }

    public Class generateProxy(Class clazz,
                               CodeGenClassLoader cl) throws ClassNotFoundException {
        String interfaceName = clazz.getName();
        String internalInterfaceName = interfaceName.replace('.', '/');
        String internalClassName = internalInterfaceName + PROXY_SUFFIX;
        int index = internalInterfaceName.lastIndexOf('/');
        String className = interfaceName + PROXY_SUFFIX;
        String fieldName;
        fieldName = index > 0 ? internalInterfaceName.substring(index + 1).toLowerCase() :
                internalInterfaceName.toLowerCase();
        byte[] b = new ServiceProxy(
                internalClassName, internalInterfaceName, fieldName, clazz).generateByteCode();
        return cl.defineClass(className, b);
    }

    /*public byte[] testProxy(String interfaceName) throws ClassNotFoundException
  {
      Class clazz = ClassLoader.getSystemClassLoader().loadClass(interfaceName);
      String internalInterfaceName = interfaceName.replace('.', '/');
      String internalClassName = internalInterfaceName + PROXY_SUFFIX;
      int index = internalInterfaceName.lastIndexOf('/');
      String className = interfaceName + PROXY_SUFFIX;
      String fieldName;
      fieldName = index > 0 ? internalInterfaceName.substring(index + 1).toLowerCase() :
          internalInterfaceName.toLowerCase();
      byte[] b = new ServiceProxy(
          internalClassName, internalInterfaceName, fieldName, clazz).generateByteCode();
      new TestClassLoader().defineClass(className, b, codebaseURL);
      return b;
    }

    public static void main(String[] args) throws Exception {
      SCAWireProxyGenerator proxygen = SCAWireProxyGenerator.getInstance();
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("/tmp/foo/Test_SCAProxy.class"));
      long start = System.currentTimeMillis();
      byte[] b = proxygen.testProxy(args[0]);
      long end = System.currentTimeMillis();

      System.out.println("Time to generate proxy " + (end - start));
      out.write(b, 0, b.length);
      out.flush();
      out.close();
    }

    private static class TestClassLoader extends SecureClassLoader {

      public void defineClass(String className, byte[] b, URL codebase) {
        defineClass(className, b, 0, b.length, (CodeSource) null);
      }

    }*/

    private static class ServiceProxy implements Opcodes {

        private final ClassWriter cw = new ClassWriter(0);

        private ServiceProxy(String className, String interfaceName,
                             String fieldName, Class clazz) {
            cw.visit(V1_5, ACC_PUBLIC | ACC_FINAL, className, null, "java/lang/Object",
                     new String[]{"java/rmi/Remote", interfaceName});
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
