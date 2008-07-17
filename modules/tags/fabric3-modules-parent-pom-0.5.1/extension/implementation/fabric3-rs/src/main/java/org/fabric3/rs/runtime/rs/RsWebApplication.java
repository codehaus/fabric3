package org.fabric3.rs.runtime.rs;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $Rev$ $Date$
 */
public final class RsWebApplication extends HttpServlet {

    RsServlet servlet;
    ClassLoader cl;
    ServletConfig cfg;
    Fabric3ComponentProvider provider;
    boolean reload = false;

    public RsWebApplication(ClassLoader cl) {
        this.cl = cl;
        this.provider = new Fabric3ComponentProvider();
        reload = true;
    }

    public void addResourceFactory(Class<?> resource, ObjectFactory<?> factory) {
        this.provider.addResource(resource, factory);
        reload = true;

    }

    public void addProviderFactory(Class<?> resource, ObjectFactory<?> factory) {
        this.provider.addProvider(resource, factory);
        reload = true;
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        cfg = new ServletConfigWrapper(config);
    }

    public void reload() throws ServletException {
        // Set the class loader to the runtime one so Jersey loads the
        // ResourceConfig properly
        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            this.servlet = new RsServlet(this.provider);
            servlet.init(cfg);
        } catch (ServletException se) {
            throw se;
        } catch (Throwable t) {
            ServletException se = new ServletException(t);
            throw se;
        } finally {
            Thread.currentThread().setContextClassLoader(oldcl);
        }
        reload = false;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (reload) {
            reload();
        }

        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        WorkContext oldContext = null;
        try {
            Thread.currentThread().setContextClassLoader(cl);
            WorkContext workContext = new WorkContext();
            CallFrame frame = new CallFrame();
            workContext.addCallFrame(frame);
            oldContext = PojoWorkContextTunnel.setThreadWorkContext(workContext);
            servlet.service(req, res);
        } catch (ServletException se) {
            throw se;
        } catch (IOException ie) {
            throw ie;
        } catch (Throwable t) {
            ServletException se = new ServletException(t);
            throw se;
        } finally {
            Thread.currentThread().setContextClassLoader(oldcl);
            PojoWorkContextTunnel.setThreadWorkContext(oldContext);
        }
    }

    /**
     * 
     * Wrapper class to add the Jersey resource class as a web app init
     * parameter
     * 
     */
    public class ServletConfigWrapper implements ServletConfig {

        ServletConfig config;

        public ServletConfigWrapper(ServletConfig config) {
            this.config = config;
        }

        public String getInitParameter(String name) {
            if ("javax.ws.rs.ApplicationConfig".equals(name)) {
                return Fabric3ResourceConfig.class.getName();
            }
            return config.getInitParameter(name);
        }

        public Enumeration getInitParameterNames() {
            final Enumeration e = config.getInitParameterNames();
            return new Enumeration() {

                boolean finished = false;

                public boolean hasMoreElements() {
                    if (e.hasMoreElements() || !finished) {
                        return true;
                    }
                    return false;
                }

                public Object nextElement() {
                    if (e.hasMoreElements()) {
                        return e.nextElement();
                    }
                    if (!finished) {
                        finished = true;
                        return "javax.ws.rs.ApplicationConfig";
                    }
                    return null;
                }
            };
        }

        public ServletContext getServletContext() {
            return config.getServletContext();
        }

        public String getServletName() {
            return config.getServletName();
        }
    }
}
