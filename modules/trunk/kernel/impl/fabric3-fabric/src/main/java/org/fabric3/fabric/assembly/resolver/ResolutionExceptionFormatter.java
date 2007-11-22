package org.fabric3.fabric.assembly.resolver;

import java.io.PrintWriter;
import java.net.URI;
import java.util.List;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.monitor.FormatterHelper;
import org.fabric3.host.monitor.ExceptionFormatter;
import org.fabric3.host.monitor.FormatterRegistry;

/**
 * Formats {@link ResolutionException}s
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ResolutionExceptionFormatter implements ExceptionFormatter<ResolutionException> {
    private FormatterRegistry registry;

    public ResolutionExceptionFormatter(@Reference FormatterRegistry factory) {
        this.registry = factory;
        factory.register(this);
    }

    public boolean canFormat(Class<?> type) {
        return ResolutionException.class.isAssignableFrom(type);
    }

    @Destroy
    public void destroy() {
        registry.unregister(this);
    }

    public void write(PrintWriter writer, ResolutionException e) {
        writer.append(e.getMessage());
        if (e.getSource() != null) {
            writer.append("\nSource: ").append(String.valueOf(e.getSource()));
        }
        if (e.getTarget() != null) {
            writer.append("\nTarget: ").append(e.getTarget().toString());
        }
        if (e instanceof AmbiguousAutowireTargetException) {
            List<URI> uris = ((AmbiguousAutowireTargetException) e).getTargets();
            if (!uris.isEmpty()) {
                writer.append("\nTargets:\n");
            }
            for (URI uri : uris) {
                writer.append("   ").append(uri.toString()).append("\n");
            }
        }
        writer.append("\n");
        Throwable cause = e.getCause();
        if (cause != null) {
            FormatterHelper.writeStackTrace(writer, e, cause);
            writer.println("Caused by:");
            registry.formatException(writer, cause);
        } else {
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement aTrace : trace) {
                writer.println("\tat " + aTrace);
            }
        }

    }

}
