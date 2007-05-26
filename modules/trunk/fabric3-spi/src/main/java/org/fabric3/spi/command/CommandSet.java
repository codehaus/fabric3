package org.fabric3.spi.command;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of commands to be executed in order on a service node.
 *
 * @version $Rev$ $Date$
 */
public class CommandSet {
    public enum Phase {
        FIRST,
        STANDARD,
        LAST
    }

    private List<Command> first = new ArrayList<Command>();
    private List<Command> standard = new ArrayList<Command>();
    private List<Command> last = new ArrayList<Command>();

    /**
     * Adds a command to the end of the command list.
     *
     * @param phase   the phase to run the command in
     * @param command the command to add
     */
    public void add(Phase phase, Command command) {
        if (phase == Phase.FIRST) {
            first.add(command);
        } else if (phase == Phase.LAST) {
            last.add(command);
        } else {
            standard.add(command);
        }
    }

    /**
     * Returns the ordered list of commands for the given phase.
     *
     * @param phase the phase to return the command list for
     * @return the ordered list of commands
     */
    public List<Command> getCommands(Phase phase) {
        if (phase == Phase.FIRST) {
            return first;
        } else if (phase == Phase.LAST) {
            return last;
        } else {
            return standard;
        }
    }
}
