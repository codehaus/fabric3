package weblogic.sca.management.mbeans.logical;

/**
 * @author Copyright (c) 2007 by BEA Systems. All Rights Reserved.
 */
public interface Property {

  String TYPE = Component.TYPE + ".Property";

  String getName();

  String getValue();
}
