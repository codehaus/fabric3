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
package org.fabric3.fabric.policy;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.definitions.AbstractDefinition;
import org.fabric3.model.type.definitions.BindingType;
import org.fabric3.model.type.definitions.ImplementationType;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.policy.PolicyActivationException;
import org.fabric3.spi.policy.PolicyRegistry;

/**
 * Default implementation of the policy registry.
 *
 * @version $Rev$ $Date$
 */
public class DefaultPolicyRegistry implements PolicyRegistry {

    private MetaDataStore metaDataStore;
    private Map<Class<? extends AbstractDefinition>, Map<QName, ? extends AbstractDefinition>> cache =
            new ConcurrentHashMap<Class<? extends AbstractDefinition>, Map<QName, ? extends AbstractDefinition>>();

    /**
     * Initializes the cache.
     *
     * @param metaDataStore the metadata store
     */
    public DefaultPolicyRegistry(@Reference MetaDataStore metaDataStore) {
        this.metaDataStore = metaDataStore;
        cache.put(Intent.class, new ConcurrentHashMap<QName, Intent>());
        cache.put(PolicySet.class, new ConcurrentHashMap<QName, PolicySet>());
        cache.put(BindingType.class, new ConcurrentHashMap<QName, BindingType>());
        cache.put(ImplementationType.class, new ConcurrentHashMap<QName, ImplementationType>());
    }

    public <D extends AbstractDefinition> Collection<D> getAllDefinitions(Class<D> definitionClass) {
        return getSubCache(definitionClass).values();
    }

    public List<PolicySet> getExternalAttachmentPolicies() {
        Map<QName, PolicySet> subCache = getSubCache(PolicySet.class);
        List<PolicySet> policySets = new ArrayList<PolicySet>();
        for (PolicySet policySet : subCache.values()) {
            if (policySet.getAttachTo() != null) {
                policySets.add(policySet);
            }
        }
        return policySets;
    }

    public <D extends AbstractDefinition> D getDefinition(QName name, Class<D> definitionClass) {
        return getSubCache(definitionClass).get(name);
    }

    public void activateDefinitions(List<URI> contributionUris) throws PolicyActivationException {
        for (URI uri : contributionUris) {
            Contribution contribution = metaDataStore.find(uri);
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> resourceElement : resource.getResourceElements()) {
                    Object value = resourceElement.getValue();
                    if (value instanceof AbstractDefinition) {
                        activate((AbstractDefinition) value);
                    }
                }
            }
        }
    }

    public void activate(AbstractDefinition definition) throws PolicyActivationException {
        if (definition instanceof Intent) {
            getSubCache(Intent.class).put(definition.getName(), (Intent) definition);
        } else if (definition instanceof PolicySet) {
            getSubCache(PolicySet.class).put(definition.getName(), (PolicySet) definition);
        } else if (definition instanceof BindingType) {
            getSubCache(BindingType.class).put(definition.getName(), (BindingType) definition);
        } else if (definition instanceof ImplementationType) {
            getSubCache(ImplementationType.class).put(definition.getName(), (ImplementationType) definition);
        }
    }

    public void deactivateDefinitions(List<URI> contributionUris) throws PolicyActivationException {
        for (URI uri : contributionUris) {
            Contribution contribution = metaDataStore.find(uri);
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> resourceElement : resource.getResourceElements()) {
                    Object value = resourceElement.getValue();
                    if (value instanceof AbstractDefinition) {
                        deactivate((AbstractDefinition) value);
                    }
                }
            }
        }
    }

    public void deactivate(AbstractDefinition definition) throws PolicyActivationException {
        if (definition instanceof Intent) {
            getSubCache(Intent.class).remove(definition.getName());
        } else if (definition instanceof PolicySet) {
            getSubCache(PolicySet.class).remove(definition.getName());
        } else if (definition instanceof BindingType) {
            getSubCache(BindingType.class).remove(definition.getName());
        } else if (definition instanceof ImplementationType) {
            getSubCache(ImplementationType.class).remove(definition.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private <D extends AbstractDefinition> Map<QName, D> getSubCache(Class<D> definitionClass) {
        return (Map<QName, D>) cache.get(definitionClass);
    }

}
