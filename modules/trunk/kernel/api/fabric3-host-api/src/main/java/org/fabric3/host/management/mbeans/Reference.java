package weblogic.sca.management.mbeans.logical;

/**
 * @author Copyright (c) 2007 by BEA Systems. All Rights Reserved.
 */
public interface Reference {

  String TYPE = Component.TYPE + ".Reference";

  String getName();

  String getInterface();

  String getBindingType();

}
