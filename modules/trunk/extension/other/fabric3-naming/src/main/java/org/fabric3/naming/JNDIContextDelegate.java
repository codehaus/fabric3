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

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;

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
    public JNDIContextDelegate(@Property(name = "jndiProperties", required = false) Properties properties) throws NamingException {
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
