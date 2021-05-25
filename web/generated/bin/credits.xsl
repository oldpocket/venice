<!--
     This stylesheet is used to generate the credit notes for readme.txt, 
     not the website.
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:doc='http://www.docbook.org/ns/docbook'
  xmlns:xlink='http://www.w3.org/xlink'
  exclude-result-prefixes='doc'> 

  <xsl:output indent='no' method='text'/>
  
  <xsl:template match='chapter[@id = "credits"]'>
    Thanks to the following people for providing additional code, patches of bugfixes: 
    <xsl:apply-templates select='section[sectioninfo/title="Code"]'/>
    And the following people for providing translations:
    <xsl:apply-templates select='section[sectioninfo/title="Translation"]'/>
    <xsl:apply-templates select='section[sectioninfo/title="Graphics"]'/>
  </xsl:template>

  <xsl:template match='para'>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match='simplelist'>
    <xsl:apply-templates select='member[2]'/>
  </xsl:template>

  <xsl:template match='member'>
    <xsl:value-of select='.'/> (<xsl:value-of select='substring-before(preceding-sibling::member[1],":")'/>), <xsl:apply-templates select='following-sibling::member[2]'/>    
  </xsl:template>

  <xsl:template match='title'/>

</xsl:stylesheet>