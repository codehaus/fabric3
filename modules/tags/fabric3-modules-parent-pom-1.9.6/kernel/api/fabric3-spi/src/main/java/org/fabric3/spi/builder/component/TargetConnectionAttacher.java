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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.builder.component;

import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;

/**
 * Attaches and detaches a pub/sub connection to a channel, component consumer, or channel binding.
 *
 * @version $Rev: 8702 $ $Date: 2010-03-15 09:43:42 +0100 (Mon, 15 Mar 2010) $
 */
public interface TargetConnectionAttacher<PCTD extends PhysicalConnectionTargetDefinition> {

    /**
     * Attach a connection to a target, which can be a channel, component consumer, or channel binding.
     *
     * @param source     the source metadata
     * @param target     the target metadata
     * @param connection the connection that flows events from a source
     * @throws ConnectionAttachException if an error is encountered performing the attach
     */
    void attach(PhysicalConnectionSourceDefinition source, PCTD target, ChannelConnection connection) throws ConnectionAttachException;

    /**
     * Detach a connection from a target.
     *
     * @param source the source metadata
     * @param target the target metadata
     * @throws ConnectionAttachException if an error is encountered performing the attach
     */
    void detach(PhysicalConnectionSourceDefinition source, PCTD target) throws ConnectionAttachException;

}