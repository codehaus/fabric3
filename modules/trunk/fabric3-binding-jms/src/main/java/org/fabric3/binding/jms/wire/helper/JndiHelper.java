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

package org.fabric3.binding.jms.wire.helper;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.fabric3.binding.jms.Fabric3JmsException;

/**
 * Helper class for JNDI lookup.
 */
public class JndiHelper {
    
    /**
     * Utility class constructor.
     */
    private JndiHelper() {
    }

    /*
     * Looks up the administered object.
     */
    public static Object lookup(String name, Hashtable<String, String> env) {
        
        Context ctx = null;
                
        try {
            
            ctx = new InitialContext(env);
            return ctx.lookup(name);
            
        } catch(NameNotFoundException ex) {
            return null;
        } catch(NamingException ex) {
            throw new Fabric3JmsException("Unable to lookup administered object", ex);
        } finally {
            try {
                if(ctx != null) {
                    ctx.close();
                }
            } catch(NamingException ex) {
                throw new Fabric3JmsException("Unable to lookup administered object", ex);
            }
        }
        
    }

}
