/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.ejb.runtime;

import java.util.Map;
import java.util.HashMap;
import java.net.URI;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.fabric3.spi.builder.WiringException;


/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class DefaultEjbRegistry implements EjbRegistry {

    private final Map<String, Object> registeredLinks = new HashMap<String, Object>();

    public Object resolveEjbLink(String ejbLink, Class<?> interfaceClass) throws WiringException {
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
