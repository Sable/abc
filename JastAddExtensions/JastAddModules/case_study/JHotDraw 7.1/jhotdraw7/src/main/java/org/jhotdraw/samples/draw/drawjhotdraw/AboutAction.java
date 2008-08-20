/*
 * @(#)AboutAction.java  1.0  04 January 2005
 *
 * Copyright (c) 1996-2006 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */

module org.jhotdraw.samples.draw.drawjhotdraw;
package org.jhotdraw.app.action;

import org.jhotdraw.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.jhotdraw.app.*;

//NEIL: Add batik import
import batik::org.apache.batik.Version;

/**
 * Displays a dialog showing information about the application.
 *
 * @author  Werner Randelshofer
 * @version 1.0  04 January 2005  Created.
 */
public class AboutAction extends AbstractApplicationAction {
    public final static String ID = "about";
    
    /** Creates a new instance. */
    public AboutAction(Application app) {
        super(app);
        ResourceBundleUtil labels = ResourceBundleUtil.getLAFBundle("org.jhotdraw.app.Labels");
        labels.configureAction(this, ID);
        }
    
    //NEIL: Add versions of batik to show it works
    public void actionPerformed(ActionEvent evt) {
        Application app = getApplication();
        JOptionPane.showMessageDialog(app.getComponent(),
                app.getName()+" "+app.getVersion()+"\n"+app.getCopyright()+
                "\n\nRunning on Java "+System.getProperty("java.vm.version")+
                ", "+System.getProperty("java.vendor")+
                "\n\nBatik version " + Version.version +
                "\n\nThis about message is created in drawjhotdraw", 
                "About", JOptionPane.PLAIN_MESSAGE);
    }
}
