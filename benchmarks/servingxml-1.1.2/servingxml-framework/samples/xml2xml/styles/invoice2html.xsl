<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:inv="http://www.telio.be/ns/2002/invoice"
                version="2.0">


<xsl:template match="inv:invoice">
<xsl:variable name="company" select="document('company.xml')/inv:company"/>

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta name="generator" content="HTML Tidy, see www.w3.org" />

    <title>Invoice <xsl:value-of select="@id"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<style type="text/css">
.text {
    FONT-SIZE: 9pt; VERTICAL-ALIGN: top; FONT-FAMILY: Helvetica, Arial; BACKGROUND-COLOR: #ffffff
}
.textbold {
    FONT-WEIGHT: bold; FONT-SIZE: 9pt; VERTICAL-ALIGN: top; FONT-FAMILY: Helvetica, Arial; BACKGROUND-COLOR: #ffffff
}
.textboldnolink {
    FONT-WEIGHT: bold; FONT-SIZE: 9pt; FONT-FAMILY: Helvetica, Arial; BACKGROUND-COLOR: #ffffff
}
.texttablebold {
    FONT-WEIGHT: bold; FONT-SIZE: 9pt; FONT-FAMILY: Helvetica, Arial; BACKGROUND-COLOR: #cccccc
}
.textctr {
    FONT-SIZE: 9pt; VERTICAL-ALIGN: top; FONT-FAMILY: Helvetica, Arial; BACKGROUND-COLOR: #ffffff; TEXT-ALIGN: center
}
.graymd {
    BACKGROUND-COLOR: #999999
}
.texttable {
    FONT-SIZE: 9pt; VERTICAL-ALIGN: top; FONT-FAMILY: Helvetica, Arial; BACKGROUND-COLOR: #cccccc
}
.ss12bmd {
    FONT-WEIGHT: bold; FONT-SIZE: 12pt; COLOR: #000000; FONT-FAMILY: Helvetica, Arial; BACKGROUND-COLOR: #999999
}
.ss12b {
    FONT-WEIGHT: bold; FONT-SIZE: 12pt; VERTICAL-ALIGN: top; COLOR: #000000; FONT-FAMILY: Helvetica, Arial; BACKGROUND-COLOR: #ffffff
}
.ss12 {
    FONT-WEIGHT: normal; FONT-SIZE: 12pt; VERTICAL-ALIGN: top; COLOR: #000000; FONT-FAMILY: Helvetica, Arial; BACKGROUND-COLOR: #ffffff
}
.breakitafter {
    PAGE-BREAK-AFTER: always
}
.breakitbefore {
    PAGE-BREAK-BEFORE: always
}
.headrepeat {
    DISPLAY: table-header-group
}
</style>
  </head>

  <body text="#000000" vlink="#999999" alink="#330099" link="#999999" bgcolor="#ffffff" topmargin="0">
    <br/><br/>
    <table align="center" cellspacing="0" cellpadding="0" width="90%" border="0">
      <thead class="headrepeat">
        <tr>
          <td>
            <table cellspacing="0" cellpadding="0" width="100%" border="0">
              <tbody>
                <tr>
                  <td valign="top" width="50%">
                    <table cellspacing="0" cellpadding="0" width="100%" border="0">
                      <tbody>
                        <tr>
                          <td class="ss12b"><xsl:value-of select="$company/inv:name"/></td>
                        </tr>
                        <xsl:for-each select="$company/inv:address">
                        <tr>
                          <td class="ss12"><xsl:value-of select="text()"/></td>
                        </tr>
                        </xsl:for-each>
                        <tr>
                          <td class="ss12">Tel: <xsl:value-of select="$company/inv:tel"/></td>
                        </tr>
                        <tr>
                          <td class="ss12">TVA: <xsl:value-of select="$company/inv:vat"/></td>
                        </tr>
                        <tr>
                          <td class="ss12">RC: <xsl:value-of select="$company/inv:rc"/></td>
                        </tr>
                        <tr>
                          <td class="ss12">Bank: <xsl:value-of select="$company/inv:bank"/></td>
                        </tr>
                      </tbody>
                    </table>
                  </td>

                  <td valign="top" width="50%">
                    <table cellspacing="0" cellpadding="0"
                    width="100%" border="0">
                      <tbody>
                        <tr>
                          <td>
                            <table cellspacing="1" cellpadding="2"
                            width="100%" border="0">
                              <tbody>
                                <tr>
                                  <td class="ss12bmd" colspan="3">
                                  Facture</td>
                                </tr>

                                <tr>
                                  <td class="texttablebold">
                                  Date</td>

                                  <td class="texttablebold">Facture N°
                                  </td>
                                </tr>

                                <tr>
                                  <td class="text"><xsl:value-of select="inv:date"/></td>

                                  <td class="text"><xsl:value-of select="@id"/></td>
                                </tr>

                                <tr>
                                  <td class="text">&#160;</td>

                                  <td class="text">&#160;</td>
                                </tr>
                              </tbody>
                            </table>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </td>
                </tr>
              </tbody>
            </table>
          </td>
        </tr>
      </thead>

      <tbody>
        <tr>
          <td>
            <table cellspacing="0" cellpadding="0" width="100%"
            border="0">
              <tbody>
                <tr>
                  <td>
