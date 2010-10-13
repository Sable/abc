/*
 * @(#)msgsend.java	1.17 03/04/22
 *
 * Copyright 1997-2003 Sun Microsystems, Inc. All Rights Reserved.
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

import java.io.*;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Date;

import javax.mail.*;
import javax.mail.internet.*;

/**
 * Demo app that shows how to construct and send an RFC822
 * (singlepart) message.
 *
 * XXX - allow more than one recipient on the command line
 *
 * @author Max Spivak
 * @author Bill Shannon
 */

public class msgsend {

    public static void main(String[] argv) {
	new msgsend(argv);
    }

    public msgsend(String[] argv) {

	String  to, subject = null, from = null, 
		cc = null, bcc = null, url = null;
	String mailhost = null;
	String mailer = "msgsend";
	String protocol = null, host = null, user = null, password = null;
	String record = null;	// name of folder in which to record mail
	boolean debug = false;
	BufferedReader in =
			new BufferedReader(new InputStreamReader(System.in));
	int optind;

	for (optind = 0; optind < argv.length; optind++) {
	    if (argv[optind].equals("-T")) {
		protocol = argv[++optind];
	    } else if (argv[optind].equals("-H")) {
		host = argv[++optind];
	    } else if (argv[optind].equals("-U")) {
		user = argv[++optind];
	    } else if (argv[optind].equals("-P")) {
		password = argv[++optind];
	    } else if (argv[optind].equals("-M")) {
		mailhost = argv[++optind];
	    } else if (argv[optind].equals("-f")) {
		record = argv[++optind];
	    } else if (argv[optind].equals("-s")) {
		subject = argv[++optind];
	    } else if (argv[optind].equals("-o")) { // originator
		from = argv[++optind];
	    } else if (argv[optind].equals("-c")) {
		cc = argv[++optind];
	    } else if (argv[optind].equals("-b")) {
		bcc = argv[++optind];
	    } else if (argv[optind].equals("-L")) {
		url = argv[++optind];
	    } else if (argv[optind].equals("-d")) {
		debug = true;
	    } else if (argv[optind].equals("--")) {
		optind++;
		break;
	    } else if (argv[optind].startsWith("-")) {
		System.out.println(
"Usage: msgsend [[-L store-url] | [-T prot] [-H host] [-U user] [-P passwd]]");
		System.out.println(
"\t[-s subject] [-o from-address] [-c cc-addresses] [-b bcc-addresses]");
		System.out.println(
"\t[-f record-mailbox] [-M transport-host] [-d] [address]");
		System.exit(1);
	    } else {
		break;
	    }
	}

	try {
	    if (optind < argv.length) {
		// XXX - concatenate all remaining arguments
		to = argv[optind];
		System.out.println("To: " + to);
	    } else {
		System.out.print("To: ");
		System.out.flush();
		to = in.readLine();
	    }
	    if (subject == null) {
		System.out.print("Subject: ");
		System.out.flush();
		subject = in.readLine();
	    } else {
		System.out.println("Subject: " + subject);
	    }

	    Properties props = System.getProperties();
	    // XXX - could use Session.getTransport() and Transport.connect()
	    // XXX - assume we're using SMTP
	    if (mailhost != null)
		props.put("mail.smtp.host", mailhost);

	    // Get a Session object
	    Session session = Session.getInstance(props, null);
	    if (debug)
		session.setDebug(true);

	    // construct the message
	    Message msg = new MimeMessage(session);
	    if (from != null)
		msg.setFrom(new InternetAddress(from));
	    else
		msg.setFrom();

	    msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
	    if (cc != null)
		msg.setRecipients(Message.RecipientType.CC,
					InternetAddress.parse(cc, false));
	    if (bcc != null)
		msg.setRecipients(Message.RecipientType.BCC,
					InternetAddress.parse(bcc, false));

	    msg.setSubject(subject);

	    collect(in, msg);

	    msg.setHeader("X-Mailer", mailer);
	    msg.setSentDate(new Date());

	    // send the thing off
	    Transport.send(msg);

	    System.out.println("\nMail was sent successfully.");

	    // Keep a copy, if requested.

	    if (record != null) {
		// Get a Store object
		Store store = null;
		if (url != null) {
		    URLName urln = new URLName(url);
		    store = session.getStore(urln);
		    store.connect();
		} else {
		    if (protocol != null)		
			store = session.getStore(protocol);
		    else
			store = session.getStore();

		    // Connect
		    if (host != null || user != null || password != null)
			store.connect(host, user, password);
		    else
			store.connect();
		}

		// Get record Folder.  Create if it does not exist.
		Folder folder = store.getFolder(record);
		if (folder == null) {
		    System.err.println("Can't get record folder.");
		    System.exit(1);
		}
		if (!folder.exists())
		    folder.create(Folder.HOLDS_MESSAGES);

		Message[] msgs = new Message[1];
		msgs[0] = msg;
		folder.appendMessages(msgs);

		System.out.println("Mail was recorded successfully.");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void collect(BufferedReader in, Message msg)
					throws MessagingException, IOException {
	String line;
	StringBuffer sb = new StringBuffer();
	while ((line = in.readLine()) != null) {
	    sb.append(line);
	    sb.append("\n");
	}

	// If the desired charset is known, you can use
	// setText(text, charset)
	msg.setText(sb.toString());
    }
}
