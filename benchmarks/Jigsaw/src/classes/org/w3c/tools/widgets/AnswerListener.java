// AnswerListener.java
// $Id: AnswerListener.java,v 1.2 2000/08/16 21:37:56 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

/**
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface AnswerListener {

    public int YES = 1;
    public int NO  = 2;

    public void questionAnswered (Object source, int response);

}
