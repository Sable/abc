/*
 * @(#)Main.java  1.0  January 15, 2007
 *
 * Copyright (c) 2007 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */

package org.jhotdraw.samples.odg;

import org.jhotdraw.app.*;

/**
 * Main.
 *
 * @author Werner Randelshofer
 * @version 1.0 January 15, 2007 Created.
 */
public class Main {
    
    /** Creates a new instance. */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	//fix to choose the correct app
    	//may not necessarily be correct, as the app seems to be
    	//written with the assumption of a mac platform.
        Application app;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("mac")) {
            app = new DefaultOSXApplication();
        } else if (os.startsWith("win")) {
          //  app = new DefaultMDIApplication();
            app = new DefaultSDIApplication();
        } else {
            app = new DefaultSDIApplication();
        }
        ApplicationModel appModel = new ODGApplicationModel();
        app.setModel(appModel);
        app.launch(args);
        // TODO code application logic here
    }
    
}
