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
package org.fabric3.binding.ftp.runtime;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.UnknownHostException;

import org.apache.commons.net.SocketFactory;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ftp.provision.FtpSecurity;
import org.fabric3.binding.ftp.provision.FtpWireTargetDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.services.expression.ExpressionExpander;
import org.fabric3.spi.services.expression.ExpressionExpansionException;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
public class FtpTargetWireAttacher implements TargetWireAttacher<FtpWireTargetDefinition> {
    private ExpressionExpander expander;

    public FtpTargetWireAttacher(@Reference ExpressionExpander expander) {
        this.expander = expander;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, FtpWireTargetDefinition target, Wire wire) throws WiringException {

        InvocationChain invocationChain = wire.getInvocationChains().values().iterator().next();
        URI uri = expandUri(target.getUri());
        try {
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 23 : uri.getPort();
            InetAddress hostAddress = "localhost".equals(host) ? InetAddress.getLocalHost() : InetAddress.getByName(host);

            FtpSecurity security = target.getSecurity();
            boolean active = target.isActive();
            int connectTimeout = target.getConectTimeout();
            SocketFactory factory = new ExpiringSocketFactory(connectTimeout);
            int socketTimeout = target.getSocketTimeout();
            FtpTargetInterceptor targetInterceptor = new FtpTargetInterceptor(hostAddress, port, security, active, socketTimeout, factory);
            invocationChain.addInterceptor(targetInterceptor);
        } catch (UnknownHostException e) {
            throw new WiringException(e);
        }

    }

    public ObjectFactory<?> createObjectFactory(FtpWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    /**
     * Expands the target URI if it contains an expression of the form ${..}.
     *
     * @param uri the target uri to expand
     * @return the expanded URI with sourced values for any expressions
     * @throws WiringException if there is an error expanding an expression
     */
    private URI expandUri(URI uri) throws WiringException {
        try {
            String decoded = URLDecoder.decode(uri.toString(), "UTF-8");
            return URI.create(expander.expand(decoded));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        } catch (ExpressionExpansionException e) {
            throw new WiringException(e);
        }
    }

}
