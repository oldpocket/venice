<?xml version="1.0"?>

<xsl:stylesheet version='1.0'
  xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
  xmlns:doc='http://docbook.org/ns/docbook'
  xmlns:xsltsl='http://xsltsl.sourceforge.net/xsltsl/1.1'>

  <doc:para>
    Generate a warning message on the header of the file noting that the file is generated so that users do not inadvertantly add content which is subsequently lost the next time the documentation is generated. 
  </doc:para>
  <xsl:template name='writeWarningMessage'>
    <xsl:comment>
      ###################################################################
      # This document is generated! Do not edit! If you wish to change  # 
      # the content edit manual.xml instead.                            #
      ###################################################################
    </xsl:comment>
    <xsl:text>
</xsl:text>
  </xsl:template>

</xsl:stylesheet>
