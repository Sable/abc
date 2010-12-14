<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns="http://www.w3.org/1999/xhtml">
 
  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
 
  <xsl:template match="/testsuite">
    <html>
      <head><title><xsl:text>Test Suite </xsl:text><xsl:value-of select="./@name"/></title></head>
      <style type="text/css">
	body {
	  background-color: AliceBlue;
	}
	code {
	  font-family: Monaco, "Courier New", Courier, monospace;
	  font-size: 120%;
	  color: Black;
	  background-color: #dee7ec;
	  padding: 0 0.1em;
	}
	pre {
	  font-family: Monaco, "Courier New", Courier, monospace;
	  font-size: 100%;
	  padding: 1em;
  	  border: 1px solid #8cacbb;
	  color: Black;
	  background-color: #dee7ec;
	  overflow: auto;
	}
      </style>
      <body>
	<h2><xsl:value-of select="./@name"/></h2>
	<ol>
	  <xsl:apply-templates select="testcase"/>
	</ol>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="testcase">
    <li>
      <xsl:apply-templates select="refactoring"/>

      <table border="1" cellpadding="10" cellspacing="5">
	<tr>
	  <th>Input</th>
	  <xsl:choose>
	    <xsl:when test="count(result/program) > 1">
	      <th>Expected Outcomes</th>
	    </xsl:when>
	    <xsl:otherwise>
	      <th>Expected Outcome</th>
	    </xsl:otherwise>
	  </xsl:choose>
	</tr>
	<tr>
	  <td><xsl:apply-templates select="program"/></td>
	  <xsl:apply-templates select="result"/>
	</tr>
      </table>
    </li>
  </xsl:template>

  <xsl:template match="rename">
    <p>
    <xsl:text>Rename </xsl:text>
    <xsl:apply-templates/>
    <xsl:text>to </xsl:text><code><xsl:value-of select="./@newname"/></code>
    </p>
  </xsl:template>

  <xsl:template match="pkgref">
    <xsl:text> package </xsl:text><code><xsl:value-of select="./@name"/></code>
  </xsl:template>

  <xsl:template match="typeref">
    <xsl:text> type </xsl:text><code><xsl:value-of select="./@name"/></code>
  </xsl:template>
                  
  <xsl:template match="methodref">
    <xsl:text> method </xsl:text><code><xsl:value-of select="./@signature"/></code>
    <xsl:text> in type </xsl:text><code><xsl:value-of select="./@host"/></code>
  </xsl:template>

  <xsl:template match="fieldref">
    <xsl:text> field </xsl:text><code><xsl:value-of select="./@name"/></code>
    <xsl:text> in type </xsl:text><code><xsl:value-of select="./@host"/></code>
  </xsl:template>

  <xsl:template match="tempref">
    <xsl:text> variable </xsl:text><code><xsl:value-of select="./@name"/></code>
    <xsl:text> in </xsl:text><code><xsl:value-of select="./@host"/></code>
  </xsl:template>

  <xsl:template match="cu">
    <pre>
      <xsl:text>// compilation unit </xsl:text><xsl:value-of select="./@name"/>
      <xsl:apply-templates/>
    </pre>
  </xsl:template>

  <xsl:template match="result">
    <xsl:if test="@mayfail = 'yes'">
      <xsl:if test="count(program) = 0">
	<td><xsl:text>Must fail.</xsl:text></td>
      </xsl:if>
      <xsl:if test="count(program) > 0">
	<td><xsl:text>May fail.</xsl:text></td>
      </xsl:if>
    </xsl:if>
    <xsl:for-each select="program">
      <td><xsl:apply-templates/></td>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
