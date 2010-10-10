// UpdateHandler.java
// $Id: UpdateHandler.java,v 1.3 1998/01/22 14:24:32 bmahe Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs;

abstract class UpdateHandler implements CVS {

    abstract void notifyEntry(String filename, int status);

}
