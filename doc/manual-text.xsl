<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:import href='utils.xsl'/>

  <xsl:strip-space elements="document"/>
  <xsl:output method="text" 
              indent="no"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="link">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="code">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="codeblock">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="help">
  </xsl:template>

  <xsl:template match="text">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="list">
    <xsl:apply-templates/><xsl:text>
</xsl:text>
  </xsl:template>

  <xsl:template match="item">
    <xsl:text>    * </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="emphasis">
    <xsl:text>*</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>*</xsl:text>
  </xsl:template>

  <xsl:template match="highlight">
    <xsl:text>"</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>"</xsl:text>
  </xsl:template>

  <xsl:template match="para">
    <xsl:apply-templates/>
    <xsl:text>
</xsl:text>
  </xsl:template>
  
  <xsl:template match="chapter">
    <xsl:number level="multiple" format="1 "/>
    <xsl:value-of select="@name"/>
    <xsl:text>

</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="section">
    <xsl:number level="multiple" count="chapter|section" format="1 "/>
    <xsl:value-of select="@name"/>
    <xsl:text>

</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="subsection">
    <xsl:number level="multiple" 
                count="chapter|section|subsection" 
                format="1 "/>
    <xsl:value-of select="@name"/>
    <xsl:text>

</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match='image'>
    Image <xsl:value-of select='@info'/>goes here
  </xsl:template>
  
</xsl:stylesheet>
