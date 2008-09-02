package org.fabric3.binding.jms.test.oneway;

import org.osoa.sca.annotations.Scope;

@Scope("COMPOSITE")
public class ParameterHolderImpl implements ParameterHolder {
    
    private String parameter;

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
    
    public String getParameter() {
        return parameter;        
    }
}
