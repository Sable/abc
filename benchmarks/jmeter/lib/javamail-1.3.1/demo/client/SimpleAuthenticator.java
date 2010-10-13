/*
 * @(#)SimpleAuthenticator.java	1.7 01/05/23
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
/*
 * @(#)SimpleAuthenticator.java	1.7 01/05/23
 *
 * Copyright (c) 1996-1998 by Sun Microsystems, Inc.
 * All Rights Reserved.
 */

import javax.mail.*;
import java.net.InetAddress;
import java.awt.*;
import javax.swing.*;

/**
 * Simple Authenticator for requesting password information.
 *
 * @version	1.7, 01/05/23
 * @author	Christopher Cotton
 * @author	Bill Shannon
 */

public class SimpleAuthenticator extends Authenticator {

    Frame frame;
    String username;
    String password;

    public SimpleAuthenticator(Frame f) {
	this.frame = f;
    }

    protected PasswordAuthentication getPasswordAuthentication() {

	// given a prompt?
	String prompt = getRequestingPrompt();
	if (prompt == null)
	    prompt = "Please login...";

	// protocol
	String protocol = getRequestingProtocol();
	if (protocol == null)
	    protocol = "Unknown protocol";

	// get the host
	String host = null;
	InetAddress inet = getRequestingSite();
	if (inet != null)
	    host = inet.getHostName();
	if (host == null)
	    host = "Unknown host";

	// port
	String port = "";
	int portnum = getRequestingPort();
	if (portnum != -1)
	    port = ", port " + portnum + " ";

	// Build the info string
	String info = "Connecting to " + protocol + " mail service on host " +
								host + port;

	//JPanel d = new JPanel();
	// XXX - for some reason using a JPanel here causes JOptionPane
	// to display incorrectly, so we workaround the problem using
	// an anonymous JComponent.
	JComponent d = new JComponent() { };

	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	d.setLayout(gb);
	c.insets = new Insets(2, 2, 2, 2);

	c.anchor = GridBagConstraints.WEST;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weightx = 0.0;
	d.add(constrain(new JLabel(info), gb, c));
	d.add(constrain(new JLabel(prompt), gb, c));

	c.gridwidth = 1;
	c.anchor = GridBagConstraints.EAST;
	c.fill = GridBagConstraints.NONE;
	c.weightx = 0.0;
	d.add(constrain(new JLabel("Username:"), gb, c));

	c.anchor = GridBagConstraints.EAST;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weightx = 1.0;
	String user = getDefaultUserName();
	JTextField username = new JTextField(user, 20);
	d.add(constrain(username, gb, c));

	c.gridwidth = 1;
	c.fill = GridBagConstraints.NONE;
	c.anchor = GridBagConstraints.EAST;
	c.weightx = 0.0;
	d.add(constrain(new JLabel("Password:"), gb, c));

	c.anchor = GridBagConstraints.EAST;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weightx = 1.0;
	JPasswordField password = new JPasswordField("", 20);
	d.add(constrain(password, gb, c));
	// XXX - following doesn't work
	if (user != null && user.length() > 0)
	    password.requestFocus();
	else
	    username.requestFocus();
	
	int result = JOptionPane.showConfirmDialog(frame, d, "Login",
	    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	
	if (result == JOptionPane.OK_OPTION)
	    return new PasswordAuthentication(username.getText(),
						password.getText());
	else
	    return null;
    }

    private Component constrain(Component cmp,
        			GridBagLayout gb, GridBagConstraints c) {
	gb.setConstraints(cmp, c);
	return (cmp);
    }
}
