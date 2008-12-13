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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import static org.fabric3.loader.plan.DeploymentPlanConstants.PLAN;
import static org.fabric3.loader.plan.DeploymentPlanConstants.PLAN_NAMESPACE;
import org.fabric3.model.type.ValidationContext;
import org.fabric3.spi.plan.DeploymentPlan;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.xml.XmlIndexer;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;

/**
 * Indexes a deployment plan.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class DeploymentPlanIndexer implements XmlIndexer {

    private XmlIndexerRegistry registry;


    public DeploymentPlanIndexer(@Reference XmlIndexerRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public QName getType() {
        return PLAN;
    }

    public void index(Resource resource, XMLStreamReader reader, ValidationContext context) throws InstallException {
        QName qname = reader.getName();
        assert PLAN.equals(qname);
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            context.addError(new MissingAttribute("Deployment plan name not specified", "name", reader));
            return;
        }
        QName planQName = new QName(PLAN_NAMESPACE, name);
        QNameSymbol symbol = new QNameSymbol(planQName);
        ResourceElement<QNameSymbol, DeploymentPlan> element = new ResourceElement<QNameSymbol, DeploymentPlan>(symbol);
        resource.addResourceElement(element);
    }

}