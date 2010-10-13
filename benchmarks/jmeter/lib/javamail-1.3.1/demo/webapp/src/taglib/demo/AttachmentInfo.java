/*
 * @(#)AttachmentInfo.java	1.3 02/04/04
 *
 * Copyright 2001-2002 Sun Microsystems, Inc. All Rights Reserved.
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

import java.io.*;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Used to store attachment information.
 */
public class AttachmentInfo {
    private Part part;
    private int num;
    

    /**
     * Returns the attachment's content type.
     */
    public String getAttachmentType() throws MessagingException {
        String contentType;
        if ((contentType = part.getContentType()) == null)
            return "invalid part";
        else
	    return contentType;
    }

    /**
     * Returns the attachment's content (if it is plain text).
     */
    public String getContent() throws java.io.IOException, MessagingException {
        if (hasMimeType("text/plain"))
            return (String)part.getContent();
        else
            return "";
    }
    
    /**
     * Returns the attachment's description.
     */
    public String getDescription() throws MessagingException {
        String description;
        if ((description = part.getDescription()) != null)
            return description;
        else 
            return "";
    }
    
    /**
     * Returns the attachment's filename.
     */
    public String getFilename() throws MessagingException {
        String filename;
        if ((filename = part.getFileName()) != null)
            return filename;
        else
            return "";
    }

    /**
     * Returns the attachment number.
     */
    public String getNum() {
        return (Integer.toString(num));
    }
    
    /**
     * Method for checking if the attachment has a description.
     */
    public boolean hasDescription() throws MessagingException {
        return (part.getDescription() != null);
    }
    
    /**
     * Method for checking if the attachment has a filename.
     */
    public boolean hasFilename() throws MessagingException {
        return (part.getFileName() != null);
    }
    
    /**
     * Method for checking if the attachment has the desired mime type.
     */
    public boolean hasMimeType(String mimeType) throws MessagingException {
        return part.isMimeType(mimeType);
    }
    
    /**
     * Method for checking the content disposition.
     */
    public boolean isInline() throws MessagingException {
        if (part.getDisposition() != null)
            return part.getDisposition().equals(Part.INLINE);
        else
            return true;
    }
    
    /**
     * Method for mapping a message part to this AttachmentInfo class.
     */
    public void setPart(int num, Part part) 
        throws MessagingException, ParseException {
            
        this.part = part;
        this.num = num;
    }
}

