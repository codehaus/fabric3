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
package org.fabric3.resource.jndi.proxy;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

/**
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class AbstractProxy<D> {
    
    private static final String COMP_ENV = "java:comp/env/";
    
    private D delegate;
    private String jndiName;
    private String providerUrl;
    private String initialContextFactory;
    private boolean env;
    
    /**
     * Gets a reference to the delegate that is proxied.
     * 
     * @return Delegate that is proxied.
     */
    protected D getDelegate() {
        return delegate;
    }
    
    @Init
    @SuppressWarnings("unchecked")
    public void init() throws NamingException {
        
        Context ctx = null;
        
        try {
            
            Properties prop = new Properties();
            if(providerUrl != null) {
                prop.put(Context.PROVIDER_URL, providerUrl);
            }
            if(initialContextFactory != null) {
                prop.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            }
            
            if(env) {
                jndiName = COMP_ENV + jndiName;
            }
            
            ctx = new InitialContext(prop);
            
            delegate = (D) ctx.lookup(jndiName);
            
        } finally {
            if(ctx != null) {
                ctx.close();
            }
        }
        
    }

    /**
     * @param jndiName the jndiName to set
     */
    @Property(required = true)
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    /**
     * @param providerUrl the providerUrl to set
     */
    @Property
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    /**
     * @param initialContextFactory the initialContextFactory to set
     */
    @Property
    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    /**
     * @param env the env to set
     */
    @Property
    public void setEnv(boolean env) {
        this.env = env;
    }
    

}
