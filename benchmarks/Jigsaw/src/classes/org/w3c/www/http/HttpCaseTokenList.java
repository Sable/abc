// HttpCaseTokenList.java
// $Id: HttpCaseTokenList.java,v 1.3 2000/08/16 21:37:59 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

/**
 * Parse a comma separated list of tokens.
 */

public class HttpCaseTokenList extends HttpTokenList {
    /**
     * Create a parsed token list, for emitting.
     */

    protected HttpCaseTokenList(String tokens[]) {
	super(tokens);
	this.casemode = CASE_ASIS;
    }

    /**
     * Create a token list from a comma separated list of tokens.
     */

    protected HttpCaseTokenList(String tokens) {
	super(tokens);
	this.casemode = CASE_ASIS;
    }

    /**
     * Create an empty token list for parsing.
     */

    protected HttpCaseTokenList() {
	super();
	this.casemode = CASE_ASIS;
    }

}
