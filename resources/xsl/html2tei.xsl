<?xml version="1.0" encoding="UTF-8"?>

<!-- http://code.google.com/p/tei2html/source/browse/trunk/html2tei.xsl?r=316 -->
<xsl:stylesheet version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:html="http://www.w3.org/1999/xhtml" 
    xmlns:mf="http://example.com/mf" 
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    exclude-result-prefixes="mf html">

<xsl:output method="xml" indent="no"/>
<xsl:variable name="css"><xsl:value-of select="//style"/></xsl:variable>


<!-- Main block-level conversions -->
<xsl:template match="html">
<TEI xmlns="http://www.tei-c.org/ns/1.0">
<teiHeader>
<sourceDesc>
<listBibl id="inlMetadata">
<bibl>
<interpGrp type="idno"><interp value="$IDNO"/></interpGrp>
<interpGrp type="author"><interp value="unknown"/></interpGrp>
<!--
<interpGrp type="title"><interp value="$TITLE"/></interpGrp>
-->
<interpGrp type="title">
<interp><xsl:attribute name="value"><xsl:value-of select="//title"/></xsl:attribute></interp>
</interpGrp>
<interpGrp type="yearFrom"><interp value="2013"/></interpGrp>
<interpGrp type="yearTo"><interp value="2013"/></interpGrp>
<interpGrp type="source"><interp value="pap.wikipedia.org"/></interpGrp>
</bibl>
</listBibl>
<!--
<listBibl id="inlMetadata_HANS">
<bibl>
<interpGrp type="title.level2">
<interp value="$TITLE.LEVEL2"/>
</interpGrp>
<interpGrp type="publisher">
<interp value="$PUBLISHER"/>
</interpGrp>
<interpGrp type="date.publication">
<interp type="pubYear_from" value="$PUBYEAR_FROM"/>
<interp type="pubMonth_from" value="$PUBMONTH_FROM"/>
<interp type="pubDay_from" value=""/>
<interp type="pubYear_to" value="$PUBYEAR_TO"/>
<interp type="pubMonth_to" value=""/>
<interp type="pubDay_to" value=""/>
</interpGrp>
<interpGrp type="date.text">
<interp type="textYear_from" value="$TEXTYEAR_FROM"/>
<interp type="textMonth_from" value=""/>
<interp type="textDay_from" value=""/>
<interp type="textYear_to" value="$TEXTYEAR_TO"/>
<interp type="textMonth_to" value=""/>
<interp type="textDay_to" value=""/>
</interpGrp>
<interpGrp type="date.witness">
<interp type="witnessYear_from" value="$WITNESSYEAR_FROM"/>
<interp type="witnessMonth_from" value=""/>
<interp type="witnessDay_from" value=""/>
<interp type="witnessYear_to" value="$WITNESSYEAR_TO"/>
<interp type="witnessMonth_to" value=""/>
<interp type="witnessDay_to" value=""/>
</interpGrp>
<interpGrp type="copyright.owner">
<interp value="$COPYRIGHT_OWNER"/>
</interpGrp>
<interpGrp type="author.level2">
<interp value="$AUTHOR.LEVEL2"/>
</interpGrp>
<interpGrp type="$IDNO">
<interp value="aubyn"/>
</interpGrp>
<interpGrp type="corpus.provenance">
<interp value="$CORPUS.PROVENANCE"/>
</interpGrp>
<interpGrp type="medium">
<interp value="$MEDIUM"/>
</interpGrp>
<interpGrp type="genre"/>
<interpGrp type="topic">
<interp value="$TOPIC"/>
</interpGrp>
<interpGrp type="language.variant">
<interp value="rob"/>
</interpGrp>
<interpGrp type="ipr">
<interp value="online"/>
</interpGrp>
</bibl>
</listBibl>
-->
</sourceDesc>
</teiHeader>
<text>
    <xsl:apply-templates select="body"/>
</text>
</TEI>
</xsl:template>


<!-- Reconstruct structure of divisions, based on HTML h1..h6 headings
     Note that this will create correctly nested div1..div5 levels, even
     if some h-levels are skipped in the source HTML. You may however get
     more div-levels than you want, and post-processing will be required. -->
<xsl:template match="body">
    <body>
<xsl:apply-templates/>
<!---
        <xsl:call-template name="level1">
            <xsl:with-param name="nodes" select="*|text()"/>
            <xsl:with-param name="level" select="1"/>
        </xsl:call-template>
-->
    </body>
