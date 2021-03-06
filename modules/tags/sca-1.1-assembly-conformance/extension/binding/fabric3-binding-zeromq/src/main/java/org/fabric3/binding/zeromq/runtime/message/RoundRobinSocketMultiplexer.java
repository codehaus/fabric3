/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.message;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import org.zeromq.ZMQ;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.SocketAddress;

/**
 * Implements a round-robin strategy for selecting an available socket from a collection of sockets.
 *
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class RoundRobinSocketMultiplexer implements SocketMultiplexer {
    private ZMQ.Context context;
    private int socketType;

    private Map<SocketAddress, ZMQ.Socket> sockets;
    private Iterator<ZMQ.Socket> iterator;
    private ZeroMQMetadata metadata;

    public RoundRobinSocketMultiplexer(ZMQ.Context context, int socketType, ZeroMQMetadata metadata) {
        this.context = context;
        this.socketType = socketType;
        this.metadata = metadata;
        sockets = new ConcurrentLinkedHashMap.Builder<SocketAddress, ZMQ.Socket>().maximumWeightedCapacity(1000).build();
    }

    public synchronized void update(List<SocketAddress> addresses) {
        if (sockets.isEmpty()) {
            if (addresses.size() == 1) {
                ZMQ.Socket socket = context.socket(socketType);
                SocketHelper.configure(socket, metadata);
                SocketAddress address = addresses.get(0);
                address.getPort().releaseLock();
                socket.connect(address.toProtocolString());
                sockets.put(address, socket);
                iterator = new SingletonIterator(socket);
            } else {
                for (SocketAddress address : addresses) {
                    ZMQ.Socket socket = context.socket(socketType);
                    SocketHelper.configure(socket, metadata);
                    address.getPort().releaseLock();
                    socket.connect(address.toProtocolString());
                    sockets.put(address, socket);
                }
                iterator = sockets.values().iterator();
            }
        } else {
            Set<SocketAddress> intersection = new HashSet<SocketAddress>(addresses);
            intersection.retainAll(sockets.keySet());

            Set<SocketAddress> toClose = new HashSet<SocketAddress>(sockets.keySet());
            toClose.removeAll(addresses);

            Set<SocketAddress> toAdd = new HashSet<SocketAddress>(addresses);
            toAdd.removeAll(sockets.keySet());

            for (SocketAddress address : toClose) {
                sockets.remove(address).close();
            }

            for (SocketAddress address : toAdd) {
                ZMQ.Socket socket = context.socket(socketType);
                SocketHelper.configure(socket, metadata);
                address.getPort().releaseLock();
                socket.connect(address.toProtocolString());
                sockets.put(address, socket);
            }

            if (sockets.size() == 1) {
                iterator = new SingletonIterator(sockets.values().iterator().next());
            } else {
                iterator = sockets.values().iterator();
            }
        }
    }

    public ZMQ.Socket get() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return iterator.next();
    }

    public Collection<ZMQ.Socket> getAll() {
        return sockets.values();
    }

    public boolean isAvailable() {
        return !sockets.isEmpty();
    }

    public void close() {
        for (ZMQ.Socket socket : sockets.values()) {
            socket.close();
        }
    }

    private boolean hasNext() {
        if (!iterator.hasNext()) {
            // return to top of list
            iterator = sockets.values().iterator();
        }
        return iterator.hasNext();
    }

    private class SingletonIterator implements Iterator<ZMQ.Socket> {
        private ZMQ.Socket socket;

        private SingletonIterator(ZMQ.Socket socket) {
            this.socket = socket;
        }

        public boolean hasNext() {
            return true;
        }

        public ZMQ.Socket next() {
            return socket;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


}
