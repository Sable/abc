// LabelBureauFactory.java
// $Id: LabelBureauFactory.java,v 1.4 1998/07/24 14:14:02 bmahe Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pics ;

import java.io.File ;

import java.util.Hashtable ;
import java.util.StringTokenizer ;
/**
 * This class manages label bureau creation.
 * It follows the general pattern for creating interface objects.
 */

public class LabelBureauFactory {

    private static Hashtable bureaus = new Hashtable () ;

    public static LabelBureauInterface getLabelBureau (File identifier) {
	String name = identifier.getName();
	SampleLabelBureau b = (SampleLabelBureau) bureaus.get (identifier) ;
	if ( b == null ) {
	    b =  new SampleLabelBureau (identifier) ;
	    bureaus.put (identifier, b) ;
	}
	return b ;
    }

}
