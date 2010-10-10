// PasswordAttribute.java
// $Id: PasswordAttribute.java,v 1.3 1999/10/21 22:36:37 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade;

public class PasswordAttribute extends StringAttribute {

    public PasswordAttribute(String name, String password, Integer flags) {
	super(name, password, flags);
	this.type = "java.lang.String";
    }

}
