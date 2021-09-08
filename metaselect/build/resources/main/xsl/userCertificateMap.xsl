<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns="http://www.w3.org/2005/xpath-functions"
    exclude-result-prefixes="xs math"
    version="3.0">
    
    <xsl:output indent="yes"/>
    
    <xsl:template match="/">
        <map>
            <string key="userID">userID</string>
            <xsl:apply-templates/>
        </map>
    </xsl:template>
    
    <xsl:template match="prod-skill-constraints">
        <array key="prod-skill-constraint">
            <xsl:apply-templates></xsl:apply-templates>
        </array>
    </xsl:template>
    
    <xsl:template match="meta-constraints">
        <array key="meta-constraint">
            <xsl:apply-templates></xsl:apply-templates>
        </array>
    </xsl:template>
    
    <xsl:template match="prod-skill-constraint">
        <xsl:variable name="HWProdIRI"   select="@HWProdIRI"/>
        <xsl:variable name="InstrumentCode"   select="@InstrumentCode"/>
        <xsl:variable name="InstrumentCodeIRI"   select="@InstrumentCodeIRI"/>
        <xsl:variable name="leaf"  select="current()"/>
        
        <xsl:element name="map">
            
            <xsl:element name="string">
                <xsl:attribute name="key">
                    <xsl:text>HWProdIRI</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="$HWProdIRI"/>
            </xsl:element>
            
            <xsl:element name="string">
                <xsl:attribute name="key">
                    <xsl:text>InstrumentCode</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="$InstrumentCode"/>
            </xsl:element>
            
            <xsl:element name="string">
                <xsl:attribute name="key">
                    <xsl:text>InstrumentCodeIRI</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="$InstrumentCodeIRI"/>
            </xsl:element>
            
            <xsl:element name="string">
                <xsl:attribute name="key">
                    <xsl:text>#text</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="$leaf"/>
            </xsl:element>
              
        </xsl:element>
    </xsl:template>
    
    
    <xsl:template match="meta-constraint">
        <xsl:variable name="metadataclass"   select="@metadataclass"/>
        <xsl:variable name="metadataIRI"   select="@metadataIRI"/>
        <xsl:variable name="blatt"  select="current()"/>
        
        <xsl:element name="map">
            
            <xsl:element name="string">
                <xsl:attribute name="key">
                    <xsl:text>metadataclass</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="$metadataclass"/>
            </xsl:element>
            
            <xsl:element name="string">
                <xsl:attribute name="key">
                    <xsl:text>metadataIRI</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="$metadataIRI"/>
            </xsl:element>
            
            <xsl:element name="string">
                <xsl:attribute name="key">
                    <xsl:text>#text</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="$blatt"/>
            </xsl:element>
            
        </xsl:element>
    </xsl:template>
    
    
    
</xsl:stylesheet>