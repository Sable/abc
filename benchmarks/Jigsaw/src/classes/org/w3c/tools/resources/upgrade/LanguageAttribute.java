// LanguageAttribute.java
// $Id: LanguageAttribute.java,v 1.3 1999/10/21 22:36:36 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

public class LanguageAttribute extends StringAttribute {

    public LanguageAttribute(String name, String def, Integer flags) {
	super(name, def, flags) ;
	this.type = "java.lang.String";
    }

}
