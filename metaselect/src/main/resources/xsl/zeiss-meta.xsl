<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:sparql="http://www.w3.org/2005/sparql-results#"
    exclude-result-prefixes="xs xd sparql"
    version="2.0">
    
    <xsl:output indent="yes"></xsl:output>
    <xsl:strip-space elements="*"/>
    
    <xsl:variable name="prod-skill-constraints" select="doc('../prod-skill-constraints.xml')/prod-skill-constraints"/>
    
    <xsl:template match="/">
        <certificate userID="141260" targetsystem="MIRA"  sourcesystem="PoolParty">
            
            <xsl:copy-of select="$prod-skill-constraints"></xsl:copy-of> 
        
            <xsl:apply-templates/>
        </certificate>
    </xsl:template>
   
    
    <!--removing the extra output -->
    <xsl:template match="text()|@*"></xsl:template>
    
    
    <xsl:template match="sparql:results">
        <meta-constraints>
            
            <!-- document  --> 
            <xsl:for-each-group select="sparql:result" group-by="sparql:binding[@name='documenttype_metadataclass']/sparql:uri">
                
                <!--<xsl:sequence select="current-group()"></xsl:sequence>-->
                
                <xsl:for-each-group select="current-group()" group-by="sparql:binding[@name='documenttype_metadataIRI']/sparql:uri">
                     
                  <!--  <xsl:copy-of select="current-group()[1]"></xsl:copy-of>-->
                    
                    <xsl:call-template name="triple">
                        <xsl:with-param name="metadataclass" select="'documenttype_metadataclass'"></xsl:with-param>
                        <xsl:with-param name="metadataIRI" select="'documenttype_metadataIRI'"></xsl:with-param>
                        <xsl:with-param name="label" select="'documenttype_label'"></xsl:with-param>
                    </xsl:call-template>
                    
                 </xsl:for-each-group>
                
            </xsl:for-each-group>
            
            
            <!-- protection  --> 
            <xsl:for-each-group select="sparql:result" group-by="sparql:binding[@name='protectionclass_metadataclass']/sparql:uri">
            
            <xsl:for-each-group select="current-group()" group-by="sparql:binding[@name='protectionclass_metadataIRI']/sparql:uri">
                
               
                <xsl:call-template name="triple">
                    <xsl:with-param name="metadataclass" select="'protectionclass_metadataclass'"></xsl:with-param>
                    <xsl:with-param name="metadataIRI" select="'protectionclass_metadataIRI'"></xsl:with-param>
                    <xsl:with-param name="label" select="'protectionclass_label'"></xsl:with-param>
                </xsl:call-template>
                
            </xsl:for-each-group>
            
            </xsl:for-each-group>
            
            
            <!-- targetgroup  --> 
            <xsl:for-each-group select="sparql:result" group-by="sparql:binding[@name='targetgroup_metadataclass']/sparql:uri">
                
                <xsl:for-each-group select="current-group()" group-by="sparql:binding[@name='targetgroup_metadataIRI']/sparql:uri">
                    
                    
                    <xsl:call-template name="triple">
                        <xsl:with-param name="metadataclass" select="'targetgroup_metadataclass'"></xsl:with-param>
                        <xsl:with-param name="metadataIRI" select="'targetgroup_metadataIRI'"></xsl:with-param>
                        <xsl:with-param name="label" select="'targetgroup_label'"></xsl:with-param>
                    </xsl:call-template>
                    
                </xsl:for-each-group>
                
            </xsl:for-each-group>
            
            
            <!-- contentorigin  --> 
            <xsl:for-each-group select="sparql:result" group-by="sparql:binding[@name='contentorigin_metadataclass']/sparql:uri">
                
                <xsl:for-each-group select="current-group()" group-by="sparql:binding[@name='contentorigin_metadataIRI']/sparql:uri">
                    
                    
                    <xsl:call-template name="triple">
                        <xsl:with-param name="metadataclass" select="'contentorigin_metadataclass'"></xsl:with-param>
                        <xsl:with-param name="metadataIRI" select="'contentorigin_metadataIRI'"></xsl:with-param>
                        <xsl:with-param name="label" select="'contentorigin_label'"></xsl:with-param>
                    </xsl:call-template>
                    
                </xsl:for-each-group>
                
            </xsl:for-each-group>
            
            <xsl:for-each-group select="sparql:result" group-by="sparql:binding[@name='documentationstatus_metadataclass']/sparql:uri">
                
                <xsl:for-each-group select="current-group()" group-by="sparql:binding[@name='docuementationstatus_metadataIRI']/sparql:uri">
                    
                    
                    <xsl:call-template name="triple">
                        <xsl:with-param name="metadataclass" select="'documentationstatus_metadataclass'"></xsl:with-param>
                        <xsl:with-param name="metadataIRI" select="'documentationstatus_metadataIRI'"></xsl:with-param>
                        <xsl:with-param name="label" select="'documentationstatus_label'"></xsl:with-param>
                    </xsl:call-template>
                    
                </xsl:for-each-group>
                
            </xsl:for-each-group>
            
        </meta-constraints> 
        
    </xsl:template>
    
    
    
    
    <xsl:template match="sparql:result" name="triple">
        <xsl:param name="metadataclass"></xsl:param>
        <xsl:param name="metadataIRI"></xsl:param>
        <xsl:param name="label"></xsl:param>
        
        <xsl:variable name="binding_metadataclass" select="sparql:binding[@name=$metadataclass][1]"/>
        <xsl:variable name="binding_metadataIRI" select="sparql:binding[@name=$metadataIRI][1]"/>
        <xsl:variable name="binding_label" select="sparql:binding[@name=$label][1]"/>
        
        
        <xsl:if test="$binding_metadataclass and $binding_metadataIRI">
            <xsl:element name="meta-constraint">
                <xsl:attribute name="metadataclass"><xsl:value-of select="$binding_metadataclass"/></xsl:attribute>
                <xsl:attribute name="metadataIRI"><xsl:value-of select="$binding_metadataIRI"/></xsl:attribute>
                <xsl:value-of select="$binding_label"/>
            </xsl:element>
        </xsl:if>
        
    </xsl:template>
    
    <xsl:template match="sparql:result" name="meta" mode="unused">
        <xsl:variable name="documenttype_metadataclass" select="sparql:binding[@name='documenttype_metadataclass'][1]"/>
        <xsl:variable name="documenttype_metadataIRI" select="sparql:binding[@name='documenttype_metadataIRI'][1]"/>
        <xsl:variable name="documenttype_label" select="sparql:binding[@name='documenttype_label'][1]"/>
        
        <xsl:variable name="protectionclass_metadataclass" select="sparql:binding[@name='protectionclass_metadataclass'][1]"/>
        <xsl:variable name="protectionclass_metadataIRI" select="sparql:binding[@name='protectionclass_metadataIRI'][1]"/>
        <xsl:variable name="protectionclass_label" select="sparql:binding[@name='protectionclass_label'][1]"/>
        
        <xsl:variable name="targetgroup_metadataclass" select="sparql:binding[@name='targetgroup_metadataclass'][1]"/>
        <xsl:variable name="targetgroup_metadataIRI" select="sparql:binding[@name='targetgroup_metadataIRI'][1]"/>
        <xsl:variable name="targetgroup_label" select="sparql:binding[@name='targetgroup_label'][1]"/>
        
        
        <xsl:variable name="contentorigin_metadataclass" select="sparql:binding[@name='contentorigin_metadataclass'][1]"/>
        <xsl:variable name="contentorigin_metadataIRI" select="sparql:binding[@name='contentorigin_metadataIRI'][1]"/>
        <xsl:variable name="contentorigin_label" select="sparql:binding[@name='contentorigin_label'][1]"/>
        
        
        
        <xsl:variable name="documentstatus_metadataclass" select="sparql:binding[@name='documentstatus_metadataclass'][1]"/>
        <xsl:variable name="documentstatus_metadataIRI" select="sparql:binding[@name='documentstatus_metadataIRI'][1]"/>
        <xsl:variable name="documentstatus_label" select="sparql:binding[@name='documentstatus_label'][1]"/>
        

        <!-- document  --> 
        <xsl:if test="$documenttype_metadataclass and $documenttype_metadataIRI">
        <xsl:element name="meta-constraint">
            <xsl:attribute name="metadataclass"><xsl:value-of select="$documenttype_metadataclass"/></xsl:attribute>
            <xsl:attribute name="metadataIRI"><xsl:value-of select="$documenttype_metadataIRI"/></xsl:attribute>
            <xsl:value-of select="$documenttype_label"/>
        </xsl:element>
        </xsl:if>
        
        <!-- protection -->
        <xsl:if test="$protectionclass_metadataclass and $protectionclass_metadataIRI">
            <xsl:element name="meta-constraint">
                <xsl:attribute name="metadataclass"><xsl:value-of select="$protectionclass_metadataclass"/></xsl:attribute>
                <xsl:attribute name="metadataIRI"><xsl:value-of select="$protectionclass_metadataIRI"/></xsl:attribute>
                <xsl:value-of select="$documenttype_label"/>
            </xsl:element>
        </xsl:if>
        
        <!-- targetgroup -->
        <xsl:if test="$targetgroup_metadataclass and $targetgroup_metadataIRI">
            <xsl:element name="meta-constraint">
                <xsl:attribute name="metadataclass"><xsl:value-of select="$targetgroup_metadataclass"/></xsl:attribute>
                <xsl:attribute name="metadataIRI"><xsl:value-of select="$targetgroup_metadataIRI"/></xsl:attribute>
                <xsl:value-of select="$targetgroup_label"/>
            </xsl:element>
        </xsl:if>
        
        <!-- contenorigin -->
        <xsl:if test="$contentorigin_metadataclass and $contentorigin_metadataIRI">
            <xsl:element name="meta-constraint">
                <xsl:attribute name="metadataclass"><xsl:value-of select="$contentorigin_metadataclass"/></xsl:attribute>
                <xsl:attribute name="metadataIRI"><xsl:value-of select="$contentorigin_metadataIRI"/></xsl:attribute>
                <xsl:value-of select="$contentorigin_label"/>
            </xsl:element>
        </xsl:if>
        
        <!-- documentsatus -->
        <xsl:if test="$documentstatus_metadataclass and $documentstatus_metadataIRI">
            <xsl:element name="meta-constraint">
                <xsl:attribute name="metadataclass"><xsl:value-of select="$documentstatus_metadataclass"/></xsl:attribute>
                <xsl:attribute name="metadataIRI"><xsl:value-of select="$documentstatus_metadataIRI"/></xsl:attribute>
                <xsl:value-of select="$documentstatus_label"/>
            </xsl:element>
        </xsl:if>
        
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    
</xsl:stylesheet>