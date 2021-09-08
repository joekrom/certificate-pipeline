<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:sparql="http://www.w3.org/2005/sparql-results#"
    exclude-result-prefixes="xs xd sparql"

    version="2.0">
    <!--  xpath-default-namespace="http://www.w3.org/2005/sparql-results#" -->
   <xsl:output indent="yes"></xsl:output>  
   <xsl:strip-space elements="*"/>
    
    <!--<xsl:variable name="zeiss_meta" select="doc('zeiss-meta.xml')/*"/>-->
    
   <!-- <xsl:template match="*">
        <xsl:apply-templates/>
    </xsl:template>
    -->
    
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>
    
    <!--removing the extra output -->
    <xsl:template match="text()|@*"></xsl:template>
    
    <xsl:template match="sparql:results">
        <prod-skill-constraints>
            <xsl:apply-templates/>
        </prod-skill-constraints>
    </xsl:template>
    
    <xsl:template match="sparql:result">
        <xsl:variable name="HWProdIRI" select="sparql:binding[@name='HWProdIRI'][1]"/>
        <!--<xsl:value-of select="$HWProdIRI"/>-->
        
        <xsl:variable name="InstrumentCode" select="sparql:binding[@name='InstrumentCode'][1]"/>
       <!-- <xsl:value-of select="$InstrumentCode"/>-->
        
        <xsl:variable name="InstrumentCodeIRI" select="sparql:binding[@name='InstrumentCodeIRI'][1]"/>
        <!--<xsl:value-of select="$InstrumentCodeIRI"/>-->
        
        <xsl:variable name="HWProdLabel" select="sparql:binding[@name='HWProdLabel'][1]"/>
        <!--<xsl:value-of select="$HWProdLabel"/>-->
        
        <xsl:element name="prod-skill-constraint">
            <xsl:attribute name="HWProdIRI"><xsl:value-of select="$HWProdIRI"/></xsl:attribute>
            <xsl:attribute name="InstrumentCode"><xsl:value-of select="$InstrumentCode"/></xsl:attribute>
            <xsl:attribute name="InstrumentCodeIRI"><xsl:value-of select="$InstrumentCodeIRI"/></xsl:attribute>
            <xsl:value-of select="$HWProdLabel"/>
        </xsl:element>
      <!--<xsl:apply-templates></xsl:apply-templates>-->
    </xsl:template>
    
    <!--<xsl:template match="$zeiss_meta/results">
        <xsl:call-template name="meta"></xsl:call-template>
    </xsl:template>-->
    
    
    
    <!--<xsl:template match="binding">
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>-->
    
   <!-- <xsl:template match="sparql:binding[@name='HWProdIRI']">
        in uri
        <p><xsl:value-of select="sparql:uri"/></p>
    </xsl:template>
-->
    
</xsl:stylesheet>