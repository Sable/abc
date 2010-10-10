// ClosableFrame.java
// $Id: ClosableFrame.java,v 1.3 2000/08/16 21:37:56 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @version $Revision: 1.3 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
abstract public class ClosableFrame extends Frame {

    class WindowCloser extends WindowAdapter {

	ClosableFrame frame = null;
	
	public void windowClosing(WindowEvent e) {
	    if (e.getWindow() == frame)
		frame.close();
	}

	WindowCloser(ClosableFrame frame) {
	    this.frame = frame;
	}
    }

    protected abstract void close();

    public ClosableFrame() {
	super();
	build();
    }

    public ClosableFrame(String title) {
	super(title);
	build();
    }

    private void build() {
	addWindowListener(new WindowCloser(this));
	setBackground(Color.lightGray);
    }

}
