package org.fabric3.fabric.synthesizer;

import java.net.URI;
import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import static org.fabric3.host.Namespaces.IMPLEMENTATION;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.Include;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.synthesize.CompositeSynthesizer;

/**
 * Default implementation of CompositeSynthesizer.
 *
 * @version $Revision$ $Date$
 */
public class CompositeSynthesizerImpl implements CompositeSynthesizer {
    private MetaDataStore metaDataStore;
    private HostInfo info;

    public CompositeSynthesizerImpl(@Reference MetaDataStore metaDataStore, @Reference HostInfo info) {
        this.metaDataStore = metaDataStore;
        this.info = info;
    }

    public Composite createComposite(List<URI> contributionUris) {
        QName qName = new QName(IMPLEMENTATION, "synthetic");
        org.fabric3.model.type.component.Composite composite = new org.fabric3.model.type.component.Composite(qName);
        for (URI uri : contributionUris) {
            Contribution contribution = metaDataStore.find(uri);
            assert contribution != null;

            RuntimeMode runtimeMode = info.getRuntimeMode();
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> entry : resource.getResourceElements()) {

                    if (!(entry.getValue() instanceof org.fabric3.model.type.component.Composite)) {
                        continue;
                    }
                    @SuppressWarnings({"unchecked"})
                    ResourceElement<QNameSymbol, org.fabric3.model.type.component.Composite> element =
                            (ResourceElement<QNameSymbol, org.fabric3.model.type.component.Composite>) entry;
                    QName name = element.getSymbol().getKey();
                    org.fabric3.model.type.component.Composite childComposite = element.getValue();
                    for (Deployable deployable : contribution.getManifest().getDeployables()) {
                        List<RuntimeMode> deployableModes = deployable.getRuntimeModes();
                        if (deployable.getName().equals(name)) {
                            if (!deployableModes.contains(runtimeMode)) {
                                // do not include the contribution as the runtime is booted in a different mode than what it is configured for
                                break;
                            }
                            Include include = new Include();
                            include.setName(name);
                            include.setIncluded(childComposite);
                            composite.add(include);
                            break;
                        }
                    }
                }
            }
        }
        return composite;
    }
}
