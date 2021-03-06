/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.ftp.control;

import java.net.URI;
import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Property;
import org.w3c.dom.Element;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.binding.ftp.provision.FtpSecurity;
import org.fabric3.binding.ftp.provision.FtpWireSourceDefinition;
import org.fabric3.binding.ftp.provision.FtpWireTargetDefinition;
import org.fabric3.binding.ftp.scdl.FtpBindingDefinition;
import org.fabric3.binding.ftp.scdl.TransferMode;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.policy.Policy;

/**
 * @version $Revision$ $Date$
 */
public class FtpBindingGenerator implements BindingGenerator<FtpBindingDefinition> {
    private int connectTimeout = 120000; // two minutes
    private int socketTimeout = 1800000;  // default timeout of 30 minutes

    /**
     * Optionally configures a timeout setting for openning a socket connection. The default wait is 2 minutes.
     *
     * @param connectTimeout the timeout in milliseconds
     */
    @Property
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Optionally configures a timeout setting for socket connections from the client to a server. The default is 30 minutes.
     *
     * @param socketTimeout the timeout in milliseconds
     */
    @Property
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public FtpWireSourceDefinition generateWireSource(LogicalBinding<FtpBindingDefinition> binding,
                                                      ServiceContract<?> contract,
                                                      List<LogicalOperation> operations,
                                                      Policy policy) throws GenerationException {

        if (contract.getOperations().size() != 1) {
            throw new GenerationException("Expects only one operation");
        }

        FtpWireSourceDefinition hwsd = new FtpWireSourceDefinition();
        URI targetUri = binding.getDefinition().getTargetUri();
        hwsd.setUri(targetUri);

        return hwsd;

    }

    public FtpWireTargetDefinition generateWireTarget(LogicalBinding<FtpBindingDefinition> binding,
                                                      ServiceContract<?> contract,
                                                      List<LogicalOperation> operations,
                                                      Policy policy) throws GenerationException {

        if (operations.size() != 1) {
            throw new GenerationException("Expects only one operation");
        }

        URI id = binding.getParent().getParent().getParent().getUri();
        FtpBindingDefinition definition = binding.getDefinition();
        boolean active = definition.getTransferMode() == TransferMode.ACTIVE;

        FtpSecurity security = processPolicies(policy, operations.get(0));

        FtpWireTargetDefinition hwtd = new FtpWireTargetDefinition(id, active, security, connectTimeout, socketTimeout);
        hwtd.setUri(definition.getTargetUri());
        if (!definition.getSTORCommands().isEmpty()) {
            hwtd.setSTORCommands(definition.getSTORCommands());
        }
        hwtd.setTmpFileSuffix(binding.getDefinition().getTmpFileSuffix());
        return hwtd;

    }

    private FtpSecurity processPolicies(Policy policy, LogicalOperation operation) throws GenerationException {

        List<PolicySet> policySets = policy.getProvidedPolicySets(operation);
        if (policySets == null || policySets.size() == 0) {
            return null;
        }
        if (policySets.size() != 1) {
            throw new GenerationException("Invalid policy configuration, only supports security policy");
        }

        PolicySet policySet = policySets.iterator().next();

        QName policyQName = policySet.getExtensionName();
        if (!policyQName.equals(Constants.POLICY_QNAME)) {
            throw new GenerationException("Unexpected policy element " + policyQName);
        }

        Element policyElement = policySet.getExtension();
        String user = policyElement.getAttribute("user");
        if (user == null) {
            throw new GenerationException("User name not specified in security policy");
        }
        String password = policyElement.getAttribute("password");
        if (password == null) {
            throw new GenerationException("Password not specified in security policy");
        }

        return new FtpSecurity(user, password);

    }

}
