// FileEnumeration.java
// $Id: FileEnumeration.java,v 1.5 2000/08/16 21:37:26 ylafon Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

class FileEnumeration implements Enumeration {
    CvsEntry    next = null;
    Enumeration _enum = null;

    private CvsEntry computeNextElement() {
	if ( _enum == null ) 
	    return (next = null);
	while ((next == null) && _enum.hasMoreElements()) {
	    CvsEntry entry = (CvsEntry) _enum.nextElement();
	    if ( ! entry.isdir )
		next = entry;
	}
	return next;
    }

    public synchronized boolean hasMoreElements() {
	return ((next != null) || ((next = computeNextElement()) != null));
    }

    public synchronized Object nextElement() {
	if ( next == null ) {
	    if ((next = computeNextElement()) == null)
		throw new NoSuchElementException("invalid _enum");
	}
	CvsEntry item = next;
	next = null;
	return item.name;
    }

    FileEnumeration(Hashtable entries) {
	this._enum = (entries != null) ? entries.elements() : null;
    }

}
