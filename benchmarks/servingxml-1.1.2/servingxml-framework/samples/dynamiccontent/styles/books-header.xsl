<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE xsl:stylesheet SYSTEM "entities.def">

<xsl:transform
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="2.0"
>

<xsl:import href="title-bar.xsl"/>
<xsl:import href="footer.xsl"/>
<xsl:import href="category-list.xsl"/>

<xsl:output method="html" media-type="text/html"/>

<xsl:template name="books-header">
<xsl:param name="selected-category"/>
<TR>
<TD>&nbsp;&nbsp;</TD>
<TD>
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0" WIDTH="100%">
<TR>
    <TD WIDTH="100%"></TD>
        <TD ALIGN="right" VALIGN="middle" NOWRAP="true" WIDTH="230" ><SPAN CLASS="smalltext">View Categories:&nbsp;&nbsp;</SPAN></TD>
        <FORM><TD ALIGN="right" VALIGN="top" width="100%"><SPAN CLASS="text">
        <xsl:call-template name="category-list">
            <xsl:with-param name="selected-category" select="$selected-category"/>
        </xsl:call-template>
        </SPAN></TD></FORM>
</TR>
</TABLE>
</TD>
</TR>
</xsl:template>

</xsl:transform>        
