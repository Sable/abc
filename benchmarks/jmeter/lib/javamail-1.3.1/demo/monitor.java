/*
 * @(#)monitor.java	1.8 03/04/22
 *
 * Copyright 1996-2003 Sun Microsystems, Inc. All Rights Reserved.
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

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.event.*;
import javax.activation.*;

/* Monitors given mailbox for new mail */

public class monitor {

    public static void main(String argv[])
    {
	if (argv.length != 5) {
  	   System.out.println("Usage: monitor <host> <user> <password> <mbox> <freq>");
	   System.exit(1);
	}
	System.out.println("\nTesting monitor\n");

        try {
	    Properties props = System.getProperties();

	    // Get a Session object
	    Session session = Session.getInstance(props, null);
	    // session.setDebug(true);

	    // Get a Store object
	    Store store = session.getStore("imap");

	   // Connect
	    store.connect(argv[0], argv[1], argv[2]);

	   // Open a Folder
	    Folder folder = store.getFolder(argv[3]);
	    if (folder == null || !folder.exists()) {
		System.out.println("Invalid folder");
		System.exit(1);
	    }

	    folder.open(Folder.READ_WRITE);

	    // Add messageCountListener to listen for new messages
	    folder.addMessageCountListener(new MessageCountAdapter() {
		public void messagesAdded(MessageCountEvent ev) {
		    Message[] msgs = ev.getMessages();
		    System.out.println("Got " + msgs.length + " new messages");

		    // Just dump out the new messages
		    for (int i = 0; i < msgs.length; i++) {
			try {
			    DataHandler dh = msgs[i].getDataHandler();
			    InputStream is = dh.getInputStream();
			    int c;
			    while ((c = is.read()) != -1)
			 	System.out.write(c); 
			} catch (IOException ioex) { 
			    ioex.printStackTrace();	
			} catch (MessagingException mex) {
			    mex.printStackTrace();
			}
		    }
		}
	    });
			
	   // Check mail once in "freq" MILLIseconds

	    int freq = Integer.parseInt(argv[4]);

	    for (; ;) {
		Thread.sleep(freq); // sleep for freq milliseconds

		// This is to force the IMAP server to send us
		// EXISTS notifications. 
		folder.getMessageCount();
	    }

	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
