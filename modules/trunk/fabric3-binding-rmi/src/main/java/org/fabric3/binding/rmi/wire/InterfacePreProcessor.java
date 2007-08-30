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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.TreeSet;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.osoa.sca.annotations.Remotable;

public class InterfacePreProcessor {

    private static final int VERSION_15 = 49;
    private static final Class REMOTABLE = Remotable.class;

    public static Class generateRemoteInterface(String name, InputStream in, CodeGenClassLoader cl)
            throws IOException {
        ClassReader cr = new ClassReader(new BufferedInputStream(in));
        ClassWriter cw = new ClassWriter(cr, 0);
        Local2RemoteInterfaceTransformer transformer = new Local2RemoteInterfaceTransformer(cw);
        cr.accept(transformer, 0);
        return cl.defineClass(name, cw.toByteArray());
    }


    private static class Local2RemoteInterfaceTransformer extends ClassAdapter {

        private static final String REMOTE = "java/rmi/Remote";
        private static final String REMOTE_EXCEPTION = "java/rmi/RemoteException";
        private static final String[] EXCEPTIONS = new String[]{REMOTE_EXCEPTION};

        private final ClassVisitor classvisitor;

        public Local2RemoteInterfaceTransformer(ClassVisitor classVisitor) {
            super(classVisitor);
            this.classvisitor = classVisitor;
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            ArrayList<String> list = new ArrayList<String>(interfaces.length + 1);
            for (String intf : interfaces) {
                list.add(intf);
            }
            list.add(REMOTE);
            String[] result = new String[list.size()];
            result = list.toArray(result);
            classvisitor.visit(VERSION_15, access, name, signature, superName, result);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name,
                                         String desc, String signature,
                                         String[] exceptions) {
            return classvisitor.visitMethod(access, name, desc, signature,
                                            getExceptions(exceptions));
        }


        private static String[] getExceptions(String[] existing) {
            if (existing != null && existing.length > 0) {
                TreeSet<String> set = new TreeSet<String>();
                for (String exception : existing) {
                    set.add(exception);
                }
                set.add(REMOTE_EXCEPTION);
                String[] newExceptions = new String[set.size()];
                set.toArray(newExceptions);
                return newExceptions;
            }
            return EXCEPTIONS;
        }

    }

}
