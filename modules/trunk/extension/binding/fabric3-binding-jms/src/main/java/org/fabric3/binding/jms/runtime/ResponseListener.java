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
package org.fabric3.binding.jms.runtime;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.jms.runtime.helper.JmsHelper;

/**
 * MessageListener that blocks for responses from a service provider. This listener is attached to the reference side of a wire.
 *
 * @version $Revison$ $Date$
 */
public class ResponseListener {
    private Destination destination;
    private ConnectionFactory connectionFactory;
    private TransactionManager tm;

    /**
     * @param destination       the response destination
     * @param connectionFactory the response connection factory
     * @param tm                the JTA transaction manager
     */
    public ResponseListener(Destination destination, ConnectionFactory connectionFactory, TransactionManager tm) {
        this.destination = destination;
        this.connectionFactory = connectionFactory;
        this.tm = tm;
    }

    /**
     * Performs a blocking receive, i.e. control will not be returned to application code until a response is received.
     *
     * @param correlationId Correlation Id.
     * @return Received message.
     */
    public Message receive(String correlationId) {
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();  // ensure the connection is started
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            String selector = "JMSCorrelationID = '" + correlationId + "'";
            MessageConsumer consumer = session.createConsumer(destination, selector);
            if (Status.STATUS_NO_TRANSACTION == tm.getStatus()) {
                tm.begin();
            }
            Message message = consumer.receive();
            tm.commit();
            return message;
        } catch (JMSException e) {
            // bubble exception to the client
            throw new ServiceRuntimeException("Unable to receive response for message with correlation id: " + correlationId, e);
        } catch (SystemException e) {
            throw new ServiceRuntimeException("Unable to receive response for message with correlation id: " + correlationId, e);
        } catch (NotSupportedException e) {
            throw new ServiceRuntimeException("Unable to receive response for message with correlation id: " + correlationId, e);
        } catch (HeuristicMixedException e) {
            //FIXME
            throw new ServiceRuntimeException("Unable to receive response for message with correlation id: " + correlationId, e);
        } catch (HeuristicRollbackException e) {
            //FIXME
            throw new ServiceRuntimeException("Unable to receive response for message with correlation id: " + correlationId, e);
        } catch (RollbackException e) {
            //FIXME
            throw new ServiceRuntimeException("Unable to receive response for message with correlation id: " + correlationId, e);
        } finally {
            JmsHelper.closeQuietly(session);
            JmsHelper.closeQuietly(connection);
        }

    }

}
