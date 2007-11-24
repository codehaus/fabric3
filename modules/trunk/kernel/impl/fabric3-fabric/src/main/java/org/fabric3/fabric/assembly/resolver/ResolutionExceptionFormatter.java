package org.fabric3.fabric.assembly.resolver;

import java.io.PrintWriter;
import java.net.URI;
import java.util.List;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.monitor.ExceptionFormatter;
import org.fabric3.host.monitor.FormatterRegistry;
import org.fabric3.monitor.FormatterHelper;

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
    }

    public Class<ResolutionException> getType() {
        return ResolutionException.class;
    }

    @Init
    public void init() {
        registry.register(ResolutionException.class, this);
    }

    @Destroy
    public void destroy() {
        registry.unregister(ResolutionException.class);
    }

    public void write(PrintWriter writer, ResolutionException e) {
        writer.append(e.getMessage());
        writer.append("\nSource: ").append(String.valueOf(e.getSource()));
        writer.append("\nTarget: ").append(String.valueOf(e.getTarget()));

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
            ExceptionFormatter<? super Throwable> formatter = getFormatter(cause.getClass());
            formatter.write(writer, cause);
        } else {
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement aTrace : trace) {
                writer.println("\tat " + aTrace);
            }
        }

    }

    private <T extends Throwable> ExceptionFormatter<? super T> getFormatter(Class<? extends T> type) {
        return registry.getFormatter(type);
    }
}
