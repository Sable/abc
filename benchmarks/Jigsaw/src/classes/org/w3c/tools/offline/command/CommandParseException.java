// CommandParseException.java
// $Id: CommandParseException.java,v 1.2 2002/02/04 17:28:12 cbournez Exp $
// (c) COPYRIGHT MIT and INRIA, 2002.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.offline.command;


/**
 * The command line is not a valid jigshell command
 */

public class CommandParseException extends Exception {

	public CommandParseException() {
		super("ParseError"); 
	}

}
