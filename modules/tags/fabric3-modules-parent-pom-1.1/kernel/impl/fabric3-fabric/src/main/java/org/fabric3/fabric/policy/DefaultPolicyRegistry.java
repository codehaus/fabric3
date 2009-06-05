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
 * @version $Revision$ $Date$
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
