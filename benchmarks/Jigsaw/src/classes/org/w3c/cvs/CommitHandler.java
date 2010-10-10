// CommitHandler.java
// $Id: CommitHandler.java,v 1.3 1998/01/22 14:22:51 bmahe Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs;

abstract class CommitHandler {
    abstract void notifyEntry(String filename, int status);
}
