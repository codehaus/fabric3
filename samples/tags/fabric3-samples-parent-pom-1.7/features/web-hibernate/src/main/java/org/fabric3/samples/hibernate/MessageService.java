/*
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
package org.fabric3.samples.hibernate;

import java.net.URI;
import java.util.List;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.ManagedTransaction;

import org.fabric3.api.Fabric3RequestContext;
import org.fabric3.api.SecuritySubject;
import org.fabric3.api.annotation.security.RolesAllowed;

/**
 * Receives resource representations as JSON or XML and persists them as part of a global transaction using Hibernate.
 *
 * @version $Rev: 8746 $ $Date: 2010-03-27 22:35:03 +0100 (Sat, 27 Mar 2010) $
 */
@Path("/")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@ManagedTransaction
@RolesAllowed("role1")
public class MessageService {
    private Session session;
    private Fabric3RequestContext context;

    @Context
    public void setContext(Fabric3RequestContext context) {
        this.context = context;
    }

    @PersistenceContext(name = "messageEmf", unitName = "message")
    public void setSession(Session session) {
        this.session = session;
    }

    @POST
    @Path("/message")
    public Response create(Message message) {
        System.out.println("Saving message");
        SecuritySubject subject = context.getCurrentSubject();
        message.setCreator(subject.getUsername());
        session.save(message);
        return Response.created(URI.create(message.getId().toString())).build();
    }

    @GET
    @Path("message/{id}")
    public Message retrieve(@PathParam("id") Long id) {
        Message message = (Message) session.get(Message.class, id);
        if (message == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(new Reason("Not found")).build();
            throw new WebApplicationException(response);
        }
        return message;
    }

    @GET
    @SuppressWarnings({"unchecked"})
    public MessageList retrieveMessages() {
        System.out.println("Getting messages");
        List<Message> messages = session.createCriteria(Message.class).list();
        return new MessageList(messages);
    }

    @DELETE
    @Path("message/{id}")
    public Response delete(@PathParam("id") Long id) {
        System.out.println("Deleting message");
        int num = session.createQuery("delete Message message where id=" + id).executeUpdate();
        if (num == 0) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(new Reason("Not found")).build();
            throw new WebApplicationException(response);
        }
        return Response.ok().build();
    }

}
