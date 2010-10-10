// HttpInvalidValueException.java
// $Id: HttpInvalidValueException.java,v 1.2 1998/01/22 14:28:17 bmahe Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

public class HttpInvalidValueException extends RuntimeException {

    public HttpInvalidValueException(String msg) {
	super(msg);
    }

}
