package org.fabric3.assembly.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class CompositeConfig {

    private List<Composite> mComponents = new ArrayList<Composite>();

    public void addComposite(Composite pComposite) {
        mComponents.add(pComposite);
    }

    public List<Composite> getComposites() {
        return mComponents;
    }
}
