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
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.jms.runtime.lookup;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * Helper class for JNDI lookup.
 */
public class JndiHelper {

    private JndiHelper() {
    }

    /**
     * Looks up the administered object in JNDI.
     *
     * @param name the object name
     * @param env  environment properties
     * @return the object
     * @throws NameNotFoundException if the object was not found
     * @throws JmsLookupException    if there was an error looking up the object
     */
    public static Object lookup(String name, Hashtable<String, String> env) throws NameNotFoundException, JmsLookupException {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Context ctx = null;
        try {
            Thread.currentThread().setContextClassLoader(JndiHelper.class.getClassLoader());
            ctx = new InitialContext(env);
            return ctx.lookup(name);
        } catch (NamingException ex) {
            throw new JmsLookupException("Unable to lookup administered object", ex);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException ex) {
                throw new JmsLookupException("Unable to lookup administered object", ex);
            }
        }
    }
}
