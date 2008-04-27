/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.naming;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Destroy;

/**
 * Implementation of a JNDI Context that delegates to a Context constructed from the supplied properties. If no properties are supplied then the
 * default InitialContext is used; this supports integration with managed container environments.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JNDIContextDelegate implements Context {
    private final Context delegate;

    /**
     * Constructor specifying the JNDI properties to use. If properties are omitted then the default InitialContext is used.
     *
     * @param properties JNDI configuration properties
     * @throws NamingException if there was a problem creating the InitialContext
     */
    public JNDIContextDelegate(@Property(name = "jndiProperties", required = false)Properties properties) throws NamingException {
        if (properties == null) {
            delegate = new InitialContext();
        } else {
            delegate = new InitialContext(properties);
        }
    }

    @Destroy
    protected void destroy() throws NamingException {
        delegate.close();
    }

    public void close() throws NamingException {
        // do nothing - the context will be closed when the component is destroyed
    }

    public synchronized Object lookup(Name name) throws NamingException {
        return delegate.lookup(name);
    }

    public synchronized Object lookup(String s) throws NamingException {
        return delegate.lookup(s);
    }

    public synchronized void bind(Name name, Object o) throws NamingException {
        delegate.bind(name, o);
    }

    public synchronized void bind(String s, Object o) throws NamingException {
        delegate.bind(s, o);
    }

    public synchronized void rebind(Name name, Object o) throws NamingException {
        delegate.rebind(name, o);
    }

    public synchronized void rebind(String s, Object o) throws NamingException {
        delegate.rebind(s, o);
    }

    public synchronized void unbind(Name name) throws NamingException {
        delegate.unbind(name);
    }

    public synchronized void unbind(String s) throws NamingException {
        delegate.unbind(s);
    }

    public synchronized void rename(Name name, Name name1) throws NamingException {
        delegate.rename(name, name1);
    }

    public synchronized void rename(String s, String s1) throws NamingException {
        delegate.rename(s, s1);
    }

    public synchronized NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return delegate.list(name);
    }

    public synchronized NamingEnumeration<NameClassPair> list(String s) throws NamingException {
        return delegate.list(s);
    }

    public synchronized NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return delegate.listBindings(name);
    }

    public synchronized NamingEnumeration<Binding> listBindings(String s) throws NamingException {
        return delegate.listBindings(s);
    }

    public synchronized void destroySubcontext(Name name) throws NamingException {
        delegate.destroySubcontext(name);
    }

    public synchronized void destroySubcontext(String s) throws NamingException {
        delegate.destroySubcontext(s);
    }

    public synchronized Context createSubcontext(Name name) throws NamingException {
        return delegate.createSubcontext(name);
    }

    public synchronized Context createSubcontext(String s) throws NamingException {
        return delegate.createSubcontext(s);
    }

    public synchronized Object lookupLink(Name name) throws NamingException {
        return delegate.lookupLink(name);
    }

    public synchronized Object lookupLink(String s) throws NamingException {
        return delegate.lookupLink(s);
    }

    public synchronized NameParser getNameParser(Name name) throws NamingException {
        return delegate.getNameParser(name);
    }

    public synchronized NameParser getNameParser(String s) throws NamingException {
        return delegate.getNameParser(s);
    }

    public synchronized Name composeName(Name name, Name name1) throws NamingException {
        return delegate.composeName(name, name1);
    }

    public synchronized String composeName(String s, String s1) throws NamingException {
        return delegate.composeName(s, s1);
    }

    public synchronized Object addToEnvironment(String s, Object o) throws NamingException {
        return delegate.addToEnvironment(s, o);
    }

    public synchronized Object removeFromEnvironment(String s) throws NamingException {
        return delegate.removeFromEnvironment(s);
    }

    public synchronized Hashtable<?, ?> getEnvironment() throws NamingException {
        return delegate.getEnvironment();
    }

    public synchronized String getNameInNamespace() throws NamingException {
        return delegate.getNameInNamespace();
    }
}
