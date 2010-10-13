<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:inv="http://www.telio.be/ns/2002/invoice"
                version="2.0">

<xsl:output indent="yes"/>
  
<!-- include client address -->

<xsl:template match="inv:client">
<xsl:variable name="id" select="@id"/>
<xsl:variable name="client" select="document('clients.xml')/inv:clients/inv:client[@id=$id]"/>
<xsl:copy-of select="$client">
</xsl:copy-of>
</xsl:template>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>