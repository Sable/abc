// LabelInterface.java
// $Id: LabelInterface.java,v 1.3 2000/08/16 21:37:43 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pics ;

import java.net.URL ;

public interface LabelInterface {

    /**
     * Dump this label in the given buffer.
     * @param buffer The buffer to dump the label to.
     * @param format The PICS format in which the label was requested.
     */

    public void dump (StringBuffer buffer, int format) ;

}
