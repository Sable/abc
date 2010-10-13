/**
 *  ServingXML
 *  
 *  Copyright (C) 2004  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  See terms of license at gnu.org.
 *
 */ 

package com.servingxml.components.common;

import com.servingxml.util.Name;
import com.servingxml.util.StringHelper;
import com.servingxml.util.record.Record;
import com.servingxml.util.QualifiedName;
import com.servingxml.expr.substitution.SubstitutionExpr;

class NameSubstitutionExprImpl extends NameSubstitutionExpr {    
  private final SubstitutionExpr localNameResolver;
  private final String namespaceUri;

  protected NameSubstitutionExprImpl(String namespaceUri, SubstitutionExpr localNameResolver) {
    this.namespaceUri = namespaceUri;
    this.localNameResolver = localNameResolver;
  }

  public boolean isExpression() {
    return localNameResolver.isLiteral() ? false : true;
  }

  public Name getName() {
    return evaluateName(Record.EMPTY,Record.EMPTY);
  }

  public boolean hasName(Name name) {

    boolean result = false;
    if (evaluateName(Record.EMPTY,Record.EMPTY).equals(name)) {
      result = true;
    }
    return result;
  }

  public Name evaluateName(Record parameters, Record record) {
    String localName = localNameResolver.evaluateAsString(parameters,record);
    if (localName == null) {
      localName = "";
    }
    if (!localNameResolver.isLiteral()) {
      localName = StringHelper.constructNameFromValue(localName);
    }
    Name name = new QualifiedName(namespaceUri,localName);
   //System.out.println(getClass()+".evaluateName " + name);
    return name;
  }
}


