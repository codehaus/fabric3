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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.host;

import java.net.URI;

/**
 * Defines URIs of well-known runtime components and contributions available through the host API.
 *
 * @version $Rev$ $Date$
 */
public interface Names {

    String VERSION = "1.7";

    URI BOOT_CONTRIBUTION = URI.create("fabric3-boot");

    URI HOST_CONTRIBUTION = URI.create("fabric3-host");

    String RUNTIME_NAME = "fabric3://runtime";

    URI RUNTIME_URI = URI.create(RUNTIME_NAME);

    URI APPLICATION_DOMAIN_URI = URI.create(RUNTIME_NAME + "/ApplicationDomain");

    URI CONTRIBUTION_SERVICE_URI = URI.create(RUNTIME_NAME + "/ContributionService");

    URI MONITOR_FACTORY_URI = URI.create(RUNTIME_NAME + "/MonitorProxyService");

    URI RUNTIME_DOMAIN_SERVICE_URI = URI.create(RUNTIME_NAME + "/RuntimeDomain");

    String RUNTIME_MONITOR_CHANNEL = "RuntimeMonitorChannel";

    String APPLICATION_MONITOR_CHANNEL = "ApplicationMonitorChannel";

    URI RUNTIME_MONITOR_CHANNEL_URI = URI.create(RUNTIME_NAME + "/" + RUNTIME_MONITOR_CHANNEL);

    URI APPLICATION_MONITOR_CHANNEL_URI = URI.create(RUNTIME_NAME + "/" + APPLICATION_MONITOR_CHANNEL);

}