<table cellspacing="1" cellpadding="2"
                    width="100%" border="0">
                      <tbody>
                        <tr>
                          <td width="50%">&#160;</td>

                          <td class="texttablebold" width="50%">
                          Ship To</td>
                        </tr>

                        <tr>
                          <td class="text" valign="top">&#160;</td>

                          <td class="text" valign="top">
	                      <xsl:value-of select="inv:client/inv:company"/><br/>
                          Att. <xsl:value-of select="inv:client/inv:contact"/><br/>
	                      <xsl:for-each select="inv:client/inv:address">
                          <xsl:value-of select="."/><br/>
	                      </xsl:for-each>
						</td>
                        </tr>
                      </tbody>
                    </table>
                  </td>
                </tr>

                <tr>
                  <td>
                  <br/><br/>
                  <font size="+1" class="textbold">Description : </font><font class="text"><xsl:value-of select="inv:description"/></font>
				  <br/><br/>
                  </td>
                </tr>


                <tr>
                  <td height="5">
                  </td>
                </tr>

                <tr>
                  <td class="graymd">
                    <table cellspacing="1" cellpadding="2"
                    width="100%" border="0">
                      <tbody>
                        <tr>
                          <td class="texttablebold">Qantité</td>

                          <td class="texttablebold">U.F.</td>

                          <td class="texttablebold">Description</td>

                          <td align="right" class="texttablebold">PU.Net</td>

                          <td align="right" class="texttablebold">Montant HTVA</td>

                          <td align="right" class="texttablebold">TVA</td>
                        </tr>

						<xsl:apply-templates/>

                      </tbody>
                    </table>
                  </td>
                </tr>
              </tbody>
            </table>
          </td>
        </tr>

        <tr>
          <td height="10">
          <br/><br/>
          </td>
        </tr>
        <tr>
          <td align="right">
                    <table border="1" cellspacing="1" cellpadding="2">
                      <tbody>
                        <tr>
                          <td align="right" class="texttablebold">BASE HTVA</td>

                          <td align="right" class="texttablebold">TAUX</td>

                          <td align="right" class="texttablebold">MONTANT TVA</td>

                          <td align="center" class="texttablebold">TOTAL TVAC</td>
                        </tr>
                      <xsl:variable name="nbtax" select="count(inv:taxtotal)"/>
                      <xsl:for-each select="inv:taxtotal">
                        <xsl:sort select="inv:taxrate" data-type="number" order="descending"/>
						<tr>
						<td align="right" class="text"><xsl:value-of select="format-number(inv:taxexcluded,'0.00')"/> &#8364;</td>
						<td align="right" class="text"><xsl:value-of select="format-number(inv:taxrate,'0')"/>%</td>
						<td align="right" class="text"><xsl:value-of select="format-number(inv:tax,'0.00')"/> &#8364;</td>

                        <xsl:if test="position()=1">
                           <td valign="center" rowspan="{$nbtax}" align="center" class="textbold">
                           <xsl:value-of select="format-number(../inv:grandtotal/inv:totalincluded,'0.00')"/> &#8364;</td>
                         </xsl:if>

						</tr>



                      </xsl:for-each>



                        </tbody>
                        </table>
          </td>
        </tr>

      </tbody>
    </table>
  </body>
</html>

</xsl:template>

<xsl:template match="inv:item">
<tr>
<td class="text"><xsl:value-of select="inv:quantity"/></td>
<td class="text"><xsl:value-of select="inv:unit"/></td>
<td class="text"><xsl:value-of select="inv:description"/></td>
<td align="right" class="text"><xsl:value-of select="inv:unitprice"/> &#8364;</td>
<td align="right" class="text"><xsl:value-of select="inv:itemtotal"/> &#8364;</td>
<td align="right" class="text"><xsl:value-of select="format-number(inv:tax,'0')"/>%</td>
</tr>
</xsl:template>

<xsl:template match="text()">
</xsl:template>

</xsl:stylesheet>