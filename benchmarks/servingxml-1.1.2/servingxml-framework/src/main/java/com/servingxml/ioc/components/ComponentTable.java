package com.servingxml.ioc.components;

import java.util.Map;

import com.servingxml.util.NameTable;

public interface ComponentTable extends ComponentDictionary {
  void addServiceComponent(ServiceComponent component);
  void addConfigurationComponent(ConfigurationComponent component);
  void addAbstractComponent(AbstractComponent component);
  void setDefaultServiceComponent(ServiceComponent defaultServiceComponent);
  boolean isEmpty();
  void initialize(NameTable nameTable, ComponentTable parent, Map<Integer,Class> serviceInterfaceMap);
  void initialize(NameTable nameTable);
  ServiceComponent findServiceComponent(int nameSymbol);
}                                                                           
