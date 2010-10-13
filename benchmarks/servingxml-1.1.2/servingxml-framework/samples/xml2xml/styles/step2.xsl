<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:inv="http://www.telio.be/ns/2002/invoice"
                version="2.0">

<xsl:output indent="yes"/>

<!-- adds tax subtotals -->

<xsl:key name="tax" match="inv:invoice/inv:item" use="inv:tax"/>

<xsl:template match="inv:invoice">
<xsl:copy>
  <xsl:apply-templates select="@*"/>
  <xsl:apply-templates/>
  <xsl:variable name="items" select="inv:item" />
  <xsl:for-each select="$items">
	  <xsl:variable name="taxrate" select="inv:tax" />
	  <xsl:if test="generate-id(.)=generate-id($items[inv:tax=$taxrate])">
		<xsl:variable name="taxexc">
			<xsl:value-of select="sum($items[inv:tax=$taxrate]/inv:itemtotal)"/>
		</xsl:variable>
		<xsl:variable name="tax" select="format-number(number(inv:tax) * number($taxexc) div 100,'0.00')"/>
  		<xsl:variable name="taxinc" select="number($tax)+number($taxexc)"/>
  		<inv:taxtotal>
 	  	 <inv:taxrate><xsl:value-of select="inv:tax"/></inv:taxrate>
  		 <inv:taxexcluded><xsl:value-of select="$taxexc"/></inv:taxexcluded>
  		 <inv:tax><xsl:value-of select="$tax"/></inv:tax>
    	 <inv:taxincluded><xsl:value-of select="$taxinc"/></inv:taxincluded>
	  </inv:taxtotal>
	  </xsl:if>
  </xsl:for-each>
</xsl:copy>
</xsl:template>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>