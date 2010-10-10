// DAVTimeout.java
// $Id: DAVTimeout.java,v 1.1 2000/09/20 15:07:47 bmahe Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 2000.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.www.webdav;

import org.w3c.www.http.BasicValue;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class DAVTimeout extends BasicValue {

    protected void parse() {

    }

    protected void updateByteValue() {

    }

    public Object getValue() {
	return this;
    }

    public DAVTimeout() {
	this.isValid = false;
    }
    
}
