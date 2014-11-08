<xsl:stylesheet 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
   xmlns:ns="http://www.namescape.nl/"
   xmlns:alto="http://schema.ccs-gmbh.com/ALTO"
   xmlns:tei="http://www.tei-c.org/ns/1.0"
   exclude-result-prefixes="tei"
   xpath-default-namespace="http://www.tei-c.org/ns/1.0"
   xmlns="http://www.tei-c.org/ns/1.0"
   version="2.0">

<!--
<String ID="P1_ST03570" HPOS="3324" VPOS="3090" WIDTH=
"33" HEIGHT="19" CONTENT="on" SUBS_TYPE="HypPart1" SUBS_CONTENT="onderaardsche" WC="0.62" CC="460"/>
                                                <HYP HPOS="3357" VPOS="3090" WIDTH="15" CONTENT="-"/>
                                        </TextLine>
                                        <TextLine ID="P1_TL00560" HPOS="2598" VPOS="3119" WIDTH="365" 
HEIGHT="35">
                                                <String ID="P1_ST03571" HPOS="2598" VPOS="3122" WIDTH=
"196" HEIGHT="28" CONTENT="deraardsche" SUBS_TYPE="HypPart2" SUBS_CONTENT="onderaardsche" WC="0.49" CC
="03487904473"/>

-->

<xsl:template match="/">
<TEI>
<teiHeader>
<fileDesc/>
<sourceDesc/>
</teiHeader>
<text>
<body>
<div>
<xsl:apply-templates select=".//*[local-name()='Layout']"/>
</div>
</body>
</text>
</TEI>
</xsl:template>

<xsl:template match="*[local-name()='Layout']">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[local-name()='TextBlock']">
<ab>
<xsl:apply-templates/>
</ab>
</xsl:template>

<xsl:template match="*[local-name()='TextLine']">
<xsl:apply-templates/> <lb/>
</xsl:template>


<!-- how to handle hyphenation 


<String ID="String190" CONTENT="on" HEIGHT="23" WIDTH="52" HPOS="1282" VPOS="1733" WC="1" CC="000" SUBS_TYPE="HypPart1" SUBS_CONTENT="onbefchaamdheid,"/>
<String ID="String191" CONTENT="befchaamdheid," HEIGHT="43" WIDTH="279" HPOS="394" VPOS="1779" WC="1" CC="00000000000000" SUBS_TYPE="HypPart2" SUBS_CONTENT="onbefchaamdheid,"/>


-->

<xsl:template match="*[local-name()='SP']">
<xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="*[local-name()='String'][@SUBS_TYPE='HypPart1']">
<reg>
<xsl:attribute name="orig"><xsl:value-of select="@CONTENT"/>|<xsl:variable name="s1"><xsl:value-of select="@CONTENT"/></xsl:variable><xsl:variable name="s2"><xsl:value-of select="@SUBS_CONTENT"/></xsl:variable><xsl:value-of select="substring-after($s2,$s1)"/></xsl:attribute>
<w>
<xsl:attribute name="xml:id"><xsl:value-of select="@ID"/></xsl:attribute>
<xsl:value-of select="@SUBS_CONTENT"/>
</w>
</reg>
</xsl:template>

<xsl:template match="*[local-name()='String'][@SUBS_TYPE='HypPart2']"/>

<xsl:template match="*[local-name()='String']">
<w><xsl:attribute name="xml:id"><xsl:value-of select="@ID"/></xsl:attribute> 
<xsl:value-of select="@CONTENT"/>
</w>
</xsl:template>

</xsl:stylesheet>
