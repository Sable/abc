/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

package com.servingxml.components.flatfile.recordtype;

import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;

import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.xml.XsltChooserFactory;
import com.servingxml.util.xml.XsltChooser;
import com.servingxml.components.common.SimpleNameEvaluator;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.util.PrefixMap;                 
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.app.Environment;

/**
 * The <code>FlatRecordTypeChoiceFactory</code> implements a resolver for resolving record types.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatRecordTypeChoiceFactory implements FlatRecordTypeFactory {
  private final Environment env;
  private final XsltConfiguration xsltConfiguration;
  private final FlatRecordFieldFactory[] fieldTypeFactories;
  private final FlatRecordTypeSelectionFactory[] flatRecordTypeSelectionFactories;
  private final TransformerFactory transformerFactory;
  private final FlatFileOptionsFactory flatFileOptionsFactory;
  private final PrefixMap prefixMap;

  public FlatRecordTypeChoiceFactory(Environment env, FlatRecordFieldFactory[] fieldTypeFactories,
                                     FlatRecordTypeSelectionFactory[] flatRecordTypeSelectionFactories,
                                     XsltConfiguration xsltConfiguration, 
                                     TransformerFactory transformerFactory, PrefixMap prefixMap,
                                     FlatFileOptionsFactory flatFileOptionsFactory) {
    this.env = env;
    this.fieldTypeFactories = fieldTypeFactories;
    this.flatRecordTypeSelectionFactories = flatRecordTypeSelectionFactories;
    this.xsltConfiguration = xsltConfiguration;
    this.flatFileOptionsFactory = flatFileOptionsFactory;
    this.transformerFactory = transformerFactory;
    this.prefixMap = prefixMap;
  }

  public FlatRecordType createFlatRecordType(ServiceContext context, Flow flow, FlatFileOptions defaultOptions) {
    //System.out.println(getClass().getName()+".createFlatRecordType enter");

    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, defaultOptions);

    FlatRecordTypeSelection[] flatRecordTypeSelections = new FlatRecordTypeSelection[flatRecordTypeSelectionFactories.length];
    String[] tests = new String[flatRecordTypeSelections.length];

    for (int i = 0; i < flatRecordTypeSelectionFactories.length; ++i) {
      flatRecordTypeSelections[i] = flatRecordTypeSelectionFactories[i].createFlatRecordTypeSelection(context, flow, flatFileOptions);
      tests[i] = flatRecordTypeSelections[i].getTestExpression();
    }
    Name defaultRecordTypeName = new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"tag");
    NameSubstitutionExpr defaultRecordTypeNameResolver = new SimpleNameEvaluator(defaultRecordTypeName);

    FlatRecordTypeFactory defaultRecordTypeFactory = new FlatRecordTypeFactoryImpl(
      defaultRecordTypeNameResolver, fieldTypeFactories, IntegerSubstitutionExpr.NULL, flatFileOptionsFactory); 
    FlatRecordType defaultRecordType = defaultRecordTypeFactory.createFlatRecordType(context, flow, flatFileOptions);

    String baseUri = "";

    XsltChooserFactory chooserFactory = new XsltChooserFactory(transformerFactory, 
                                                               baseUri,
                                                               tests, 
                                                               prefixMap, 
                                                               xsltConfiguration.getVersion());
    XsltChooser chooser = chooserFactory.createXsltChooser();
    chooser.setUriResolverFactory(context.getUriResolverFactory());
    FlatRecordTypeChoice flatRecordTypeChoice = new FlatRecordTypeChoice(env, defaultRecordType, flatRecordTypeSelections, 
                                                                          chooser);

    return flatRecordTypeChoice;
  }

  public void appendFlatRecordField(ServiceContext context, Flow flow,
    FlatFileOptions defaultOptions, List<FlatRecordField> flatRecordFieldList) {
  }

  public boolean isFieldDelimited() {
    boolean delimited = true;
    for (int i = 0; delimited && i < fieldTypeFactories.length; ++i) {
      if (!fieldTypeFactories[i].isFieldDelimited()) {
        delimited = false;
      }
    }
    for (int i = 0; delimited && i < flatRecordTypeSelectionFactories.length; ++i) {
      if (!flatRecordTypeSelectionFactories[i].isFieldDelimited()) {
        delimited = false;
      }
    }
    return delimited;
  }

  public boolean isBinary() {
    boolean binary = true;
    for (int i = 0; binary && i < fieldTypeFactories.length; ++i) {
      if (!fieldTypeFactories[i].isBinary()) {
        binary = false;
      }
    }
    for (int i = 0; binary && i < flatRecordTypeSelectionFactories.length; ++i) {
      if (!flatRecordTypeSelectionFactories[i].isBinary()) {
        binary = false;
      }
    }
    return binary;
  }

  public boolean isText() {
    boolean textValue = true;
    for (int i = 0; textValue && i < fieldTypeFactories.length; ++i) {
      if (!fieldTypeFactories[i].isText()) {
        textValue = false;
      }
    }
    for (int i = 0; textValue && i < flatRecordTypeSelectionFactories.length; ++i) {
      if (!flatRecordTypeSelectionFactories[i].isText()) {
        textValue = false;
      }
    }
    return textValue;
  }
}
