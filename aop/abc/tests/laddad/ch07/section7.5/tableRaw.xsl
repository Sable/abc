<!-- createTable.xsl -->

<!-- A generic strylesheet for transforming a table-like structured XML
	document into an HTML table
	
	The expected structure is of form
	<table-marker>
		<row-marker>
			<column-marker1>
				column1-data
			</column-marker1>
			<column-marker2>
				column2-data
			</column-marker2>
			...
		</row-marker>
		...
	</table-marker>
-->

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
<xsl:template match="/">
  <html>
    <body>
      <xsl:apply-templates/>
    </body>
  </html>
</xsl:template>

<xsl:template match="/*">
    <table>
    <xsl:apply-templates/>    
    </table>
</xsl:template>

<xsl:template match="/*/*">
  <tr>
    <xsl:apply-templates/>
  </tr>
</xsl:template>

<xsl:template match="/*/*/*">
  <td>
    <xsl:apply-templates/>
  </td>
</xsl:template>

</xsl:stylesheet>

