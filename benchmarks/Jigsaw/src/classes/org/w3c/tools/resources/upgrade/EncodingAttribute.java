// EncodingAttribute.java
// $Id: EncodingAttribute.java,v 1.3 1999/10/21 22:36:35 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

public class EncodingAttribute extends StringAttribute {

    public EncodingAttribute(String name, String def, Integer flags) {
	super(name, def, flags) ;
	this.type = "java.lang.String";
    }

}
