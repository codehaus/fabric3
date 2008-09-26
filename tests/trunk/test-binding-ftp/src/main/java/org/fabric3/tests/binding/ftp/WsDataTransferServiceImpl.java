/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ÒLicenseÓ), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an Òas isÓ basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.tests.binding.ftp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.activation.DataHandler;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;

/**
 *
 * @version $Revision$ $Date$
 */
public class WsDataTransferServiceImpl implements WsDataTransferService {

    public OMElement transferData(OMElement message) throws Exception {
        
        OMElement fileNameElement = (OMElement) message.getFirstOMChild();
        String fileName = fileNameElement.getText();
        System.err.println("File name from web service:" + fileName);
        
        OMElement dataElement = (OMElement) fileNameElement.getNextOMSibling();
        OMText data = (OMText) dataElement.getFirstOMChild();
        data.setOptimize(true);
        DataHandler dataHandler = (DataHandler) data.getDataHandler();
        InputStream is = dataHandler.getInputStream();
        InputStreamReader reader = new InputStreamReader(is);
        char buffer[] = new char[1024];
        StringWriter writer = new StringWriter();
        for (int count; (count = reader.read(buffer, 0, buffer.length)) > 0;) {
            writer.write(buffer, 0, count);
        }
        System.err.println("File data from web service:" + writer.toString());
        return fileNameElement;
    }

}
