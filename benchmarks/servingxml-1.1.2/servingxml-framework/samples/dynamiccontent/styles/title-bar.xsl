<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY nbsp "&#160;">
]>

<xsl:transform 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="2.0">
 
 <xsl:template name="title-bar">
   <xsl:param name="title" select="'Default Title'"/>
   <HEAD>
   <TITLE><xsl:value-of select="$title"/></TITLE>
   <LINK REL="stylesheet" TYPE="text/css" HREF="../css/style.css"/>
<SCRIPT LANGUAGE="JavaScript">

  function popupWindow(url){
    popup =  window.open(url,"","toolbar=no,directories=no,scrollbars=yes,menubar=no,resizable=yes,width=600,height=500")
    popup.focus();
  }</SCRIPT>
   </HEAD>
 </xsl:template>
</xsl:transform>
