// $Id: CvsAddException.java,v 1.3 2000/08/16 21:37:25 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.cvs;

/**
 * @version $Revision: 1.3 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class CvsAddException extends CvsException {

    CvsAddException(String filename) {
	super(filename);
    }

}
