<xsl:stylesheet version='1.0' 
  xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
  xmlns:doc='http://www.docbook.org/ns/docbook'
  exclude-result-prefixes='doc'>  

  <xsl:import href='docbook-xsl/html/docbook.xsl'/>

  <xsl:output method='xml' indent='yes'/>
  
  <xsl:key name='chapters-by-role' match='chapter' use='@role'/>

  <xsl:template match='/'>
    <xsl:apply-templates select='book'/>    
  </xsl:template>

  <xsl:template match='book'>
    <div id='wrapper'>
      <xsl:call-template name='book.titlepage'/>
      <div id='sidebar'>        
        <xsl:apply-templates select='chapter[generate-id() = generate-id(key("chapters-by-role", @role)[1])]'/>  
      </div>
    </div>
        
  </xsl:template>
  
  <xsl:template match='bookinfo' mode='book.titlepage.recto.auto.mode'>    
    <xsl:apply-templates select='mediaobject' mode='book.titlepage.recto.auto.mode'/>    
    <xsl:apply-templates select='*[name() != "mediaobject"]' mode='book.titlepage.recto.auto.mode'/>
  </xsl:template>
  
  <xsl:template match='mediaobject[@role = "winged-lion"]' mode='book.titlepage.recto.auto.mode'>
    <center>
      <img src='{imageobject/imagedata/@fileref}' height='{imageobject/imagedata/@height}' width='{imageobject/imagedata/@width}'>
        
      </img>
    </center>    
  </xsl:template>

  <xsl:template name='book.titlepage'>
    <div id='header'>
      <xsl:apply-templates select='*' mode='book.titlepage.recto.auto.mode'/>
    </div>
  </xsl:template>

  <xsl:template match='title' mode='book.titlepage.recto.auto.mode'>    
    <xsl:choose>
      <xsl:when test='name(..) = "bookinfo"'>
        <div>
          <h1 class='title'>
            <a name='N10001'></a>Merchant of Venice</h1>
          </div>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:template>
  
  
  <xsl:template match='chapter'>
    <xsl:variable name='lc'>ud</xsl:variable>
    <xsl:variable name='uc'>UD</xsl:variable>
    <h3><xsl:value-of select='translate(@role, $lc, $uc)'/></h3>
    <ul>
      <xsl:for-each select='key("chapters-by-role",@role)'>
        <xsl:variable name='filename'>
          <xsl:choose>
            <xsl:when test='@id = "main"'>
              <xsl:value-of select='"index.html"'/>
            </xsl:when>
            <xsl:when test='@id = "download"'>
              <xsl:value-of select='"https://sourceforge.net/projects/mov/files/"'/>
            </xsl:when>
            <xsl:when test='@id = "api"'>
              <xsl:value-of select='"api/index.html"'/> 
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select='concat(@id, ".html")'/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <li>
          <a href='{$filename}'><xsl:apply-templates select='chapterinfo/title'/></a>
        </li>
      </xsl:for-each>
    </ul>    
  </xsl:template>
    
  <xsl:template match='chapterinfo/title'>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match='text()' mode='book.titlepage.recto.auto.mode'/>  
  
  <xsl:template match='chapterinfo' mode='book.titlepage.recto.auto.mode'/>
  <xsl:template match='subtitle' mode='book.titlepage.recto.auto.mode'/>
  <xsl:template match='revhistory' mode='book.titlepage.recto.auto.mode'/>
  <xsl:template match='authorgroup' mode='book.titlepage.recto.auto.mode'/>

</xsl:stylesheet>