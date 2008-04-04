package weblogic.sca.management.mbeans.logical;

/**
 * @author Copyright (c) 2007 by BEA Systems. All Rights Reserved.
 */
public interface Service {

  String TYPE = Component.TYPE + ".Service";
  
  String getName();

  String getInterface();

  String getBindingType();
  
}
