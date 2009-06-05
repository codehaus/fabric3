/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.generator.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.ConnectionCommand;
import org.fabric3.fabric.command.StartContextCommand;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.util.graph.DirectedGraph;
import org.fabric3.util.graph.DirectedGraphImpl;
import org.fabric3.util.graph.Edge;
import org.fabric3.util.graph.EdgeImpl;
import org.fabric3.util.graph.GraphException;
import org.fabric3.util.graph.TopologicalSorter;
import org.fabric3.util.graph.TopologicalSorterImpl;
import org.fabric3.util.graph.Vertex;
import org.fabric3.util.graph.VertexImpl;

/**
 * Generates commands to start deployable contexts in a zone. The start commands will be ordered based on a reverse topological sort of collocated
 * wiring dependencies. For example, if component A' in deployable A is wired to component B' in deployable B, and the wire is local (i.e. not via a
 * binding), the command to start deployable context B is ordered before that to start context A.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class StartContextCommandGeneratorImpl implements StartContextCommandGenerator {
    private TopologicalSorter<QName> sorter;

    public StartContextCommandGeneratorImpl() {
        sorter = new TopologicalSorterImpl<QName>();
    }

    public Map<String, List<Command>> generate(List<LogicalComponent<?>> components, CommandMap map, boolean incremental) throws GenerationException {
        Map<String, List<Command>> commands = new HashMap<String, List<Command>>();
        List<QName> deployables = new ArrayList<QName>();
        for (LogicalComponent<?> component : components) {
            if (component.getState() == LogicalState.NEW || !incremental) {
                StartContextCommand command = new StartContextCommand(component.getDeployable());
                List<Command> list = getCommands(component.getZone(), commands);
                if (!list.contains(command)) {
                    list.add(command);
                }
                deployables.add(component.getDeployable());
            }
        }
        try {
            sort(commands, map, deployables);
        } catch (GraphException e) {
            throw new GenerationException(e);
        }
        return commands;

    }

    /**
     * Returns the list of commands by zone, creating one if necessary.
     *
     * @param zone     the zone
     * @param commands the list of commands maped by zone
     * @return the list of commands for a zone
     */
    private List<Command> getCommands(String zone, Map<String, List<Command>> commands) {
        List<Command> list = commands.get(zone);
        if (list == null) {
            list = new ArrayList<Command>();
            commands.put(zone, list);
        }
        return list;
    }

    /**
     * Sorts the start context commands by the order of the deployable contexts.
     *
     * @param commands    the start context commands
     * @param map         the sorted map of zone id to ordered list of start context commands
     * @param deployables the list of deployables
     * @throws GraphException if an error occurs building the graph used to calculatre order occurs
     */
    private void sort(Map<String, List<Command>> commands, CommandMap map, List<QName> deployables) throws GraphException {
        for (Map.Entry<String, List<Command>> entry : map.getCommands().entrySet()) {
            Map<QName, Integer> order = calculateDeployableOrder(entry.getValue(), deployables);
            if (order.isEmpty()) {
                return;
            }
            ContextComparator comparator = new ContextComparator(order);
            List<Command> list = commands.get(entry.getKey());
            if (list == null) {
                // no commands for zone
                return;
            }
            Collections.sort(list, comparator);
        }
    }


    /**
     * Builds a directed acyclic graph of deployable contexts related through collocated forward wires by introspecting the list of AttachWire
     * commands for a zone. This DAG is then sorted and the reverse topological order representing the deployable context dependencies is returned.
     * Callback wires are ignored as depenency ordering is done in the forward direction.
     *
     * @param commands    the list of commands
     * @param deployables the list of deployables
     * @return a map of deplopyable contexts and their relative ordering
     * @throws GraphException if an error building the graph is raised
     */
    private Map<QName, Integer> calculateDeployableOrder(List<Command> commands, List<QName> deployables) throws GraphException {
        DirectedGraph<QName> dag = new DirectedGraphImpl<QName>();
        // add the contributions as vertices
        for (Command command : commands) {
            if (command instanceof ConnectionCommand) {
                ConnectionCommand connectionCommand = (ConnectionCommand) command;
                for (AttachWireCommand wireCommand : connectionCommand.getAttachCommands()) {
                    PhysicalWireDefinition definition = wireCommand.getPhysicalWireDefinition();
                    QName source = definition.getSourceDeployable();
                    QName target = definition.getTargetDeployable();
                    Vertex<QName> sourceVertex = null;
                    if (source != null) {
                        sourceVertex = findVertex(dag, source);
                        if (sourceVertex == null) {
                            sourceVertex = new VertexImpl<QName>(source);
                            dag.add(sourceVertex);
                        }
                    }
                    Vertex<QName> targetVertex = null;
                    if (target != null && (source == null || !source.equals(target))) {
                        targetVertex = findVertex(dag, target);
                        if (targetVertex == null) {
                            targetVertex = new VertexImpl<QName>(target);
                            dag.add(targetVertex);
                        }
                    }
                    if (sourceVertex != null && targetVertex != null) {
                        Edge<QName> edge = dag.getEdge(sourceVertex, targetVertex);
                        if (edge == null) {
                            dag.add(new EdgeImpl<QName>(sourceVertex, targetVertex));
                        }
                    }
                }
            }
        }
        List<Vertex<QName>> vertices = sorter.reverseSort(dag);
        Map<QName, Integer> deployableOrder = new HashMap<QName, Integer>(vertices.size());
        int i = 0;
        while (i < vertices.size()) {
            Vertex<QName> vertex = vertices.get(i);
            deployableOrder.put(vertex.getEntity(), i);
            i++;
        }
        // The deployables calculated from the graph of wires may not be all of the deployables since a composite may contain components with no wires
        // Add the rest of the deployables to the end of the list
        for (QName deployable : deployables) {
            if (!deployableOrder.containsKey(deployable)) {
                deployableOrder.put(deployable, i);
                i++;
            }
        }
        return deployableOrder;

    }

    private Vertex<QName> findVertex(DirectedGraph<QName> dag, QName name) {
        for (Vertex<QName> vertex : dag.getVertices()) {
            if (vertex.getEntity().equals(name)) {
                return vertex;
            }
        }
        return null;
    }


}
