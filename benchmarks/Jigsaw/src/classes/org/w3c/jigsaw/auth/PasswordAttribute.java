// PasswordAttribute.java
// $Id: PasswordAttribute.java,v 1.8 2002/06/09 11:25:47 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.auth ;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.StringAttribute;

public class PasswordAttribute extends StringAttribute {

    public PasswordAttribute(String name, String password, int flags) {
	super(name, password, flags);
	this.type = "java.lang.String".intern();
    }

    public PasswordAttribute() {
	super();
    }

}
