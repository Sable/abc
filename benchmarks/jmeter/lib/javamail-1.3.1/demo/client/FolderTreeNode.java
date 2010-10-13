/*
 * @(#)FolderTreeNode.java	1.8 01/05/23
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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.MessagingException;

/**
 * Node which represents a Folder in the javax.mail apis. 
 *
 * @version 1.8, 01/05/23
 * @author Christopher Cotton
 */
public class FolderTreeNode extends DefaultMutableTreeNode {
    
    protected Folder	folder = null;
    protected boolean	hasLoaded = false;

    /**
     * creates a tree node that points to the particular Store.
     *
     * @param what	the store for this node
     */
    public FolderTreeNode(Folder what) {
	super(what);
	folder = what;
    }

    
    /**
     * a Folder is a leaf if it cannot contain sub folders
     */
    public boolean isLeaf() {
	try {
	    if ((folder.getType() & Folder.HOLDS_FOLDERS) == 0)
	    	return true;
	} catch (MessagingException me) { }
	
	// otherwise it does hold folders, and therefore not
	// a leaf
	return false;
    }
   
    /**
     * returns the folder for this node
     */
    public Folder getFolder() {
	return folder;
    }
    


    /**
     * return the number of children for this folder node. The first
     * time this method is called we load up all of the folders
     * under the store's defaultFolder
     */

    public int getChildCount() {
	if (!hasLoaded) {
	    loadChildren();
	}
	return super.getChildCount();
    }
    
    protected void loadChildren() {
	// if it is a leaf, just say we have loaded them
	if (isLeaf()) {
	    hasLoaded = true;
	    return;
	}

	try {
	    // Folder[] sub = folder.listSubscribed();
	    Folder[] sub = folder.list();

	    // add a FolderTreeNode for each Folder
	    int num = sub.length;
	    for(int i = 0; i < num; i++) {
		FolderTreeNode node = new FolderTreeNode(sub[i]);
		// we used insert here, since add() would make
		// another recursive call to getChildCount();
		insert(node, i);
	    }
	    
	} catch (MessagingException me) {
	    me.printStackTrace();
	}
    }


    /**
     * override toString() since we only want to display a folder's
     * name, and not the full path of the folder
     */
    public String toString() {
	return folder.getName();
    }
    
}

