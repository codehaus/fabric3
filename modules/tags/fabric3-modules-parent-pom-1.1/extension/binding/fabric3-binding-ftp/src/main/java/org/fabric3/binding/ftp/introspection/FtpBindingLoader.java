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
package org.fabric3.binding.ftp.introspection;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ftp.scdl.FtpBindingDefinition;
import org.fabric3.binding.ftp.scdl.TransferMode;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;

/**
 * @version $Revision$ $Date$
 */
public class FtpBindingLoader implements TypeLoader<FtpBindingDefinition> {

    private final LoaderHelper loaderHelper;

    /**
     * Constructor.
     *
     * @param loaderHelper the policy helper
     */
    public FtpBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public FtpBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        validateAttributes(reader, introspectionContext);

        FtpBindingDefinition bd = null;
        String uri = null;

        try {

            uri = reader.getAttributeValue(null, "uri");
            String transferMode = reader.getAttributeValue(null, "mode");
            if (uri == null) {
                MissingAttribute failure = new MissingAttribute("A binding URI must be specified ", reader);
                introspectionContext.addError(failure);
                return null;
            }
            if (!uri.startsWith("ftp://") && !uri.startsWith("FTP://")) {
                uri = "ftp://" + uri;
            }
            TransferMode tMode = transferMode != null ? TransferMode.valueOf(transferMode) : TransferMode.PASSIVE;
            // encode the URI since there may be expressions (e.g. "${..}") contained in it
            URI endpointUri = new URI(URLEncoder.encode(uri, "UTF-8"));
            bd = new FtpBindingDefinition(endpointUri, tMode, loaderHelper.loadKey(reader));
            
            String tmpFileSuffix = reader.getAttributeValue(null, "tmpFileSuffix");
            if(tmpFileSuffix != null) {
                bd.setTmpFileSuffix(tmpFileSuffix);
            }

            loaderHelper.loadPolicySetsAndIntents(bd, reader, introspectionContext);
            while (true) {
                switch (reader.next()) {
                case XMLStreamConstants.END_ELEMENT:
                    if ("binding.ftp".equals(reader.getName().getLocalPart())) {
                        return bd;
                    }
                case XMLStreamConstants.START_ELEMENT:
                    if ("commands".equals(reader.getName().getLocalPart())) {
                        boolean success = parseCommands(bd, reader, introspectionContext);
                        if (!success) {
                            while (true) {
                                // position the curser at the end of the binding.ftp entry
                                LoaderUtil.skipToEndElement(reader);
                                if ("binding.ftp".equals(reader.getName().getLocalPart())) {
                                    return bd;
                                }
                            }
                        }
                    }
                }

            }

        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The FTP binding URI is not valid: " + uri, reader);
            introspectionContext.addError(failure);
        } catch (UnsupportedEncodingException e) {
            InvalidValue failure = new InvalidValue("Invalid encoding for URI: " + uri + "\n" + e, reader);
            introspectionContext.addError(failure);
        }

        LoaderUtil.skipToEndElement(reader);
        return bd;

    }

    private boolean parseCommands(FtpBindingDefinition bd, XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        while (true) {
            switch (reader.nextTag()) {
            case XMLStreamConstants.END_ELEMENT:
                if ("commands".equals(reader.getName().getLocalPart())) {
                    return true;
                }
                break;
            case XMLStreamConstants.START_ELEMENT:
                if ("command".equals(reader.getName().getLocalPart())) {
                    reader.next();
                    bd.addSTORCommand(reader.getText());
                } else {
                    UnrecognizedElement error = new UnrecognizedElement(reader);
                    context.addError(error);
                    return false;
                }
            }
        }
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"uri".equals(name) && !"requires".equals(name) && !"policySets".equals(name) && !"mode".equals(name) && 
                !"tmpFileSuffix".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
