/*
 * @(#)FilterServlet.java	1.3 02/12/20
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
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This servlet is used to determine whether the user is logged in before
 * forwarding the request to the selected URL.
 */
public class FilterServlet extends HttpServlet {

    /**
     * This method handles the "POST" submission from two forms: the
     * login form and the message compose form.
     */
    public void doPost(HttpServletRequest request, 
                       HttpServletResponse  response) 
                       throws IOException, ServletException {

        String servletPath = request.getServletPath();
        servletPath = servletPath.concat(".jsp");
        
        getServletConfig().getServletContext().
            getRequestDispatcher("/" + servletPath).forward(request, response);
    }

    /**
     * This method handles the GET requests from the client.
     */
    public void doGet(HttpServletRequest request, 
                      HttpServletResponse  response)
                      throws IOException, ServletException {
      
        // check to be sure we're still logged in 
        // before forwarding the request.
        HttpSession session = request.getSession();
        MailUserBean mailuser = (MailUserBean)session.getAttribute("mailuser");
        String servletPath = request.getServletPath();
        servletPath = servletPath.concat(".jsp");
        
        if (mailuser.isLoggedIn())
            getServletConfig().getServletContext().
                getRequestDispatcher("/" + servletPath).
                forward(request, response);
        else
            getServletConfig().getServletContext().
                getRequestDispatcher("/index.html").
                forward(request, response);
    }
}

