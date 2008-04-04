package weblogic.sca.management.mbeans.logical;

/**
 * @author Copyright (c) 2007 by BEA Systems. All Rights Reserved.
 */
public interface DomainComposite {

  String TYPE = "Type=DomainComposite";

  String getName();

  Component[] getComponents();

}
