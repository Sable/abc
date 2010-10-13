/*
 * @(#)ComponentFrame.java	1.8 01/05/23
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES
 * SUFFERED BY LICENSEE AS A RESULT OF  OR RELATING TO USE, MODIFICATION
 * OR DISTRIBUTION OF THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.WindowConstants;


/**
 * this Frame provides a utility class for displaying a single
 * Component in a Frame.
 *
 * @version	1.8, 01/05/23
 * @author	Christopher Cotton
 */

public class ComponentFrame extends JFrame {
    
    /**
     * creates the frame
     * @param what	the component to display
     */
    public ComponentFrame(Component what) {
	this(what, "Component Frame");
    }

    /**
     * creates the frame with the given name
     * @param what	the component to display
     * @param name	the name of the Frame
     */
    public ComponentFrame(Component what, String name) {
	super(name);

	// make sure that we close and dispose ourselves when needed
	setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	// default size of the frame
	setSize(700,600);

	// we want to display just the component in the entire frame
	if (what != null) {
	    getContentPane().add("Center", what);
	}
    }
}
