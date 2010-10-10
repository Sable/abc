// InvalidLabelFileException.java
// $Id: InvalidLabelFileException.java,v 1.4 2000/08/16 21:37:43 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pics ;

import java.io.File ;

public class InvalidLabelFileException extends InvalidLabelException {

    public InvalidLabelFileException (String msg) {
        super (msg) ;
    }

    public InvalidLabelFileException (File file, int lineno, String msg) {
        this (file.getAbsolutePath()
              + "[" + lineno + "]"
              + ": " + msg) ;
    }
}
