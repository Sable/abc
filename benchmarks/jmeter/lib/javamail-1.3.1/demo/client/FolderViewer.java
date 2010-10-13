/*
 * @(#)FolderViewer.java	1.11 01/05/23
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
import javax.mail.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * @version	1.11, 01/05/23
 * @author	Christopher Cotton
 * @author	Bill Shannon
 */

public class FolderViewer extends JPanel {

    FolderModel model = new FolderModel();
    JScrollPane scrollpane;
    JTable table;

    public FolderViewer() {
	this(null);
    }

    public FolderViewer(Folder what) {
	super(new GridLayout(1,1));

	table = new JTable(model);
	table.setShowGrid(false);

	scrollpane = JTable.createScrollPaneForTable(table);
	
	// setup the folder we were given
	setFolder(what);
	
	// find out what is pressed
	table.getSelectionModel().addListSelectionListener(
	    new FolderPressed());
	scrollpane.setPreferredSize(new Dimension(700, 300));
	add(scrollpane);
    }

    /**
     * Change the current Folder for the Viewer
     *
     * @param what	the folder to be viewed
     */
    public void setFolder(Folder what) {
	try {
	    table.getSelectionModel().clearSelection();
	    if (SimpleClient.mv != null)
		SimpleClient.mv.setMessage(null);
	    model.setFolder(what);
	    scrollpane.invalidate();
	    scrollpane.validate();
	} catch (MessagingException me) {
	    me.printStackTrace();
	}
    }

    class FolderPressed implements ListSelectionListener {

	public void valueChanged(ListSelectionEvent e) {
	    if (model != null && !e.getValueIsAdjusting()) {
		ListSelectionModel lm = (ListSelectionModel) e.getSource();
		int which = lm.getMaxSelectionIndex();
		if (which != -1) {
		    // get the message and display it
		    Message msg = model.getMessage(which);
		    SimpleClient.mv.setMessage(msg);
		}
	    }
	}
    }
}
