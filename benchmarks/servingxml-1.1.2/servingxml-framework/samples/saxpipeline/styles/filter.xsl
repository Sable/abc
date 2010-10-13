<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   version="2.0">

<xsl:strip-space elements="head body"/> 
   
<xsl:template match="html | head | title | body | p | ul | li | b | i" priority="2">
  <xsl:copy>
  <xsl:copy-of select="@*"/>
  <xsl:apply-templates/>
  </xsl:copy>   
</xsl:template>

<xsl:template match="*" priority="1"/>

</xsl:stylesheet>

