package com.servingxml.ioc.components;

import java.io.PrintStream;
import com.servingxml.util.NameTable;

public interface ComponentDictionary {
  AbstractComponent getAbstractComponent(int nameSymbol);
  ServiceComponent getServiceComponent(int nameSymbol);
  ServiceComponent findServiceComponent(int nameSymbol);
  ServiceComponent getDefaultServiceComponent();
  ConfigurationComponent getConfigurationComponent(int nameSymbol);
  Class getInterface(int nameSymbol);
  void printDiagnostics(PrintStream printStream, NameTable nameTable);
}
