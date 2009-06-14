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
package org.fabric3.tests.binding.ftp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import javax.activation.DataHandler;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;

/**
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
