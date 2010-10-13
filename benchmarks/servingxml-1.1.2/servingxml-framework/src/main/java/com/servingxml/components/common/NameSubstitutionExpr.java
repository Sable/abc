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
import com.servingxml.util.QnameContext;
import com.servingxml.util.record.Record;
import com.servingxml.expr.substitution.SubstitutionExpr;

public abstract class NameSubstitutionExpr {    

  public static NameSubstitutionExpr parse(QnameContext context, String expr) {
    String prefix = "";
    String namespaceUri = "";
    int pos = expr.indexOf(':');
    if (pos != -1) {
      prefix = expr.substring(0,pos);
      namespaceUri = context.getNamespaceUri(prefix);
    }
    String localPart = pos == -1 ? expr : expr.substring(pos+1);
   //System.out.println("NameSubstitutionExpr.parse namespace=" + namespaceUri + ", expr="+expr);
    SubstitutionExpr localNameResolver = SubstitutionExpr.parseString(context,localPart);
    return new NameSubstitutionExprImpl(namespaceUri,localNameResolver);
  }

  public abstract boolean isExpression();

  public abstract Name getName();

  public abstract boolean hasName(Name name);

  public abstract Name evaluateName(Record parameters, Record record);
}


