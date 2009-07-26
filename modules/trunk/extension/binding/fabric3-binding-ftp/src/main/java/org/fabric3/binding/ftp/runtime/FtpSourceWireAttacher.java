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
package org.fabric3.binding.ftp.runtime;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.ftp.provision.FtpSourceDefinition;
import org.fabric3.ftp.spi.FtpLetContainer;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.expression.ExpressionExpander;
import org.fabric3.spi.expression.ExpressionExpansionException;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class FtpSourceWireAttacher implements SourceWireAttacher<FtpSourceDefinition> {

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
    public FtpSourceWireAttacher(@Reference(required = false) FtpLetContainer ftpLetContainer,
                                 @Reference ExpressionExpander expander,
                                 @Monitor BindingMonitor monitor) {
        this.ftpLetContainer = ftpLetContainer;
        this.expander = expander;
        this.monitor = monitor;
    }

    public void attachToSource(FtpSourceDefinition source, PhysicalTargetDefinition target, final Wire wire) throws WiringException {
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

    public void detachFromSource(FtpSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detachObjectFactory(FtpSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void attachObjectFactory(FtpSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalTargetDefinition definition)
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
