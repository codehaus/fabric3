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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.federation.deployment.domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.SerializedDeploymentUnit;
import org.fabric3.federation.deployment.spi.FederatedDeployerListener;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.DeployerMonitor;
import org.fabric3.spi.domain.DeploymentPackage;
import org.fabric3.spi.federation.DomainTopologyService;
import org.fabric3.spi.federation.ErrorResponse;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.Response;
import org.fabric3.spi.generator.Deployment;
import org.fabric3.spi.generator.DeploymentUnit;

/**
 * A Deployer that deploys the contents of a {@link DeploymentPackage} to a set of zones in a distributed domain using commit/rollback semantics.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class FederatedDeployer implements Deployer {
    private DeployerMonitor monitor;
    private DomainTopologyService topologyService;
    private SerializationService serializationService;
    private List<FederatedDeployerListener> listeners;
    private long timeout = 60000;

    public FederatedDeployer(@Reference DomainTopologyService topologyService,
                             @Reference SerializationService serializationService,
                             @Monitor DeployerMonitor monitor) {
        this.topologyService = topologyService;
        this.serializationService = serializationService;
        this.monitor = monitor;
        this.listeners = new ArrayList<FederatedDeployerListener>();
    }

    @Reference(required = false)
    public void setListeners(List<FederatedDeployerListener> listeners) {
        this.listeners = listeners;
    }

    @Property(required = false)
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public synchronized void deploy(DeploymentPackage deploymentPackage) throws DeploymentException {
        Deployment currentDeployment = deploymentPackage.getCurrentDeployment();
        Deployment fullDeployment = deploymentPackage.getFullDeployment();

        // tracks deployment responses by zone
        List<DeploymentCommand> completed = new ArrayList<DeploymentCommand>();
        for (String zone : currentDeployment.getZones()) {
            monitor.deploy(zone);
            DeploymentCommand command;
            try {
                command = createCommand(zone, currentDeployment, fullDeployment);
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
            notifyDeploy(command);
            List<Response> responses;
            try {
                // send deployments in a fail-fast manner
                responses = topologyService.sendSynchronousToZone(zone, command, true, timeout);
            } catch (MessageException e) {
                // rollback(zone, currentDeployment, completed, zoneResponses);
                throw new DeploymentException(e);
            }

            if (responses.isEmpty()) {
                throw new DeploymentException("Deployment responses not received");
            }
            completed.add(command);
            Response last = responses.get(responses.size() - 1);

            String runtimeName = last.getRuntimeName();
            if (last instanceof ErrorResponse) {
                ErrorResponse response = (ErrorResponse) last;
                monitor.deploymentError(runtimeName, response.getException());
                // rollback(zone, currentDeployment, completed, zoneResponses);
                throw new DeploymentException("Deployment errors encountered and logged");
            }
        }
        for (DeploymentCommand command : completed) {
            // TODO a commit needs to be refactored to a separate coordinator
            notifyCompletion(command);
        }
    }

    private DeploymentCommand createCommand(String zone, Deployment currentDeployment, Deployment fullDeployment) throws IOException {
        DeploymentUnit currentDeploymentUnit = currentDeployment.getDeploymentUnit(zone);
        SerializedDeploymentUnit currentSerializedUnit = createSerializedUnit(currentDeploymentUnit);
        DeploymentUnit fullDeploymentUnit = fullDeployment.getDeploymentUnit(zone);
        SerializedDeploymentUnit fullSerializedUnit = createSerializedUnit(fullDeploymentUnit);
        return new DeploymentCommand(zone, currentSerializedUnit, fullSerializedUnit);
    }

    private SerializedDeploymentUnit createSerializedUnit(DeploymentUnit deploymentUnit) throws IOException {
        List<CompensatableCommand> extensionCommands = deploymentUnit.getExtensionCommands();
        byte[] serializedExtensionCommands = serializationService.serialize((Serializable) extensionCommands);
        List<CompensatableCommand> commands = deploymentUnit.getCommands();
        byte[] serializedCommands = serializationService.serialize((Serializable) commands);
        return new SerializedDeploymentUnit(serializedExtensionCommands, serializedCommands);
    }

    private void notifyDeploy(DeploymentCommand command) throws DeploymentException {
        for (FederatedDeployerListener listener : listeners) {
            listener.onDeploy(command);
        }
    }

    private void notifyCompletion(DeploymentCommand command) {
        for (FederatedDeployerListener listener : listeners) {
            listener.onCompletion(command);
        }
    }


}
