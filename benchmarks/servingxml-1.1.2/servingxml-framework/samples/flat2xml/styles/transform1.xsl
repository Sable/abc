<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" 
              encoding="iso-8859-1" 
              indent="no"/>  
  <xsl:template match="/">
   <foo>
    <xsl:for-each select="//element1">
      insert into element1 (attribute1) values ('<xsl:value-of select="@attribute1"/>');
    </xsl:for-each>
   </foo>
  </xsl:template>
</xsl:stylesheet>