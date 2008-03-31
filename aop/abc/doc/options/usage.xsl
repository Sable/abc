<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:include href="to_latex.xsl"/>
<xsl:output method="text" indent="no"/>

<xsl:template match="/">
\documentclass{article}
\usepackage{fullpage}
\usepackage{html}
\usepackage{longtable}

\title{abc command-line options}

\begin{document}

\maketitle

\tableofcontents

\pagebreak

<xsl:for-each select="options/intro">
  <xsl:apply-templates mode="to_latex" select="."/>
</xsl:for-each>

<xsl:for-each select="options/section [not(@undoc)]">
\section{<xsl:value-of select="name"/>}
<xsl:apply-templates mode="to_latex" select="long_desc"/>

<xsl:if test="boolopt|pathopt|intopt|stringopt|argfileopt">
\begin{description}
<xsl:for-each select="boolopt|pathopt|intopt|stringopt|argfileopt">
  <xsl:call-template name="opt"/>
</xsl:for-each>
\end{description}
</xsl:if>

</xsl:for-each>

\end{document}

</xsl:template>

<xsl:template name="opt">
  <xsl:variable name="argLabel">
    <xsl:choose>
      <xsl:when test="name()='boolopt'"></xsl:when>
      <xsl:when test="set_arg_label">$\langle${\it <xsl:value-of select="set_arg_label"/>}$\rangle$</xsl:when>
      <xsl:otherwise>{\it arg}</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="argspace">
    <xsl:choose>
      <xsl:when test="name()='intopt'"></xsl:when>
      <xsl:otherwise>{ }</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="argfile">
    <xsl:choose>
      <xsl:when test="name()='argfileopt'">@<xsl:value-of select="$argLabel"/>, </xsl:when>
      <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  \item[<xsl:for-each select="alias">
  {\tt <xsl:value-of select="$argfile"/>-<xsl:value-of select="."/>}<xsl:value-of select="$argspace"/><xsl:value-of select="$argLabel"/><xsl:if test="following-sibling::alias"><xsl:text>, </xsl:text></xsl:if>
  </xsl:for-each>]
<xsl:if test="default|value/default">
(default value: {\tt <xsl:choose>  
<xsl:when test="default"><xsl:value-of select="default"/></xsl:when>
<xsl:when test="value/default"><xsl:for-each select="value"><xsl:if test="default"><xsl:value-of select="alias"/></xsl:if></xsl:for-each></xsl:when>
<xsl:otherwise>false</xsl:otherwise>
</xsl:choose>})
</xsl:if>

<xsl:apply-templates mode="to_latex" select="long_desc"/>
<xsl:text>

</xsl:text>

<xsl:if test="value">

Possible values:\\
\begin{longtable}{p{1in}p{4in}}
<xsl:for-each select="value">
  <xsl:for-each select="alias">{\tt <xsl:value-of select="."/>}<xsl:if test="following-sibling::alias">,</xsl:if><xsl:text> </xsl:text></xsl:for-each>
&amp;
<xsl:apply-templates mode="to_latex" select="long_desc"/>\\
</xsl:for-each>
\end{longtable}

</xsl:if>

</xsl:template>

<xsl:template match="use_arg_label" mode="to_latex">
  <xsl:choose>
    <xsl:when test="ancestor::*/set_arg_label">{\it <xsl:value-of select="ancestor::*/set_arg_label"/>}</xsl:when>
    <xsl:otherwise>{\it arg}</xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>

