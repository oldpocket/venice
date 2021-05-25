<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:doc='http://www.docbook.org/ns/docbook'
  xmlns:xlink='http://www.w3.org/xlink'
  exclude-result-prefixes='doc'
>  

  <xsl:import href="docbook-xsl/html/docbook.xsl"/>
  <xsl:import href='sections.xsl'/>

  <xsl:output indent='yes'/>

  <xsl:key name='chapters-by-role' match='chapter' use='@role'/>

  <xsl:param name='common-elements' select='document("common.xml",/)'/>

  <xsl:param name="html.stylesheet" select="'stylesheet.css'"/>
  <xsl:param name='generate.toc' select='"0"'/>

  <xsl:template name="body.attributes">
    <xsl:attribute name="bgcolor">#ffffff</xsl:attribute>
    <xsl:attribute name="text">black</xsl:attribute>
    <xsl:attribute name="link">#0000cc</xsl:attribute>
    <xsl:attribute name="vlink">#840084</xsl:attribute>
    <xsl:attribute name="alink">#0000ff</xsl:attribute>
  </xsl:template>

  <xsl:template match='revhistory' mode='book.titlepage.recto.auto.mode'/>
  <xsl:template match='authorgroup' mode='book.titlepage.recto.auto.mode'/>
  
  <!-- Handle this elsewhere -->
  <xsl:template match='subtitle' mode='book.titlepage.recto.auto.mode'/>

  
  <xsl:template match='book'>
    <xsl:call-template name='build-header'/>
    <xsl:apply-templates select='$common-elements/div[@id = "wrapper"]/div[@id="sidebar"]' mode='copy'/>      
    <!-- This needs to be outside main body or else it gets indented -->
    <xsl:apply-templates select='bookinfo/subtitle'/>
    <div id='mainbody'>
      <xsl:apply-templates select='chapter[@id="main"]'/>
    </div>  
  </xsl:template> 
    
  <xsl:template match='subtitle'>
    <h3><xsl:apply-templates/></h3>
  </xsl:template>  
  
  <xsl:template match='chapter'>
    <xsl:if test='@id != "main"'>
      <xsl:call-template name='build-header'/>      
      <xsl:apply-templates select='$common-elements/div[@id = "wrapper"]/div[@id="sidebar"]' mode='copy'/>      
    </xsl:if>      
    <div id='mainbody'>
      <xsl:apply-templates/> 
    </div>

  </xsl:template>  
  
  <xsl:template match='*' mode='copy'>
    <xsl:copy>
      <xsl:for-each select='@*'>
        <xsl:copy/>
      </xsl:for-each>
      <xsl:apply-templates mode='copy'/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name='build-header'>
    <xsl:apply-templates select='$common-elements/div[@id = "wrapper"]/div[@id= "header"]' mode='copy'/>
  </xsl:template>

  <xsl:template match='link'>
    <a href='{@xlink:href}'><xsl:apply-templates/></a>

  </xsl:template>

</xsl:stylesheet>