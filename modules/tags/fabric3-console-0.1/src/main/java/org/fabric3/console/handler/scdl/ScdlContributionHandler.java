package org.fabric3.console.handler.scdl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.marshaller.MarshallerRegistry;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.services.messaging.MessagingService;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

@SuppressWarnings("serial")
/**
 * TODO JFM this class needs to be reimplemented with the Assembly
 */
public class ScdlContributionHandler extends Fabric3Servlet {

    // Marshaller registry
    private MarshallerRegistry marshallerRegistry;

    // Messaging service
    private MessagingService messagingService;

    /**
     * Injects the servlet host and path mapping.
     *
     * @param servletHost Servlet host to use.
     * @param path        Path mapping for the servlet.
     */
    public ScdlContributionHandler(@Reference(name = "servletHost") ServletHost servletHost, 
                                   @Property(name = "path") String path, 
                                   @Reference MarshallerRegistry marshallerRegistry, 
                                   @Reference MessagingService messagingService) {
        super(servletHost, path);
        this.marshallerRegistry = marshallerRegistry;
        this.messagingService = messagingService;
    }

    /**
     * Processes the request.
     *
     * @param req Servlet request.
     * @param res Servlet response.
     * @throws ServletException Servlet exception.
     * @throws IOException      IO Exception.
     */
    protected void process(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);

            PhysicalChangeSet physicalChangeSet = new PhysicalChangeSet();
            marshallerRegistry.marshall(physicalChangeSet, writer);

            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in);

            messagingService.sendMessage("slave1", reader);
            
            System.err.println("Message sent");
            
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

}
