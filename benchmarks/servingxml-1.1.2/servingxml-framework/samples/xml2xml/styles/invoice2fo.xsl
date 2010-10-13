<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:inv="http://www.telio.be/ns/2002/invoice">

  <xsl:import href="invoice-master.xsl"/>
  
  <xsl:output indent="yes"/>
  
  <xsl:param name="paperhead">true</xsl:param>
  <xsl:variable name="company" select="document('company.xml')/inv:company"/>

  <xsl:template match="inv:invoice">
      
      <fo:page-sequence master-reference="main">

      <!-- add static content -->
  	  <xsl:if test="$paperhead='true'">
		  <fo:static-content flow-name="xsl-region-before">
			<xsl:apply-templates select="." mode="company-header"/>
		  </fo:static-content>
		  <fo:static-content flow-name="xsl-region-after">
			<xsl:apply-templates select="." mode="company-footer"/>
		  </fo:static-content>
	  </xsl:if>
	  
      <!-- main part -->
            <fo:flow flow-name="xsl-region-body">
                
                <fo:block-container height="4cm" width="12cm" top="3cm" left="10cm" position="absolute">
                    <fo:block text-align="start" font-family="sans-serif" font-weight="bold" font-size="10pt">
    					<xsl:value-of select="inv:client/inv:company"/>
    				</fo:block>
                    <fo:block text-align="start" font-family="sans-serif"  font-size="10pt">
                        Att. <xsl:value-of select="inv:client/inv:contact"/>
    				</fo:block>
                    <xsl:for-each select="inv:client/inv:address">
                    <fo:block text-align="start" font-family="sans-serif"  font-size="10pt">
                    <xsl:value-of select="."/>
                    </fo:block>
                    </xsl:for-each>
                </fo:block-container>

                <fo:block-container height="4cm" width="6cm" top="6.5cm" left="0cm" position="absolute">
                  <fo:table border-collapse="separate" border-color="black" border-style="solid" border-width="0.1mm">
                    <fo:table-column column-width="22.5mm"/>
                    <fo:table-column column-width="35.3mm"/>

                    <fo:table-body font-family="sans-serif" font-weight="normal" font-size="10pt">
                      <fo:table-row>
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid">
                          <fo:block font-weight="bold">Code Client</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid"><fo:block><xsl:value-of select="inv:client/@id"/></fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell padding="1pt"><fo:block font-weight="bold">Doc.</fo:block></fo:table-cell>
                        <fo:table-cell padding="1pt">
                        <fo:block>
							<xsl:choose>
							<xsl:when test="@type='credit'">NOTE DE CREDIT</xsl:when>
							<xsl:otherwise>FACTURE</xsl:otherwise>
							</xsl:choose>
						</fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell padding="1pt"><fo:block font-weight="bold">N°</fo:block></fo:table-cell>
                        <fo:table-cell padding="1pt"><fo:block><xsl:value-of select="@id"/></fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell padding="1pt"><fo:block font-weight="bold">Date</fo:block></fo:table-cell>
                        <fo:table-cell padding="1pt"><fo:block><xsl:value-of select="inv:date"/></fo:block></fo:table-cell>
                      </fo:table-row>
                      <fo:table-row>
                        <fo:table-cell padding="1pt" border-top-width="0.1mm" border-top-color="black" border-top-style="solid"><fo:block font-weight="bold">TVA Client</fo:block></fo:table-cell>
                        <fo:table-cell padding="1pt" border-top-width="0.1mm" border-top-color="black" border-top-style="solid"><fo:block><xsl:value-of select="inv:client/inv:vat"/></fo:block></fo:table-cell>
                      </fo:table-row>
                    </fo:table-body>
                  </fo:table>
                </fo:block-container>

                <fo:block-container height="3cm" width="21cm" top="9.5cm" left="0cm" position="absolute">
                    <fo:block text-align="start" font-family="sans-serif" font-weight="normal" font-size="10pt">
    					Description : <xsl:value-of select="inv:description"/>
    				</fo:block>
				</fo:block-container>

                <fo:block-container height="20cm" width="21cm" top="10cm" left="0cm" position="absolute">
                  <fo:table border-collapse="separate" border-color="black" border-style="solid" border-width="0.1mm">
                    <fo:table-column column-width="19.4mm"/>
                    <fo:table-column column-width="9.5mm"/>
                    <fo:table-column column-width="74.6mm"/>
                    <fo:table-column column-width="15.9mm"/>
                    <fo:table-column column-width="27mm"/>
                    <fo:table-column column-width="9.8mm"/>

                    <fo:table-body font-family="sans-serif" font-weight="normal" font-size="8pt">
                      <fo:table-row padding-left="5pt">
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid"
                                       border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                          <fo:block font-weight="bold">QUANTITE</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid"
                                       border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                          <fo:block font-weight="bold">U.F.</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid"
                                       border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                          <fo:block font-weight="bold">DESIGNATION</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid"
                                       border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                          <fo:block text-align="right" font-weight="bold">PU.NET</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid"
                                       border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                          <fo:block text-align="right" font-weight="bold">MONTANT HTVA</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid">
                          <fo:block text-align="right" font-weight="bold">TVA</fo:block>
                        </fo:table-cell>
                      </fo:table-row>
                      <xsl:apply-templates/>
                    </fo:table-body>
                  </fo:table>
                </fo:block-container>

                <fo:block-container height="20cm" width="21cm" top="16cm" left="6cm" position="absolute">
                  <fo:table border-collapse="separate" border-color="black" border-style="solid" border-width="0.1mm">
                    <fo:table-column column-width="27mm"/>
                    <fo:table-column column-width="14.3mm"/>
                    <fo:table-column column-width="30.2mm"/>
                    <fo:table-column column-width="27.3mm"/>

                    <fo:table-body font-family="sans-serif" font-weight="normal" font-size="10pt">
                      <fo:table-row>
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid"
                                       border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                          <fo:block text-align="center" font-weight="bold">BASE HTVA</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid"
                                       border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                          <fo:block text-align="center" font-weight="bold">TAUX</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid"
                                       border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                          <fo:block text-align="center" font-weight="bold">MONTANT TVA</fo:block>
                        </fo:table-cell>
                        <fo:table-cell padding="1pt" border-bottom-width="0.2mm" border-bottom-color="black" border-bottom-style="solid"
                                       border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                          <fo:block text-align="center" font-weight="bold">TOTAL TVAC</fo:block>
                        </fo:table-cell>
                        </fo:table-row>

                      <xsl:variable name="nbtax" select="count(inv:taxtotal)"/>
                      <xsl:for-each select="inv:taxtotal">
                        <xsl:sort select="inv:taxrate" data-type="number" order="descending"/>
                        <fo:table-row>
                         <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                                        border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                           <fo:block text-align="right" ><xsl:value-of select="format-number(inv:taxexcluded,'0.00')"/> &#8364;</fo:block>
                         </fo:table-cell>
                         <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                                        border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                           <fo:block text-align="right" ><xsl:value-of select="format-number(inv:taxrate,'0')"/>%</fo:block>
                         </fo:table-cell>
                         <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                                        border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                           <fo:block text-align="right" ><xsl:value-of select="format-number(inv:tax,'0.00')"/> &#8364;</fo:block>
                         </fo:table-cell>

                         <xsl:if test="position()=1">
                           <fo:table-cell vertical-align="bottom"
                                          padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                                          border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                             <xsl:attribute name="number-rows-spanned"><xsl:value-of select="$nbtax+1"/></xsl:attribute>
                             <fo:block font-weight="bold" text-align="center" ><xsl:value-of select="format-number(../inv:grandtotal/inv:totalincluded,'0.00')"/> &#8364;</fo:block>
                           </fo:table-cell>
                         </xsl:if>
                         </fo:table-row>
                      </xsl:for-each>

                      <fo:table-row>
                       <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                                      border-top-width="0.1mm" border-top-color="black" border-top-style="solid"
                                      border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                         <fo:block font-weight="bold" text-align="right" ><xsl:value-of select="format-number(inv:grandtotal/inv:totalexcluded,'0.00')"/> &#8364;</fo:block>
                       </fo:table-cell>
                       <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                                      border-top-width="0.1mm" border-top-color="black" border-top-style="solid"
                                      border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                         <fo:block font-weight="bold" text-align="right" ></fo:block>
                       </fo:table-cell>
                       <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                                      border-top-width="0.1mm" border-top-color="black" border-top-style="solid"
                                      border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
                         <fo:block font-weight="bold" text-align="right" ><xsl:value-of select="format-number(inv:grandtotal/inv:totaltax,'0.00')"/> &#8364;</fo:block>
                       </fo:table-cell>

                       </fo:table-row>

                      </fo:table-body>
                  </fo:table>

                  <xsl:if test="@terms='c'">
                  <fo:table>
                    <fo:table-column column-width="98.8mm"/>
					<fo:table-body font-family="sans-serif" font-weight="normal" font-size="10pt">
					<fo:table-row line-height="30pt">
                        <fo:table-cell padding="1pt">
							<fo:block text-align="start" font-family="sans-serif" font-weight="normal" font-size="10pt">
								Conditions de paiement : comptant
							</fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                    </fo:table-body>
				  </fo:table>
				  </xsl:if>
				  
                </fo:block-container>

                <xsl:if test="@terms != 'c'">
                <fo:block-container height="10cm" width="17cm" top="22cm" left="0cm" position="absolute">
                <fo:block text-align="justify" font-size="7pt">
                <fo:inline text-decoration="underline">Conditions générales</fo:inline> : Les factures sont payables au comptant, dans les 30 jours à compter
                de la date de réception. Le défaut de paiment à l'échéance fera courir de plein droit et
                sans sommation un intérêt de 1% par mois à partir de la date de la facture. Dans ce cas,
                le montant de celui-ci sera également majoré de 15% avec un minimum de 150 Eur, à titre
                d'indemnité forfaitaire et irréductible. Toute réclamation, pour être admise,
                doit être faite par lettre recommandée à la poste, dans les quinze jours de la date de
                facturation. Les litiges seront de la compétence des juridictions de Bruxelles.
                </fo:block>
                </fo:block-container>
                </xsl:if>
                </fo:flow>

      </fo:page-sequence>
  </xsl:template>

  <xsl:template match="inv:item">
    <fo:table-row>
      <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                     border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
        <fo:block><xsl:value-of select="inv:quantity"/></fo:block>
      </fo:table-cell>
      <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                     border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
        <fo:block><xsl:value-of select="inv:unit"/></fo:block>
      </fo:table-cell>
      <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                     border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
        <fo:block><xsl:value-of select="inv:description"/></fo:block>
      </fo:table-cell>
      <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                     border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
        <fo:block text-align="right"><xsl:value-of select="inv:unitprice"/> &#8364;</fo:block>
      </fo:table-cell>
      <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid"
                     border-right-width="0.1mm" border-right-color="black" border-right-style="solid">
        <fo:block text-align="right"><xsl:value-of select="inv:itemtotal"/> &#8364;</fo:block>
      </fo:table-cell>
      <fo:table-cell padding="1pt" border-bottom-width="0.1mm" border-bottom-color="black" border-bottom-style="solid">
        <fo:block text-align="right"><xsl:value-of select="format-number(inv:tax,'0')"/>%</fo:block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>

  <xsl:template match="text()"/>

</xsl:stylesheet>
