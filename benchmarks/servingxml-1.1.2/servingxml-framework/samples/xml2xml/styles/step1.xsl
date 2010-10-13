<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:inv="http://www.telio.be/ns/2002/invoice"
                version="2.0">

<!-- adds item totals -->

<xsl:template match="inv:item">
<xsl:copy>
  <xsl:apply-templates select="@*"/>
  <xsl:apply-templates/>
<inv:itemtotal><xsl:value-of select="format-number(number(inv:quantity)*number(inv:unitprice),'0.00')"/></inv:itemtotal>
</xsl:copy>
</xsl:template>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>