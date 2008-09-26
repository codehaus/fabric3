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
package org.fabric3.binding.ftp.runtime;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.ftp.provision.FtpWireSourceDefinition;
import org.fabric3.ftp.spi.FtpLetContainer;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.expression.ExpressionExpander;
import org.fabric3.spi.services.expression.ExpressionExpansionException;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
public class FtpSourceWireAttacher implements SourceWireAttacher<FtpWireSourceDefinition> {

    private FtpLetContainer ftpLetContainer;
    private ExpressionExpander expander;
    private BindingMonitor monitor;

    /**
     * Injects the references.
     *
     * @param ftpLetContainer FtpLet container.  The FtpLet container is optional. If it is not available, only reference bindings will be supported.
     * @param expander        the expander for '${..}' expressions.
     * @param monitor         the binding monitor for reporting events.
     */
    public FtpSourceWireAttacher(@Reference(required = false)FtpLetContainer ftpLetContainer,
                                 @Reference ExpressionExpander expander,
                                 @Monitor BindingMonitor monitor) {
        this.ftpLetContainer = ftpLetContainer;
        this.expander = expander;
        this.monitor = monitor;
    }

    public void attachToSource(FtpWireSourceDefinition source, PhysicalWireTargetDefinition target, final Wire wire) throws WiringException {
        URI uri = source.getUri();
        String servicePath = expandUri(uri).getSchemeSpecificPart();
        if (servicePath.startsWith("//")) {
            servicePath = servicePath.substring(2);
        }
        BindingFtpLet bindingFtpLet = new BindingFtpLet(servicePath, wire, monitor);
        if (ftpLetContainer == null) {
            throw new WiringException(
                    "An FTP server was not configured for this runtime. Ensure the FTP server extension is installed and configured properly.");
        }
        ftpLetContainer.registerFtpLet(servicePath, bindingFtpLet);

    }

    public void detachFromSource(FtpWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void attachObjectFactory(FtpWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition definition)
            throws WiringException {
        throw new UnsupportedOperationException();
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
