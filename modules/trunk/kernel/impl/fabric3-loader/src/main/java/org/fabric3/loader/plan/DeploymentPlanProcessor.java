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
package org.fabric3.loader.plan;

import java.net.URI;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.InstallException;
import static org.fabric3.loader.plan.DeploymentPlanConstants.PLAN;
import static org.fabric3.loader.plan.DeploymentPlanConstants.PLAN_NAMESPACE;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoader;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoaderRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.plan.DeploymentPlan;

/**
 * Processes a deployment plan.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DeploymentPlanProcessor implements XmlResourceElementLoader {
    private static final QName DEPLOYABLE_MAPPING = new QName(Namespaces.CORE, "mapping");

    private XmlResourceElementLoaderRegistry registry;

    public DeploymentPlanProcessor(@Reference XmlResourceElementLoaderRegistry registry) {
        this.registry = registry;
    }

    public QName getType() {
        return PLAN;
    }


    @Init
    public void init() {
        registry.register(this);
    }

    public void load(XMLStreamReader reader, URI contributionUri, Resource resource, IntrospectionContext context, ClassLoader loader)
            throws InstallException {
        try {
            QName qname = reader.getName();
            assert PLAN.equals(qname);
            String planName = reader.getAttributeValue(null, "name");
            if (planName == null) {
                // this won't happen as it is checked in the indexer
                context.addError(new MissingAttribute("Deployment plan name not specified", reader));
                return;
            }
            DeploymentPlan plan = new DeploymentPlan(planName);
            while (true) {
                switch (reader.next()) {
                case START_ELEMENT:
                    qname = reader.getName();
                    if (DEPLOYABLE_MAPPING.equals(qname)) {
                        if (!processDeployableMapping(plan, reader, context)) {
                            return;
                        }
                    }
                    break;
                case END_ELEMENT:
                    QName name = reader.getName();
                    if (PLAN.equals(name)) {
                        updatePlan(resource, plan);
                        return;
                    }
                    // update indexed elements with the loaded definitions
                }
            }
        } catch (XMLStreamException e) {
            throw new InstallException("Error processing contribution: " + contributionUri, e);
        }


    }

    /**
     * Parses a deployable mapping
     *
     * @param plan    the deployment plan
     * @param reader  the XML reader
     * @param context the validation context
     * @return true if the mapping was processed successfully, false if there was a validation error
     */
    private boolean processDeployableMapping(DeploymentPlan plan, XMLStreamReader reader, IntrospectionContext context) {
        String deployableName = reader.getAttributeValue(null, "deployable");
        if (deployableName == null) {
            context.addError(new MissingAttribute("Deployable name not specified in mapping", reader));
            return false;
        }
        QName deployable = LoaderUtil.getQName(deployableName, null, reader.getNamespaceContext());
        String zoneName = reader.getAttributeValue(null, "zone");
        if (zoneName == null) {
            context.addError(new MissingAttribute("Zone not specified in mapping", reader));
            return false;
        }
        plan.addDeployableMapping(deployable, zoneName);
        return true;
    }

    /**
     * Updates the deployment plan ResourceElement with the parsed DeploymentPlan.
     *
     * @param resource the plan resource to update
     * @param plan     the deployment plan
     */
    private void updatePlan(Resource resource, DeploymentPlan plan) {
        String name = plan.getName();
        QName planQName = new QName(PLAN_NAMESPACE, name);
        QNameSymbol symbol = new QNameSymbol(planQName);
        boolean found = false;
        for (ResourceElement element : resource.getResourceElements()) {
            if (element.getSymbol().equals(symbol)) {
                element.setValue(plan);
                found = true;
                break;
            }
        }
        if (!found) {
            // this is a programming error if this happens as the indexer did not set the resource element properly
            throw new AssertionError("Deployment plan not found: " + name);
        }
        resource.setProcessed(true);
    }


}