package org.fabric3.fabric.command;

import java.net.URI;

import org.fabric3.spi.command.AbstractCommand;

/**
 * @author Copyright (c) 2008 by BEA Systems. All Rights Reserved.
 */
public class UnprovisionClassloaderCommand extends AbstractCommand {

    private final URI uri;
    public UnprovisionClassloaderCommand(int order, URI uri) {
        super(order);
        this.uri = uri;
        assert uri != null;
    }

    public URI getUri() {
        return uri;
    }

    public int hashCode() {
        return uri.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        try {
            UnprovisionClassloaderCommand other = (UnprovisionClassloaderCommand) obj;
            return uri.equals(other.uri);
        } catch (ClassCastException cce) {
            return false;
        }
    }

}

