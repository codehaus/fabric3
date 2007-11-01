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
package org.fabric3.binding.ejb.wire;

import java.util.Map;
import java.util.HashMap;
import java.net.URI;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.fabric3.binding.ejb.spi.EjbRegistry;
import org.fabric3.spi.builder.WiringException;


/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class DefaultEjbRegistry implements EjbRegistry {

    private final Map registeredLinks = new HashMap();

    public Object resolveEjbLink(String ejbLink, Class interfaceClass) throws WiringException {
        Object ejb = registeredLinks.get(ejbLink);
        if(ejb == null) {
            throw new WiringException("Unable to resolve ejb-link-name "+ejbLink);
        }

        return ejb;
    }

    public void registerEjbLink(String ejbLink, Object ejb) throws WiringException {
        registeredLinks.put(ejbLink, ejb);
    }

    public Object resolveEjb(URI uri) throws WiringException {

        try {

            String name = uri.getPath();
            if(uri.getFragment() != null) {
                name = name + '/' + uri.getFragment();
            }

            InitialContext ic = new InitialContext();
            return ic.lookup(name);

        } catch(NamingException ne) {
            throw new WiringException("Error resolving EJB binding at URI: " + uri, uri.toString(), ne);
        }

    }

    public void registerEjb(URI uri, Object ejb) throws WiringException {

        try {

            String name = uri.getPath();

            if(uri.getFragment() != null) {
                name = name + '/' + uri.getFragment();
            }

            InitialContext ic = new InitialContext();
            ic.bind(name, ejb);

        } catch (NamingException ne) {
            throw new WiringException("Error binding EJB binding to URI: " + uri, uri.toString(), ne);
        }
    }

}
