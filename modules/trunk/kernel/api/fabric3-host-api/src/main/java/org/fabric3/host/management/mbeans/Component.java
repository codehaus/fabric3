package weblogic.sca.management.mbeans.logical;

/**
 * @author Copyright (c) 2007 by BEA Systems. All Rights Reserved.
 */
public interface Component {

  String TYPE = DomainComposite.TYPE + ".Component";

  String getName();

  String getImplementationType();

  Service[] getServices();

  Reference[] getReferences();

  Property[] getProperties();

}
