/*
 * @(#)MailUserBean.java	1.6 03/01/10
 *
 * Copyright 2001-2003 Sun Microsystems, Inc. All Rights Reserved.
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
 *
 */

package demo;

import java.util.*;
import javax.mail.*;
import javax.naming.*;

/**
 * This JavaBean is used to store mail user information.
 */
public class MailUserBean {
    private Folder folder;
    private String hostname;
    private String username;
    private String password;
    private Session session;
    private Store store;
    private URLName url;
    private String protocol = "imap";
    private String mbox = "INBOX";	

    public MailUserBean(){}

    /**
     * Returns the javax.mail.Folder object.
     */
    public Folder getFolder() {
        return folder;
    }
    
    /**
     * Returns the number of messages in the folder.
     */
    public int getMessageCount() throws MessagingException {
        return folder.getMessageCount();
    }

    /**
     * hostname getter method.
     */
    public String getHostname() {
        return hostname;
    }
    
    /**
     * hostname setter method.
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
	
    /**
     * username getter method.
     */
    public String getUsername() {
        return username;
    }

    /**
     * username setter method.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * password getter method.
     */
    public String getPassword() {
        return password;
    }

    /**
     * password setter method.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * session getter method.
     */
    public Session getSession() {
        return session;
    }

    /**
     * session setter method.
     */
    public void setSession(Session s) {
        this.session = session;
    }

    /**
     * store getter method.
     */
    public Store getStore() {
        return store;
    }

    /**
     * store setter method.
     */
    public void setStore(Store store) {
        this.store = store;
    }

    /**
     * url getter method.
     */
    public URLName getUrl() {
        return url;
    }

    /**
     * Method for checking if the user is logged in.
     */
    public boolean isLoggedIn() {
        return store.isConnected();
    }
      
    /**
     * Method used to login to the mail host.
     */
    public void login() throws Exception {
        url = new URLName(protocol, getHostname(), -1, mbox, 
                          getUsername(), getPassword());
	/*
	 * First, try to get the session from JNDI,
	 * as would be done under J2EE.
	 */
	try {
	    InitialContext ic = new InitialContext();
	    Context ctx = (Context)ic.lookup("java:comp/env");
	    session = (Session)ctx.lookup("MySession");
	} catch (Exception ex) {
	    // ignore it
	}

	// if JNDI fails, try the old way that should work everywhere
	if (session == null) {
	    Properties props = null;
	    try {
		props = System.getProperties();
	    } catch (SecurityException sex) {
		props = new Properties();
	    }
	    session = Session.getInstance(props, null);
	}
        store = session.getStore(url);
        store.connect();
        folder = store.getFolder(url);
        
        folder.open(Folder.READ_WRITE);
    }

    /**
     * Method used to login to the mail host.
     */
    public void login(String hostname, String username, String password) 
        throws Exception {
            
        this.hostname = hostname;
        this.username = username;
        this.password = password;
	    
        login();
    }

    /**
     * Method used to logout from the mail host.
     */
    public void logout() throws MessagingException {
        folder.close(false);
        store.close();
        store = null;
        session = null;
    }
}

