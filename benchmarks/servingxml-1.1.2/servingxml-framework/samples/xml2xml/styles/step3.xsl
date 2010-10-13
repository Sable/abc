<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:inv="http://www.telio.be/ns/2002/invoice"
                version="2.0">

<xsl:output indent="yes"/>
  
<!-- adds grand total -->

<xsl:template match="inv:invoice">
<xsl:copy>
  <xsl:apply-templates select="@*"/>
  <xsl:apply-templates/>
  <inv:grandtotal>
  <inv:totalincluded><xsl:value-of select="sum(inv:taxtotal/inv:taxincluded)"/></inv:totalincluded>
  <inv:totalexcluded><xsl:value-of select="sum(inv:taxtotal/inv:taxexcluded)"/></inv:totalexcluded>
  <inv:totaltax><xsl:value-of select="sum(inv:taxtotal/inv:tax)"/></inv:totaltax>
  </inv:grandtotal>
</xsl:copy>
</xsl:template>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>