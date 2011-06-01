package org.fabric3.assembly.configuration;

import org.fabric3.assembly.dependency.UpdatePolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class CompositeConfig {

    //TODO <capo> does composites needs a update? are they cleaned up on every start? or just when servers' update policy for server to clean it up?
    private UpdatePolicy mUpdatePolicy;

    private List<Composite> mComponents = new ArrayList<Composite>();

    public void setUpdatePolicy(UpdatePolicy pUpdatePolicy) {
        mUpdatePolicy = pUpdatePolicy;
    }

    public UpdatePolicy getUpdatePolicy() {
        return mUpdatePolicy;
    }

    public void addComposite(Composite pComposite) {
        mComponents.add(pComposite);
    }

    public List<Composite> getComposites() {
        return mComponents;
    }
}
