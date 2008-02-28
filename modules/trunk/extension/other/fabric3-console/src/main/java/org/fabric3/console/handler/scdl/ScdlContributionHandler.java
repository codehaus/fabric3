package org.fabric3.console.handler.scdl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.spi.host.ServletHost;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

@SuppressWarnings("serial")
/**
 * TODO JFM this class needs to be reimplemented with the Assembly
 */
public class ScdlContributionHandler extends Fabric3Servlet {


    /**
     * Injects the servlet host and path mapping.
     *
     * @param servletHost Servlet host to use.
     * @param path        Path mapping for the servlet.
     */
    public ScdlContributionHandler(@Reference(name = "servletHost") ServletHost servletHost, 
                                   @Property(name = "path") String path) {
        super(servletHost, path);
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
    }

}
