/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.management.rest.runtime;

import java.io.Serializable;

import org.fabric3.management.rest.spi.Verb;

/**
 * Used to replicate resource requests to participants in a zone.
 *
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class ReplicationEnvelope implements Serializable {
    private static final long serialVersionUID = -7548186506338136783L;
    private String path;
    private Verb verb;
    private Object[] params;

    /**
     * Constructor.
     *
     * @param path   the request path
     * @param verb   the HTTP request verb
     * @param params the request params
     */
    public ReplicationEnvelope(String path, Verb verb, Object[] params) {
        this.path = path;
        this.verb = verb;
        this.params = params;
    }

    public String getPath() {
        return path;
    }

    public Verb getVerb() {
        return verb;
    }

    public Object[] getParams() {
        return params;
    }
}
