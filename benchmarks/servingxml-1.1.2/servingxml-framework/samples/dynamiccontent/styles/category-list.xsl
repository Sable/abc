<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE xsl:stylesheet SYSTEM "entities.def">

<xsl:transform 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<xsl:template name="category-list">
<xsl:param name="selected-category"/>

<SCRIPT LANGUAGE="JavaScript">
function selectCategoryList(category){
}
</SCRIPT>

<SELECT id="category-select" name="category-select" onchange="selectCategoryList(this)" width="40">
   <OPTION>Select Category</OPTION>
   <xsl:for-each select="document('categories?category=all',/)//category">
    <xsl:sort select="@desc" order="ascending"/>
    <OPTION VALUE="{@code}">
    <xsl:if test="$selected-category=@code"><xsl:attribute name="SELECTED">true</xsl:attribute></xsl:if>
    <xsl:value-of select="@desc"/>
    </OPTION>
    </xsl:for-each>
</SELECT>

</xsl:template>

</xsl:transform>
