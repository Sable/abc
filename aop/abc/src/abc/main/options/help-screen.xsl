<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:output method="text" indent="no"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="/options">
/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ondrej Lhotak
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

/* THIS FILE IS AUTO-GENERATED FROM options.xml. DO NOT MODIFY. */

package abc.main.options;

public class Usage extends UsageBase {
    public String getUsage() {
        return ""
<xsl:apply-templates mode="usage" select="/options/section"/>;
    }
}
  </xsl:template>

<!--*************************************************************************-->
<!--* USAGE TEMPLATES *******************************************************-->
<!--*************************************************************************-->

  <xsl:template mode="usage" match="* [@undoc]"/>

  <xsl:template mode="usage" match="section">
+"\n<xsl:value-of select="name"/>:\n"
        <xsl:apply-templates mode="usage" select="boolopt|pathopt|intopt|stringopt|argfileopt"/>
  </xsl:template>

<!--* BOOLEAN_OPTION *******************************************************-->
  <xsl:template mode="usage" match="boolopt">
+padOpt("<xsl:for-each select="alias"> -<xsl:value-of select="."/></xsl:for-each>", "<xsl:apply-templates mode="desc" select="short_desc"/>" )<xsl:text/>
  </xsl:template>

<!--* PATH_OPTION *******************************************************-->
  <xsl:template mode="usage" match="pathopt">
    <xsl:for-each select="alias">
      <xsl:if test="position() != last()">
+padOpt(" -<xsl:value-of select="."/><xsl:text> </xsl:text><xsl:call-template name="arg-label"/>", "" )<xsl:text/>
       </xsl:if>
     </xsl:for-each>
+padOpt(" -<xsl:value-of select="alias[last()]"/><xsl:text> </xsl:text><xsl:call-template name="arg-label"/>", "<xsl:apply-templates mode="desc" select="short_desc"/>" )<xsl:text/>
  </xsl:template>

<!--* INT_OPTION *******************************************************-->
  <xsl:template mode="usage" match="intopt">
+padOpt("<xsl:for-each select="alias"> -<xsl:value-of select="."/><xsl:text></xsl:text><xsl:call-template name="arg-label"/></xsl:for-each>", "<xsl:apply-templates mode="desc" select="short_desc"/>" )<xsl:text/>
  </xsl:template>

<!--* STRING_OPTION *******************************************************-->
  <xsl:template mode="usage" match="stringopt">
    <xsl:for-each select="alias">
      <xsl:if test="position() != last()">
+padOpt(" -<xsl:value-of select="."/><xsl:text> </xsl:text><xsl:call-template name="arg-label"/>", "" )<xsl:text/>
       </xsl:if>
     </xsl:for-each>
+padOpt(" -<xsl:value-of select="alias[last()]"/><xsl:text> </xsl:text><xsl:call-template name="arg-label"/>", "<xsl:apply-templates mode="desc" select="short_desc"/>" )<xsl:text/>
  </xsl:template>

<!--* ARGFILE_OPTION *******************************************************-->
  <xsl:template mode="usage" match="argfileopt">
+padOpt(" @<xsl:call-template name="arg-label"/>", "" )<xsl:text/>
    <xsl:for-each select="alias">
      <xsl:if test="position() != last()">
+padOpt(" -<xsl:value-of select="."/><xsl:text> </xsl:text><xsl:call-template name="arg-label"/>", "" )<xsl:text/>
       </xsl:if>
     </xsl:for-each>
+padOpt(" -<xsl:value-of select="alias[last()]"/><xsl:text> </xsl:text><xsl:call-template name="arg-label"/>", "<xsl:apply-templates mode="desc" select="short_desc"/>" )<xsl:text/>
  </xsl:template>

<!-- code to justify comments -->
  <xsl:template name="wrap-string">
    <xsl:param name="text"/>
    <xsl:call-template name="wrap">
      <xsl:with-param name="text" select="$text"/>
      <xsl:with-param name="newline"><xsl:text>\n</xsl:text></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="wrap-comment">
    <xsl:param name="text"/>
    <xsl:call-template name="wrap">
      <xsl:with-param name="text" select="$text"/>
      <xsl:with-param name="newline"><xsl:text>
     * </xsl:text></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="wrap">
    <xsl:param name="text"/>
    <xsl:param name="newline"/>
    <xsl:call-template name="wrap-guts">
      <xsl:with-param name="text" select="translate($text,'&#10;',' ')"/>
      <xsl:with-param name="width" select='0'/>
      <xsl:with-param name="newline" select="$newline"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="wrap-guts">
    <xsl:param name="text"/>
    <xsl:param name="width"/>
    <xsl:param name="newline"/>
    <xsl:variable name="print" select="concat(substring-before(concat($text,' '),' '),' ')"/>
    <xsl:choose>
      <xsl:when test="string-length($print) > number($width)">
        <xsl:copy-of select="$newline"/>
        <xsl:call-template name="wrap-guts">
          <xsl:with-param name="text" select="$text"/>
          <xsl:with-param name="width" select='65'/>
          <xsl:with-param name="newline" select="$newline"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="substring($print,1,string-length($print)-1)"/>
        <xsl:if test="contains($text,' ')">
          <xsl:if test="string-length($print) > 1">
            <xsl:text> </xsl:text>
          </xsl:if>
          <xsl:call-template name="wrap-guts">
            <xsl:with-param name="text" select="substring-after($text,' ')"/>
            <xsl:with-param name="width" select="number($width) - string-length($print)"/>
            <xsl:with-param name="newline" select="$newline"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="desc" match="use_arg_label" priority="2">
    <xsl:call-template name="arg-label"/>
  </xsl:template>

  <!-- Factored out so it can be used to print the argument labels in
       the option summary, e.g. "-src-prec format", 
       as well as argument labels in short_desc and long_desc. -->
  <xsl:template name="arg-label">
  <xsl:choose>
    <xsl:when test="set_arg_label">&lt;<xsl:value-of select="string(set_arg_label)"/>&gt;</xsl:when>
    <xsl:when test="ancestor::*/set_arg_label">&lt;<xsl:value-of select="string(ancestor::*/set_arg_label)"/>&gt;</xsl:when>
    <xsl:otherwise>&lt;arg&gt;</xsl:otherwise>
  </xsl:choose>
  </xsl:template>

  <xsl:template mode="desc" match="*" priority="1">
    <xsl:variable name="subtree">
      <xsl:apply-templates mode="desc"/>
    </xsl:variable>
  <xsl:value-of select="translate($subtree,
                            '&#10;',
                            ' ')"/>
  </xsl:template>

  <xsl:template match="var">
  <xsl:value-of select="translate(string(),
                            'abcdefghijklmnopqrstuvwxyz',
                            'abcdefghijklmnopqrstuvwxyz')"/>
  </xsl:template>
</xsl:stylesheet>
