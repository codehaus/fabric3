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
package org.fabric3.binding.ws.metro.runtime.policy;

import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;

import com.sun.xml.ws.developer.BindingTypeFeature;
import com.sun.xml.ws.developer.JAXWSProperties;

/**
 * Default implementation of FeatureResolver.�
 *
 * @version $Rev$ $Date$
 */
public class DefaultFeatureResolver implements FeatureResolver {

    /**
     * Translates the requested intents to web service features.
     *
     * @param requestedIntents    Requested intents.
     * @return Rsolved feature sets.
     */
    public WebServiceFeature[] getFeatures(List<QName> requestedIntents) {

        List<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
        if (requestedIntents.contains(MayProvidedIntents.MESSAGE_OPTIMISATION)) {
            features.add(new MTOMFeature());
        }
        if (requestedIntents.contains(MayProvidedIntents.PROTOCOL_REST)) {
            features.add(new BindingTypeFeature(JAXWSProperties.REST_BINDING));
        }

        features.add(new AddressingFeature());
        WebServiceFeature[] webServiceFeatures = new WebServiceFeature[features.size()];
        webServiceFeatures = features.toArray(webServiceFeatures);

        return webServiceFeatures;

    }

}
