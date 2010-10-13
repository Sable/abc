<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:inv="http://www.telio.be/ns/2002/invoice">

  <xsl:output indent="yes"/>
  <xsl:variable name="logo-url">logo.svg</xsl:variable>

  <!--================================================================-->
  <!-- Root Element Template                                          -->
  <!-- This template specifies what creates the root element of the   -->
  <!-- result tree.  In this case, it tells the XSL processor to      -->
  <!-- start with the <timesheet> element.                            -->
  <!--================================================================-->

  <!-- adds border all around a formatting object -->
  <xsl:attribute-set name="border-all">
  	<xsl:attribute name="border-bottom-width">0.1mm</xsl:attribute>
  	<xsl:attribute name="border-bottom-color">black</xsl:attribute>
  	<xsl:attribute name="border-bottom-style">solid</xsl:attribute>
  	<xsl:attribute name="border-top-width">0.1mm</xsl:attribute>
  	<xsl:attribute name="border-top-color">black</xsl:attribute>
  	<xsl:attribute name="border-top-style">solid</xsl:attribute>
  	<xsl:attribute name="border-start-width">0.1mm</xsl:attribute>
  	<xsl:attribute name="border-start-color">black</xsl:attribute>
  	<xsl:attribute name="border-start-style">solid</xsl:attribute>
  	<xsl:attribute name="border-end-width">0.1mm</xsl:attribute>
  	<xsl:attribute name="border-end-color">black</xsl:attribute>
  	<xsl:attribute name="border-end-style">solid</xsl:attribute>
  </xsl:attribute-set>

  <xsl:template match="/">

  <!--================================================================-->
  <!-- The fo:root element contains the entire document.              -->
  <!--================================================================-->

    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <!--================================================================-->
  <!-- The layout-master-set defines a set of page layouts.  For our  -->
  <!-- purposes here, we only need one.                               -->
  <!--================================================================-->

      <fo:layout-master-set>
        <fo:simple-page-master
	      page-height="29.7cm" 
  		  page-width="21cm"
          master-name="main"
		  margin-top="0cm"
    	  margin-bottom="1cm"
		  margin-left="0cm"
    	  margin-right="0cm">

  <!--================================================================-->
  <!-- The region-body element is where all the action is.  We'll put -->
  <!-- all of our content into this space.                            -->
  <!--================================================================-->

          <fo:region-body 
          margin-top="2cm"
    	  margin-bottom="0cm"
		  margin-left="2.5cm"
    	  margin-right="2.5cm"/>
		  <fo:region-before overflow="visible"/>
		  <fo:region-after margin-right="0.5cm" margin-left="0.5cm"
		  	extent="20mm" overflow="visible"/>
        </fo:simple-page-master>
      </fo:layout-master-set>

	  <xsl:apply-templates/>

    </fo:root>
  </xsl:template>

  <xsl:template match="*" mode="company-header">
	<!-- Left green strip -->
	<fo:block-container background-color="#9EBD0D" height="11cm" width="0.5cm" top="0cm" left="0cm" position="absolute">
      <fo:block text-align="center" font-weight="bold" font-size="8pt" color="#0D2B88"></fo:block>
    </fo:block-container>

	<!-- logo -->
	<fo:block-container height="5cm" width="10cm" top="0.3cm" left="1cm" position="absolute">
		<fo:block>
			<fo:external-graphic content-height="2cm" content-width="5cm" src="{$logo-url}"/>
		</fo:block>
	</fo:block-container>
  </xsl:template>

  <xsl:template match="*" mode="company-footer">
	<fo:block-container height="1cm" width="19.5cm" top="0.5cm" left="1cm" position="absolute">
		<fo:block text-align="center" font-weight="bold" font-size="8pt" color="#0D2B88">Footer</fo:block>
	</fo:block-container>
  </xsl:template>
  		
  <xsl:template match="text()"/>
  <xsl:template match="text()" mode="company-header"/>
  <xsl:template match="text()" mode="company-footer"/>

</xsl:stylesheet>
