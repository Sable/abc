/*
 * @(#)MultipartViewer.java	1.14 01/05/23
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
import java.io.*;
import java.beans.*;
import javax.activation.*;
import javax.mail.*;
import javax.swing.JPanel;


/**
 * A Viewer Bean for the type multipart/mixed
 *
 * @version	1.14, 01/05/23
 * @author	Christopher Cotton
 */

public class MultipartViewer extends JPanel implements CommandObject {
    
    protected DataHandler	dh = null;
    protected String		verb = null;
    
    public MultipartViewer() {
	super(new GridBagLayout());
    }

    
    public void setCommandContext(String verb, DataHandler dh) throws IOException {
	this.verb = verb;
	this.dh = dh;
	
	// get the content, and hope it is a Multipart Object
	Object content = dh.getContent();
	if (content instanceof Multipart) {
	    setupDisplay((Multipart)content);
	} else {
	    setupErrorDisplay(content);
	}
    }

    protected void setupDisplay(Multipart mp) {
	// we display the first body part in a main frame on the left, and then
	// on the right we display the rest of the parts as attachments

	GridBagConstraints gc = new GridBagConstraints();
	gc.gridheight = GridBagConstraints.REMAINDER;
	gc.fill = GridBagConstraints.BOTH;
	gc.weightx = 1.0;
	gc.weighty = 1.0;

	// get the first part
	try {
	    BodyPart bp = mp.getBodyPart(0);
	    Component comp = getComponent(bp);
	    add(comp, gc);
	    
	} catch (MessagingException me) {
	    add(new Label(me.toString()), gc);
	}

	// see if there are more than one parts
	try {
	    int count = mp.getCount();

	    // setup how to display them
	    gc.gridwidth = GridBagConstraints.REMAINDER;
	    gc.gridheight = 1;
	    gc.fill = GridBagConstraints.NONE;
	    gc.anchor = GridBagConstraints.NORTH;
	    gc.weightx = 0.0;
	    gc.weighty = 0.0;
	    gc.insets = new Insets(4,4,4,4);

	    // for each one we create a button with the content type
	    for(int i = 1; i < count; i++) { // we skip the first one 
		BodyPart curr = mp.getBodyPart(i);
		String label = null;
		if (label == null) label = curr.getFileName();
		if (label == null) label = curr.getDescription();
		if (label == null) label = curr.getContentType();

		Button but = new Button(label);
		but.addActionListener( new AttachmentViewer(curr));
		add(but, gc);
	    }
	    
	    
	} catch(MessagingException me2) {
	    me2.printStackTrace();
	}

    }

    protected Component getComponent(BodyPart bp) {

	try {
	    DataHandler dh = bp.getDataHandler();
	    CommandInfo ci = dh.getCommand("view");
	    if (ci == null) {
		throw new MessagingException(
		    "view command failed on: " +
		    bp.getContentType());
	    }
	    
	    Object bean = dh.getBean(ci);
	
	    if (bean instanceof Component) {
		return (Component)bean;
	    } else {
		if (bean == null)
		    throw new MessagingException(
			"bean is null, class " + ci.getCommandClass() +
			" , command " + ci.getCommandName());
		else
		    throw new MessagingException(
			"bean is not a awt.Component" +
			bean.getClass().toString());
	    }
	}
	catch (MessagingException me) {
	    return new Label(me.toString());
	}
    }
    

    
    protected void setupErrorDisplay(Object content) {
	String error;

	if (content == null)
	    error = "Content is null";
	else
	    error = "Object not of type Multipart, content class = " +
	    content.getClass().toString();
	
	System.out.println(error);
	Label lab = new Label(error);
	add(lab);
    }
   
    class AttachmentViewer implements ActionListener {
	
	BodyPart bp = null;
	
	public AttachmentViewer(BodyPart part) {
	    bp = part;
	}
	
	public void actionPerformed(ActionEvent e) {
	    ComponentFrame f = new ComponentFrame(
		getComponent(bp), "Attachment");
	    f.pack();
	    f.show();
	}
    }

}
