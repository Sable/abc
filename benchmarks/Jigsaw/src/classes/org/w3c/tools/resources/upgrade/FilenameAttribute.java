// FilenameAttribute.java
// $Id: FilenameAttribute.java,v 1.4 2000/08/16 21:37:55 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.upgrade ;

/**
 * The generic description of a FilenameAttribute.
 * A file name is a String, augmented with the fact that it should be a valid 
 * file name.
 */

public class FilenameAttribute extends StringAttribute {

    public FilenameAttribute(String name, String def, Integer flags) {
	super(name, def, flags) ;
	this.type = "java.lang.String";
    }

}
