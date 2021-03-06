/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.api;

import java.io.Serializable;

import org.fabric3.api.annotation.monitor.MonitorLevel;

/**
 * Encapsulates monitor data that will be broadcast as an event.
 *
 * @version $Rev$ $Date$
 */
public interface MonitorEvent extends Serializable {

    /**
     * Returns the runtime which originated the event.
     *
     * @return the runtime which originated the event
     */
    public String getRuntime();

    /**
     * Returns the name of the object (such as a component) which originated the event.
     *
     * @return the Monitorable name
     */
    public String getSource();

    /**
     * Returns the event level.
     *
     * @return the event level
     */
    public MonitorLevel getMonitorLevel();

    /**
     * Returns the time the event was created.
     *
     * @return the time the event was created
     */
    public long getTimeStamp();

    /**
     * Returns the name of the executing thread when the event was created.
     *
     * @return the name of the executing thread when the event was created
     */
    public String getThreadName();

    /**
     * Returns the event message.
     *
     * @return the event message
     */
    public String getMessage();

    /**
     * Returns any data (such as an exception) associated with the event.
     *
     * @return any data associated with the event
     */
    public Object[] getData();
}