</xsl:template>


<xsl:template match="div">
<div>
<xsl:apply-templates/>
</div>
</xsl:template>

<xsl:template name="level1">
    <xsl:param name="nodes" as="element()*"/>
    <xsl:param name="level" as="xs:integer"/>
    
    <xsl:choose>
        <xsl:when test="$nodes/self::h1">
            <xsl:for-each-group select="$nodes" group-starting-with="h1">
                <xsl:element name="{concat('div', $level)}">
                    <xsl:call-template name="level2">
                        <xsl:with-param name="nodes" select="current-group()"/>
                        <xsl:with-param name="level" select="$level + 1"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:for-each-group>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="level2">
                <xsl:with-param name="nodes" select="$nodes"/>
                <xsl:with-param name="level" select="$level"/>
            </xsl:call-template>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="level2">
    <xsl:param name="nodes" as="element()*"/>
    <xsl:param name="level" as="xs:integer"/>
    
    <xsl:choose>
        <xsl:when test="$nodes/self::h2">
            <xsl:for-each-group select="$nodes" group-starting-with="h2">
                <xsl:element name="{concat('div', $level)}">
                    <xsl:call-template name="level3">
                        <xsl:with-param name="nodes" select="current-group()"/>
                        <xsl:with-param name="level" select="$level + 1"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:for-each-group>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="level3">
                <xsl:with-param name="nodes" select="$nodes"/>
                <xsl:with-param name="level" select="$level"/>
            </xsl:call-template>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="level3">
    <xsl:param name="nodes" as="element()*"/>
    <xsl:param name="level" as="xs:integer"/>
    
    <xsl:choose>
        <xsl:when test="$nodes/self::h3">
            <xsl:for-each-group select="$nodes" group-starting-with="h3">
                <xsl:element name="{concat('div', $level)}">
                    <xsl:call-template name="level4">
                        <xsl:with-param name="nodes" select="current-group()"/>
                        <xsl:with-param name="level" select="$level + 1"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:for-each-group>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="level4">
                <xsl:with-param name="nodes" select="$nodes"/>
                <xsl:with-param name="level" select="$level"/>
            </xsl:call-template>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="level4">
    <xsl:param name="nodes" as="element()*"/>
    <xsl:param name="level" as="xs:integer"/>
    
    <xsl:choose>
        <xsl:when test="$nodes/self::h4">
            <xsl:for-each-group select="$nodes" group-starting-with="h4">
                <xsl:element name="{concat('div', $level)}">
                    <xsl:call-template name="level5">
                        <xsl:with-param name="nodes" select="current-group()"/>
                        <xsl:with-param name="level" select="$level + 1"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:for-each-group>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="level5">
                <xsl:with-param name="nodes" select="$nodes"/>
                <xsl:with-param name="level" select="$level"/>
            </xsl:call-template>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="level5">
    <xsl:param name="nodes" as="element()*"/>
    <xsl:param name="level" as="xs:integer"/>
    
    <xsl:choose>
        <xsl:when test="$nodes/self::h5">
            <xsl:for-each-group select="$nodes" group-starting-with="h5">
                <xsl:element name="{concat('div', $level)}">
                    <xsl:call-template name="level6">
                        <xsl:with-param name="nodes" select="current-group()"/>
                        <xsl:with-param name="level" select="$level + 1"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:for-each-group>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="level6">
                <xsl:with-param name="nodes" select="$nodes"/>
                <xsl:with-param name="level" select="$level"/>
            </xsl:call-template>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>


<xsl:template name="level6">
    <xsl:param name="nodes" as="element()*"/>
    <xsl:param name="level" as="xs:integer"/>

    <xsl:apply-templates select="$nodes/self::*"/>
</xsl:template>











<!-- Paragraphs -->
<xsl:template match="p">
    <xsl:choose>
        <xsl:when test="normalize-space(.) = ''">
            <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:text>
</xsl:text>
            <p>
                <xsl:apply-templates/>
            </p>
            <xsl:text>
</xsl:text>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>


<!-- Ignore empty paragraphs -->
<xsl:template match="p[normalize-space(.) = '' and count(*) = 0]"/>


<xsl:template match="text()">
    <xsl:copy/>
</xsl:template>



<!-- inline formatting -->
<xsl:template match="b">
    <hi rend="bold">
        <xsl:apply-templates/>
    </hi>
</xsl:template>

<xsl:template match="i">
 <hi>
  <xsl:apply-templates/>
 </hi>
