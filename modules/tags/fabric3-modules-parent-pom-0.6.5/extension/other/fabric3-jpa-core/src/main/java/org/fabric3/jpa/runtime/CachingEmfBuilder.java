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
package org.fabric3.jpa.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;

import org.fabric3.jpa.spi.delegate.EmfBuilderDelegate;
import org.fabric3.jpa.spi.EmfBuilderException;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

/**
 * Creates entity manager factories using the JPA provider SPI. Creation of entity manager factories are expensive operations and hence created
 * instances are cached.
 *
 * @version $Revision$ $Date$
 */
@Service(interfaces = {EmfBuilder.class, EmfCache.class})
public class CachingEmfBuilder implements EmfBuilder, EmfCache {

    private Map<String, EntityManagerFactory> cache = new HashMap<String, EntityManagerFactory>();
    private PersistenceUnitScanner scanner;
    private Map<String, EmfBuilderDelegate> delegates = new HashMap<String, EmfBuilderDelegate>();

    /**
     * Injects the scanner.
     *
     * @param scanner Injected scanner.
     */
    public CachingEmfBuilder(@Reference PersistenceUnitScanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Injects the delegates.
     *
     * @param delegates Provider specific delegates.
     */
    @Reference(required = false)
    public void setDelegates(Map<String, EmfBuilderDelegate> delegates) {
        this.delegates = delegates;
    }

    public synchronized EntityManagerFactory build(String unitName, ClassLoader classLoader) throws EmfBuilderException {

        if (cache.containsKey(unitName)) {
            return cache.get(unitName);
        }

        EntityManagerFactory emf = createEntityManagerFactory(unitName, classLoader);
        cache.put(unitName, emf);

        return emf;

    }

    /**
     * Closes the entity manager factories.
     */
    @Destroy
    public void destroy() {
        for (EntityManagerFactory emf : cache.values()) {
            if (emf != null) {
                emf.close();
            }
        }
    }

    public EntityManagerFactory getEmf(String unitName) {
        return cache.get(unitName);
    }

    /*
    * Creates the entity manager factory using the JPA provider API.
    */
    private EntityManagerFactory createEntityManagerFactory(String unitName, ClassLoader classLoader) throws EmfBuilderException {

        PersistenceUnitInfoImpl info = (PersistenceUnitInfoImpl) scanner.getPersistenceUnitInfo(unitName, classLoader);
        String providerClass = info.getPersistenceProviderClassName();
        String dataSourceName = info.getDataSourceName();

        EmfBuilderDelegate delegate = delegates.get(providerClass);
        if (delegate != null) {
            return delegate.build(info, classLoader, dataSourceName);
        }

        // No configured delegates, try standard JPA
        try {
            PersistenceProvider provider = (PersistenceProvider) classLoader.loadClass(providerClass).newInstance();
            return provider.createContainerEntityManagerFactory(info, Collections.emptyMap());
        } catch (InstantiationException ex) {
            throw new EmfBuilderException(ex);
        } catch (IllegalAccessException ex) {
            throw new EmfBuilderException(ex);
        } catch (ClassNotFoundException ex) {
            throw new EmfBuilderException(ex);
        }

    }

}
