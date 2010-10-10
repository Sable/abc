// DAVPropertyBehavior.java
// $Id: DAVPropertyBehavior.java,v 1.2 2000/10/16 12:30:22 bmahe Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVPropertyBehavior extends DAVNode {

    public boolean omit() {
	return (getDAVNode(OMIT_NODE) != null);
    }

    public boolean keepaliveAll() {
	String star = getTextChildValue(KEEPALIVE_NODE).trim();
	return ((star != null) && (star.equals("*")));
    }

    public String[] getHrefs() {
	Node keepalive = getDAVNode(KEEPALIVE_NODE);
	if (keepalive != null) {
	    return getMultipleTextChildValue(keepalive, HREF_NODE);
	}
	return null;
    }

    DAVPropertyBehavior(Element element) {
	super(element);
    }
    
}
