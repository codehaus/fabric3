package org.fabric3.binding.codegen.impl;

import java.security.SecureClassLoader;
import java.security.CodeSource;

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

public final class CodeGenClassLoader  extends SecureClassLoader {
    private static final CodeSource CS =
            CodeGenClassLoader.class.getProtectionDomain().getCodeSource();
    private final String name;

    public CodeGenClassLoader(String name, ClassLoader parent) {
        super(parent);
        this.name = name;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" name: ").append(name);
        return sb.toString();
    }


    public Class defineClass(String name, byte[] bytes) {
        return super.defineClass(name, bytes, 0, bytes.length, CS);
    }

}