</xsl:template>

<xsl:template match="small">
  <xsl:apply-templates/>
</xsl:template>


<xsl:template match="br">
    <lb/>
</xsl:template>



<!-- External links -->
<xsl:template match="a[contains(@href,'http://') or contains(@href,'ftp://')]">
 <xref>
  <xsl:attribute name="url">
   <xsl:value-of select="normalize-space(@href)"/>
  </xsl:attribute>
  <xsl:apply-templates/>
 </xref>
</xsl:template>

<xsl:template match="link">
 <xref>
  <xsl:attribute name="url">
   <xsl:value-of select="normalize-space(@href)"/>
  </xsl:attribute>
  <xsl:apply-templates/>
 </xref>
</xsl:template>

<!-- Internal cross references -->
<xsl:template match="a[contains(@href,'#')]">
 <ref>
  <xsl:attribute name="target">
    <xsl:value-of select="substring-after(@href,'#')"/>
  </xsl:attribute>
  <xsl:apply-templates/>
 </ref>
</xsl:template>

<xsl:template match="a">
  <xsl:apply-templates/>
</xsl:template>



<!-- Block level elements -->

<xsl:template match="h1|h2|h3|h4|h5|h6">
    <head>
        <xsl:apply-templates/>
    </head>
</xsl:template>


<xsl:template match="hr">
    <milestone unit="tb"/>
</xsl:template>


<xsl:template match="span">
<xsl:variable name="class"><xsl:value-of select="@class"/></xsl:variable>
<xsl:variable name="pattern">.<xsl:value-of select="$class"/>{font-style:italic</xsl:variable>
<xsl:choose>
<xsl:when test="contains($css,$pattern)">
<xsl:message>Yep!</xsl:message>
<hi rend="italic">
<xsl:apply-templates/>
</hi>
</xsl:when>
<xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
</xsl:choose>
</xsl:template>


<xsl:template match="form"><gap type="form"/></xsl:template>
<xsl:template match="noscript"><gap type="noscript"/></xsl:template>
<xsl:template match="embed"><gap type="embed"/></xsl:template>

<xsl:template match="sup|sub|em|strong|i|b">
<hi>
<xsl:attribute name="rend"><xsl:value-of select="name(.)"/></xsl:attribute>
<xsl:apply-templates/>
</hi>
</xsl:template>

<xsl:template match="center">
<ab type='center'>
<xsl:apply-templates/>
</ab>
</xsl:template>

<xsl:template match="font">
<hi>
<xsl:attribute name="rend">font-family:<xsl:value-of select="@face"/></xsl:attribute>
<xsl:apply-templates/>
</hi>
</xsl:template>

<xsl:template match="pre">
<hi rend='pre'>
    <xsl:apply-templates/>
</hi>
</xsl:template>



<!-- Images -->
<xsl:template match="img">
 
 
</xsl:template>




<!-- Lists -->
<xsl:template match="ul">
 <list>
  <xsl:apply-templates/>
 </list>
</xsl:template>

<xsl:template match="ol">
 <list>
  <xsl:apply-templates/>
 </list>
</xsl:template>
        
<xsl:template match="li">
 <item>
    <xsl:apply-templates/>
 </item>
</xsl:template>
        



<!-- Tables -->
<xsl:template match="table">
    <table>
        <xsl:apply-templates/>
    </table>
</xsl:template>


<xsl:template match="tbody">
    <xsl:apply-templates/>
</xsl:template>


<xsl:template match="tr">
    <row>
        <xsl:apply-templates/>
    </row>
</xsl:template>

<xsl:template match="th|td">
 <xsl:variable name="position" select="count(preceding-sibling::*) + 1"/>
 <cell>
  <xsl:if test="@colspan &gt; 1">
   <xsl:attribute name="cols">
    <xsl:value-of select="@colspan"/>
   </xsl:attribute>
  </xsl:if>
  <xsl:if test="@rowspan &gt; 1">
   <xsl:attribute name="rows">
    <xsl:value-of select="@rowspan"/>
   </xsl:attribute>
  </xsl:if>
  <xsl:apply-templates/>
 </cell>
</xsl:template>


<!-- Stop processing when an unknown element is encountered -->     
<xsl:template match="*">
 <xsl:message>Unhandled element <xsl:value-of select="name()"/>
 </xsl:message>
 <xsl:apply-templates/>
</xsl:template>

<xsl:template match="script|style" priority="100"/>

</xsl:stylesheet>