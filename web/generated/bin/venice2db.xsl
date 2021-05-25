<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:doc='http://www.docbook.org/ns/docbook'
  xmlns:xlink='http://www.w3.org/xlink'
  exclude-result-prefixes='doc'
>  

  <xsl:output indent='yes'/>

  <xsl:template match='document'>    
  <chapter id='manual' fileref='manual-db.xml' role='user'>
    <chapterinfo>
      <title>Manual</title>
    </chapterinfo>
    
    <xsl:apply-templates select='chapter'/>
  </chapter>
</xsl:template>

  <xsl:template match='help'>

  </xsl:template>

  <xsl:template match='para'>
    <para><xsl:apply-templates/></para>
  </xsl:template>

  <xsl:template match='link'>
    <xref linkend='{@to}'><xsl:apply-templates/></xref>
  </xsl:template>

  <xsl:template match='text'>
    
  </xsl:template>

  <xsl:template match='chapter'>
    <section id='{@name}'>
      <sectioninfo>
        <title><xsl:number level='multiple' format='1 '/><xsl:apply-templates select='@name'/></title>
      </sectioninfo>
      <xsl:apply-templates select='para | section'/>
    </section>
  </xsl:template>

  <xsl:template match='list'>
    <itemizedlist>
      <xsl:apply-templates/>
    </itemizedlist>
  </xsl:template>

  <xsl:template match='item'>
    <listitem>
      <xsl:apply-templates/>
    </listitem>
  </xsl:template>

  <xsl:template match='highlight | emphasis'>
    <emphasis>
      <xsl:apply-templates/>
    </emphasis>
  </xsl:template>
  
  <xsl:template match='section | subsection'>             
    <xsl:variable name='sectLabel'>
      <xsl:choose>
        <xsl:when test='name() = "section"'>
          <xsl:number level='multiple' count='chapter|section' format='1 '/>
        </xsl:when>
        <xsl:when test='name() = "subsection"'>
          <xsl:number level='multiple' count='chapter|section|subsection' format='1 '/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <section id='{@name}'>
      <sectioninfo>
        <title><xsl:value-of select='$sectLabel'/> <xsl:apply-templates select='@name'/></title>
      </sectioninfo>
      <xsl:apply-templates/>
    </section>
  </xsl:template>

  <xsl:template match='pre'>
    <xsl:apply-templates/>
  </xsl:template>

    <xsl:template match='codeblock | code'>
      <literallayout>
        <xsl:apply-templates/>
      </literallayout>
    </xsl:template>
    
    <xsl:template match='*'>
      <xsl:message>
        No template matches for: <xsl:value-of select='name()'/>
    </xsl:message>
  </xsl:template>
     


</xsl:stylesheet>