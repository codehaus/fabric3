package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.stream.Source;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 * Processes composite files deployed as a contribution.
 * <p/>
 * During processing, a synthetic deployable composite is created and added to the contribution. In addition, a synthetic QName export equal to the
 * composite target namespace is created and added to the contribution, which can be used for contribution ordering durng deployment.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class CompositeContributionProcessor implements ContributionProcessor {
    private LoaderRegistry loaderRegistry;
    private ProcessorRegistry processorRegistry;

    public CompositeContributionProcessor(@Reference LoaderRegistry loaderRegistry, @Reference ProcessorRegistry processorRegistry) {
        this.loaderRegistry = loaderRegistry;
        this.processorRegistry = processorRegistry;
    }

    @Init
    public void start() {
        processorRegistry.register(this);
    }

    @Destroy
    public void stop() {
        processorRegistry.unregister(this);
    }

    public boolean canProcess(Contribution contribution) {
        String sourceUrl = contribution.getLocation().toString();
        return sourceUrl.endsWith(".composite");
    }

    public void processManifest(Contribution contribution, IntrospectionContext context) throws InstallException {
        // no-op
    }

    public void index(Contribution contribution, IntrospectionContext context) throws InstallException {
        // no-op
    }

    public void process(Contribution contribution, IntrospectionContext context) throws InstallException {
        try {
            Source source = contribution.getSource();
            Composite composite = loaderRegistry.load(source, Composite.class, context);
            QName name = composite.getName();

            Resource resource = new Resource(contribution, source, "application/xml");
            QNameSymbol symbol = new QNameSymbol(name);
            ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
            element.setValue(composite);
            resource.addResourceElement(element);
            resource.setState(ResourceState.PROCESSED);

            contribution.addResource(resource);

            ContributionManifest manifest = contribution.getManifest();
            Deployable deployable = new Deployable(name);
            manifest.addDeployable(deployable);

            QNameExport export = new QNameExport(name.getNamespaceURI());
            manifest.addExport(export);
        } catch (LoaderException e) {
            throw new InstallException(e);
        }
    }
}
