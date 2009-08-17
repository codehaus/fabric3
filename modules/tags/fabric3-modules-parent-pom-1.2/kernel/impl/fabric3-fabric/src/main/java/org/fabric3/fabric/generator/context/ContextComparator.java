package org.fabric3.fabric.generator.context;

import java.util.Comparator;
import java.util.Map;
import javax.xml.namespace.QName;

import org.fabric3.fabric.command.StartContextCommand;
import org.fabric3.spi.command.Command;

public class ContextComparator implements Comparator<Command> {
    private Map<QName, Integer> deployableOrder;

    public ContextComparator(Map<QName, Integer> deployableOrder) {
        this.deployableOrder = deployableOrder;
    }

    public int compare(Command first, Command second) {
        if (!(first instanceof StartContextCommand) || !(second instanceof StartContextCommand)) {
            return 0;
        }
        QName firstDeployable = ((StartContextCommand) first).getDeployable();
        Integer firstPos = deployableOrder.get(firstDeployable);
        if (firstPos == null) {
            throw new AssertionError("Deployable not found:" + firstDeployable);
        }
        QName secondDeployable = ((StartContextCommand) second).getDeployable();
        Integer secondPos = deployableOrder.get(secondDeployable);
        if (secondPos == null) {
            throw new AssertionError("Deployable not found:" + secondDeployable);
        }
        return firstPos - secondPos;
    }
}
