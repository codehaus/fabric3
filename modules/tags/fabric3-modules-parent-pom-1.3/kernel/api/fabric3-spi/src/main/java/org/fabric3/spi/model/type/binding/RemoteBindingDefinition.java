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
package org.fabric3.spi.model.type.binding;

import javax.xml.namespace.QName;

import org.fabric3.host.Namespaces;
import org.fabric3.model.type.component.BindingDefinition;

/**
 * Represents binding information for the source or target side of a bound wire. For bound services, the remote binding definition is used to
 * represent the side of the wire that is attached to to the component providing the service. For bound references, the remote binding definition is
 * used to represent the side of the wire that is attached to the component containing the reference.
 *
 * @version $Rev$ $Date$
 */
public class RemoteBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = 975283470994901368L;
    private static final QName QNAME = new QName(Namespaces.BINDING, "binding.remote");

    public static final RemoteBindingDefinition INSTANCE = new RemoteBindingDefinition();

    public RemoteBindingDefinition() {
        super(null, QNAME);
    }
}
