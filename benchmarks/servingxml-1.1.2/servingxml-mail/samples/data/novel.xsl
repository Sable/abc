<?xml version="1.0" encoding="iso-8859-1"?>

<!--
This is modeled after the novel-fo.xsl stylesheet on the apache cocoon site
-->

<xsl:stylesheet version="2.0"                                                   
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">
  
  <xsl:output method="xml" media-type="text/xslfo"/>

  <xsl:template match="novel">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
      <fo:simple-page-master
        master-name="right"
        margin-top="75pt"
        margin-bottom="25pt"
        margin-left="100pt"
        margin-right="50pt">
        <fo:region-body margin-bottom="50pt"/>
        <fo:region-after extent="25pt"/>
      </fo:simple-page-master>
      <fo:simple-page-master
        master-name="left"
        margin-top="75pt"
        margin-bottom="25pt"
        margin-left="50pt"
        margin-right="100pt">
        <fo:region-body margin-bottom="50pt"/>
        <fo:region-after extent="25pt"/>
      </fo:simple-page-master>
      <fo:page-sequence-master master-name="odd-even-sequence">
        <fo:repeatable-page-master-alternatives>
          <fo:conditional-page-master-reference master-reference="right" page-position="first"/>
          <fo:conditional-page-master-reference master-reference="right" odd-or-even="even"/>
          <fo:conditional-page-master-reference master-reference="left" odd-or-even="odd"/>
          <!-- recommended fallback procedure -->
          <fo:conditional-page-master-reference master-reference="right"/>
        </fo:repeatable-page-master-alternatives>
      </fo:page-sequence-master>
      </fo:layout-master-set>

      <fo:page-sequence master-reference="odd-even-sequence">

        <fo:static-content flow-name="xsl-region-after">
          <fo:block text-align-last="center" font-size="10pt"><fo:page-number/></fo:block>
        </fo:static-content>

        <fo:flow flow-name="xsl-region-body">
          <xsl:apply-templates/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <xsl:template match="front/title">
    <fo:block font-size="36pt" text-align-last="center" space-before.optimum="24pt"><xsl:apply-templates/></fo:block>
  </xsl:template>

  <xsl:template match="author">
    <fo:block font-size="24pt" text-align-last="center" space-before.optimum="24pt"><xsl:apply-templates/></fo:block>
  </xsl:template>

  <xsl:template match="revision-list">
  </xsl:template>

  <xsl:template match="chapter">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="chapter/title">
    <fo:block font-size="24pt" text-align-last="center" space-before.optimum="24pt"><xsl:apply-templates/></fo:block>
  </xsl:template>

  <xsl:template match="para">
    <fo:block font-size="12pt" space-before.optimum="12pt" text-align="justify"><xsl:apply-templates/></fo:block>
  </xsl:template>
</xsl:stylesheet>
