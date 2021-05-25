<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">  
  
  <xsl:import href='utils.xsl'/>
  <xsl:output method="html" indent="yes"/>

  <xsl:template match="/">
    <xsl:call-template name='writeWarningMessage'/>
    <html>

    <head>
      <title>Merchant of Venice</title>
    </head>
    
    <body bgcolor="#ffffff" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
    
      <link rel="stylesheet" href="stylesheet.css"/>

  <table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
    <tbody>
      <tr height="52">
        <td bgcolor="#6f8389">
          <center>
  	    <table border="0" cellspacing="5" cellpadding="0">
	      <tdata>
	        <tr valign="middle">
	          <td>
		    <center><img src="winged_lion.png" width="64" height="64"/><h1>Merchant of Venice</h1></center>
		  </td>
                </tr>
              </tdata>
            </table>
          </center>
        </td>
      </tr>

          <tr height="1">
            <td height="1" bgcolor="#000000"><img src="pixel.png" width="1" height="1"/></td>
          </tr>

          <tr height="100%">
            <td>
              <table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
                <tbody>
                  <tr height="100%">
                    <td bgcolor="#eef1ee" width="18%" valign="top">
		      <table width="100%" height="100%" border="0" cellspacing="10"
		       cellpadding="0">
		        <tdata>
                          <tr valign="top">
                            <td>

  		              <h3>
		                User
		              </h3>

 		              <ul>
		                <li><a href="index.html">Main</a></li>
		                <li><a href="contact.html">Contact Us / Mailing List</a></li>
    				<li><a href="contribute.html">Contribute</a></li>
		                <li><a href="credits.html">Credits</a></li>
  		                <li><a href="http://sourceforge.net/project/showfiles.php?group_id=53631&amp;release_id=152557">Download</a></li>
		                <li><a href="features.html">Features</a></li>
		                <li><a href="links.html">Links</a></li>
		                <li><a href="manual.html">Manual</a></li>
                                <li><a href="screenshots.html">Screenshots</a></li>
		              </ul>

		              <h3>
		                Developer
		              </h3>

		              <ul>
		                <li><a href="api/index.html">API</a></li>
  				<li><a href="future.html">Future Features</a></li>
		              </ul>

		            </td>
                          </tr>
                        </tdata>
                      </table>

		    </td>

                    <td width="1" bgcolor="#000000"><img src="pixel.png" width="1" height="1"/></td>
                    <td bgcolor="#ffffff" width="82%" valign="top">

		      <table width="100%" height="100%" border="0" cellspacing="10" 
		       cellpadding="0">
                        <tdata>
                          <tr valign="top">
                            <td>
                              <xsl:apply-templates/>
		            </td>
                          </tr>
                        </tdata>
                      </table>
                    </td>
                  </tr>
                </tbody>
              </table>
            </td>
          </tr>
        </tbody>
      </table>
    </body>
  </html>
  </xsl:template>

  <xsl:template match="document">
    <h2>Merchant of Venice Manual</h2>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="emphasis">
    <b><xsl:apply-templates/></b>
  </xsl:template>

  <xsl:template match="highlight">
    <i><xsl:apply-templates/></i>
  </xsl:template>

  <xsl:template match="help">
  </xsl:template>

  <xsl:template match="text">
  </xsl:template>

  <xsl:template match="link">
    <a href="#{@to}"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="code">
    <code><xsl:apply-templates/></code>
  </xsl:template>

  <xsl:template match="codeblock">
    <pre><xsl:apply-templates/></pre>
  </xsl:template>

  <xsl:template match="list">
    <ul><xsl:apply-templates/></ul>
  </xsl:template>

  <xsl:template match="item">
    <li><xsl:apply-templates/></li>
  </xsl:template>

  <xsl:template match="para">
    <p><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match='image'>
    <img src='{@name}' alt='{@info}'>
      <xsl:if test='@width != ""'>
	<xsl:attribute name='width'>
	  <xsl:value-of select='@width'/>
	</xsl:attribute>
      </xsl:if>
      <xsl:if test='@height != ""'>
	<xsl:attribute name='height'>
	  <xsl:value-of select='@height'/>
	</xsl:attribute>
      </xsl:if>
    </img>
  </xsl:template>

  <xsl:template match="chapter">
    <h2>
      <a name="{@name}"/>
      <xsl:number level="multiple" format="1 "/>
      <xsl:value-of select="@name"/>
   </h2>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="section">
    <h3>
      <xsl:number level="multiple" count="chapter|section" format="1 "/>
      <xsl:value-of select="@name"/>
    </h3>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="subsection">
    <h4>
      <xsl:number level="multiple" 
                  count="chapter|section|subsection" 
                  format="1 "/>
      <xsl:value-of select="@name"/>
    </h4>
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>
