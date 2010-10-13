<%--
 % @(#)compose.jsp	1.1 01/05/14
 %
 % Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 %
 % Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 % modify and redistribute this software in source and binary code form,
 % provided that i) this copyright notice and license appear on all copies of
 % the software; and ii) Licensee does not utilize the software in a manner
 % which is disparaging to Sun.
 %
 % This software is provided "AS IS," without a warranty of any kind. ALL
 % EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 % IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 % NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 % LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 % OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 % LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 % INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 % CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 % OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 % POSSIBILITY OF SUCH DAMAGES.
 %
 % This software is not designed or intended for use in on-line control of
 % aircraft, air traffic, aircraft navigation or aircraft communications; or in
 % the design, construction, operation or maintenance of any nuclear
 % facility. Licensee represents and warrants that it will not use or
 % redistribute the Software for such purposes.
 %
--%>

<%@ page language="java" %>
<%@ page errorPage="errorpage.jsp" %>

<html>
<head>
	<title>JavaMail compose</title>
</head>

<body bgcolor="#ccccff">
<form ACTION="send" METHOD=POST>
<input type="hidden" name="send" value="send">
<p align="center">
<b><font size="4" face="Verdana, Arial, Helvetica">
JavaMail Compose Message</font></b>
<p>
<table border="0" width="100%">
<tr>
<td width="16%" height="22">	
<p align="right">
<b><font face="Verdana, Arial, Helvetica">To:</font></b></td>
<td width="84%" height="22">
<% if (request.getParameter("to") != null) { %>
<input type="text" name="to" value="<%= request.getParameter("to") %>" size="30">
<% } else { %>
<input type="text" name="to" size="30"> 
<% } %>
<font size="1" face="Verdana, Arial, Helvetica">
 (separate addresses with commas)</font></td></tr>
<tr>
<td width="16%"><p align="right">
<b><font face="Verdana, Arial, Helvetica">From:</font></b></td>
<td width="84%">
<input type="text" name="from" size="30"> 
<font size="1" face="Verdana, Arial, Helvetica">
 (separate addresses with commas)</font></td></tr>
<tr>
<td width="16%"><p align="right">
<b><font face="Verdana, Arial, Helvetica">Subject:</font></b></td>
<td width="84%">
<input type="text" name="subject" size="55"></td></tr>
<tr>
<td width="16%">&nbsp;</td>
<td width="84%"><textarea name="text" rows="15" cols="53"></textarea></td></tr>
<tr>
<td width="16%" height="32">&nbsp;</td>
<td width="84%" height="32">
<input type="submit" name="Send" value="Send">
<input type="reset" name="Reset" value="Reset"></td></tr>
</table>
</form>
</body>
</html>

