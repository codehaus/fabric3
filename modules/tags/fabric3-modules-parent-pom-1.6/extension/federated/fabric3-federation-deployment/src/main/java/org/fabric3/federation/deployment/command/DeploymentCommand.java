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
package org.fabric3.federation.deployment.command;

import org.fabric3.spi.command.ResponseCommand;
import org.fabric3.spi.command.Response;

/**
 * Broadcast by the controller to to perform a deployment to all participants in a zone. The current deployment is incremental from the previous
 * deployment. The full deployment contains the complete list of commands required to update a participant runtime to the current zone state. The
 * latter is cached by participants which can be used to bootstrap zone peers without the need to contact the controller.
 *
 * @version $Rev$ $Date$
 */
public class DeploymentCommand implements ResponseCommand, Response {
    private static final long serialVersionUID = 8673100303949676875L;

    private String zone;
    private SerializedDeploymentUnit currentDeploymentUnit;
    private SerializedDeploymentUnit fullDeploymentUnit;
    private Response response;
    private String runtimeName;

    public DeploymentCommand(String zone, SerializedDeploymentUnit currentDeploymentUnit, SerializedDeploymentUnit fullDeploymentUnit) {
        this.zone = zone;
        this.currentDeploymentUnit = currentDeploymentUnit;
        this.fullDeploymentUnit = fullDeploymentUnit;
    }

    public String getZone() {
        return zone;
    }

    public SerializedDeploymentUnit getCurrentDeploymentUnit() {
        return currentDeploymentUnit;
    }

    public SerializedDeploymentUnit getFullDeploymentUnit() {
        return fullDeploymentUnit;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public void setRuntimeName(String runtimeName) {
        this.runtimeName = runtimeName;
    }
}
