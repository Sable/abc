// FilenameAttribute.java
// $Id: FilenameAttribute.java,v 1.4 2002/06/09 10:14:29 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

/**
 * The generic description of a FilenameAttribute.
 * A file name is a String, augmented with the fact that it should be a valid 
 * file name.
 */

public class FilenameAttribute extends StringAttribute {

    public FilenameAttribute(String name, Object def, int flags) {
	super(name, (String) def, flags) ;
	this.type = "java.lang.String".intern();
    }

    public FilenameAttribute() {
	super() ;
    }

}
