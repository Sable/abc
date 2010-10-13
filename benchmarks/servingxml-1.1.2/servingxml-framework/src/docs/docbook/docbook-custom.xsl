<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE xsl:stylesheet [
        <!ENTITY nbsp "&#160;">
]>
<xsl:transform 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="2.0">
        
<xsl:import href="file:///C:/tools/docbook/docbook-xsl-1.74.0/html/docbook.xsl"/>

<xsl:param name="generate.toc" select="'article toc'"/> 
<xsl:param name="toc.max.depth" select="'2'"/>
<xsl:param name="toc.section.depth" select="'2'"/>

<xsl:param name="index.prefer.titleabbrev" select="1"/>
<!--
<xsl:param name="toc.max.depth" select="'0'"/>
<xsl:param name="toc.section.depth" select="'0'"/>
-->
</xsl:transform>
