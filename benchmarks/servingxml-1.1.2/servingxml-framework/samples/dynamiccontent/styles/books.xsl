<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE xsl:stylesheet SYSTEM "entities.def">

<xsl:transform
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="2.0"           
>

<xsl:import href="title-bar.xsl"/>
<xsl:import href="footer.xsl"/>
<xsl:import href="books-header.xsl"/>

<xsl:output method="html" media-type="text/html"/>

<xsl:param name="category" select="''"/>

<xsl:template match="/">

 <HTML>

   <xsl:call-template name="title-bar">
       <xsl:with-param name="title" select="'Presenting Books'"/>
   </xsl:call-template>

   <BODY BGCOLOR="#FFFFFF" LEFTMARGIN="0" TOPMARGIN="0">
   <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" WIDTH="760">
   <TR>       
      <TD>&nbsp;&nbsp;</TD>      
<TD>
<H1 CLASS="titlefont">Presenting Books</H1>
</TD>
</TR>
<TR>
<TD BGCOLOR="#424242" COLSPAN="2">&nbsp; </TD> 
<TD>
<TR>
  
<TD>
&nbsp;
</TD>
</TR>
<xsl:call-template name="books-header">
<xsl:with-param name="selected-category" select="$category"/>
</xsl:call-template>
<TR>

    <xsl:message>***books category=<xsl:value-of select="$category"/></xsl:message>
<TD>&nbsp;&nbsp;</TD>
<TD VALIGN="TOP">
  <TABLE>
    <TR><TD COLSPAN="3">&nbsp;</TD></TR>  
    <xsl:apply-templates select="books"/>
  </TABLE>    
</TD>
</TR>
</TD>
</TR>
</TABLE>
<TR>
<TD COLSPAN="1">&nbsp; </TD> 
</TR>
<xsl:call-template name="footer"/>
</BODY>
</HTML>
</xsl:template>

<xsl:template match="book">
  <TR>
    <td>&nbsp;</td>
   <TD VALIGN="TOP" WIDTH="200"><xsl:value-of select="author"/></TD>
   <TD VALIGN="TOP" WIDTH="290"><B><xsl:value-of select="title"/></B></TD>   
  </TR>  
</xsl:template>

<xsl:template match="books">
  <xsl:apply-templates select="book">
   <xsl:sort select="author"/>  
  </xsl:apply-templates>
</xsl:template>

</xsl:transform>        
