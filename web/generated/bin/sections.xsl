<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:doc='http://www.docbook.org/ns/docbook'
  exclude-result-prefixes='doc'
>  

  <xsl:output indent='yes'/>

  <xsl:template match='section'>        
  <div>
    <a name='{@id}'></a>      
    <xsl:apply-templates select='sectioninfo/title'/>    
    <xsl:apply-templates/>      
  </div>    
  </xsl:template>

  <xsl:template match='sectioninfo/title'>
    <h4><a name='{../@id}'></a><xsl:apply-templates select='text()'/></h4>
  </xsl:template>

</xsl:stylesheet>