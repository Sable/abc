// DAVResponse.java
// $Id: DAVResponse.java,v 1.5 2000/10/12 16:19:20 bmahe Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav.xml;

import java.util.Date;
import java.util.Vector;

import org.w3c.dom.Element;

/**
 * @version $Revision: 1.5 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVResponse extends DAVNode {

    //
    // Response
    //

    public String getHref() {
	return getTextChildValue(HREF_NODE);
    }

    public String[] getHrefs() {
	return getMultipleTextChildValue(HREF_NODE);
    }

    public String getStatus() {
	return getTextChildValue(STATUS_NODE);
    }

    public String getDescription() {
	return getTextChildValue(RESPONSEDESC_NODE);
    }

    public void setDescription(String descr) {
	addDAVNode(RESPONSEDESC_NODE, descr);
    }

    //
    // Propstat
    //
    public DAVPropStat[] getPropStats() {
	Vector v = getDAVElementsByTagName(PROPSTAT_NODE);
	DAVPropStat dps[] = new DAVPropStat[v.size()];
	for (int i = 0 ; i < v.size(); i++) {
	    dps[i] = new DAVPropStat((Element)v.elementAt(i));
	}
	return dps;
    }

    DAVResponse(Element element) {
	super(element);
    }
    
}
