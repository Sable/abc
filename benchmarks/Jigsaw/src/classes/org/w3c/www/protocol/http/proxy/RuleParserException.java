// RuleParserException.java
// $Id: RuleParserException.java,v 1.1 1998/09/11 10:37:26 bmahe Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.proxy ;
/**
 * Exception thrown when parsing the rule file fails.
 */

public class RuleParserException extends Exception {

    public RuleParserException(String msg) {
	super(msg);
    }

}
