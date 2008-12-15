<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:oryx="http://oryx-editor.org/">

	<xsl:output method="xml" />
	
	<xsl:template match="rdf:Description">	
		<xsl:variable name="typeString" select="./oryx:type" />	
		<xsl:variable name="type">
			<xsl:call-template name="get-exact-type">
				<xsl:with-param name="typeString" select="$typeString" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:if test="$type='worksheet'">
			<!-- root element -->
			<grounding>
				<xsl:variable name="name" select="./oryx:name" />
				<xsl:if test="$name!=''">
					<xsl:attribute name="topology">
						<xsl:value-of select="concat($name,'topology') " />
					</xsl:attribute>
				</xsl:if>

				<xsl:call-template name="add-otherxmlns-attribute"/>

				<messageLinks>
					<xsl:call-templa name="find-all-messageLinks"/>
				</messageLinks>
									
				<participantRefs>
					<xsl:call-templa name="find-all-participantRefs"/>
				</participantRefs>	

			</grounding>	
	 	</xsl:if>
	</xsl:template>
	
	<xsl:template name="find-all-participantRefs">
        <xsl:for-each select="//rdf:Description">
			<xsl:variable name="typeString" select="./oryx:type" />	
			<xsl:variable name="type">
				<xsl:call-template name="get-exact-type">
					<xsl:with-param name="typeString" select="$typeString" />
				</xsl:call-template>
			</xsl:variable>
			
			<!--process-->
			<xsl:if test="$type='process'">
				<participantRef>
					<xsl:variable name="name" select="./oryx:name" />
					<xsl:if test="$name!=''">
						<xsl:attribute name="name">
							<xsl:value-of select="$name" />
						</xsl:attribute>
					</xsl:if>
				</participantRef>
			</xsl:if>	
		</xsl:for-each>
	</xsl:template>	
	
	<xsl:template name="find-all-messageLinks">
        <xsl:for-each select="//rdf:Description">
			<xsl:variable name="typeString" select="./oryx:type" />	
			<xsl:variable name="type">
				<xsl:call-template name="get-exact-type">
					<xsl:with-param name="typeString" select="$typeString" />
				</xsl:call-template>
			</xsl:variable>
			
			<!--messageLink-->
			<xsl:if test="$type='messageLink'">
				<messageLink>
					<xsl:variable name="name" select="./oryx:name" />
					<xsl:if test="$name!=''">
						<xsl:attribute name="name">
							<xsl:value-of select="$name" />
						</xsl:attribute>
					</xsl:if>
					
					<xsl:variable name="portType" select="./oryx:portType" />
					<xsl:if test="$portType!=''">
						<xsl:attribute name="portType">
							<xsl:value-of select="$portType" />
						</xsl:attribute>
					</xsl:if>
					
					<xsl:variable name="operation" select="./oryx:operation" />
					<xsl:if test="$operation!=''">
						<xsl:attribute name="operation">
							<xsl:value-of select="$operation" />
						</xsl:attribute>
					</xsl:if>

				</messageLink>
			</xsl:if>	
		</xsl:for-each>
	</xsl:template>	
	
	<xsl:template name="add-otherxmlns-attribute">
		<xsl:variable name="otherxmlns" select="./oryx:otherxmlns" />
		<xsl:if test="$otherxmlns!=''">
			<xsl:variable name="count">
				<xsl:call-template name="get-number-of-elements-in-complex-type">
					<xsl:with-param name="original_content" select="$otherxmlns" />
				</xsl:call-template>
			</xsl:variable>
			
			<xsl:call-template name="loop-for-adding-otherxmlns-attribute">
				<xsl:with-param name="i">1</xsl:with-param>
				<xsl:with-param name="count" select="$count" />
				<xsl:with-param name="data-set" select="$otherxmlns" />
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="loop-for-adding-otherxmlns-attribute">
		<xsl:param name="i"/>
 		<xsl:param name="count"/>
		<xsl:param name="data-set"/>
			
 		<xsl:if test="$i &lt;= $count">
 			<xsl:variable name="prefix" select="substring-before(substring-after($data-set, 'prefix%3A%22'), '%22%2C%20namespace') " />
			<xsl:variable name="namespace" select="substring-before(substring-after($data-set, 'namespace%3A%22'), '%22%7D') " />
			<xsl:variable name="attribute-name" select="concat('xmlns:',$prefix)" />
			<xsl:attribute name="{$attribute-name}">
				<xsl:value-of select="$namespace"/>
			</xsl:attribute>
			
  			<xsl:call-template name="loop-for-adding-otherxmlns-attribute">
   				<xsl:with-param name="i" select="$i + 1"/>
   				<xsl:with-param name="count" select="$count"/>
   				<xsl:with-param name="data-set" select="substring-after($data-set,'%22%7D%2C%20%7B')"/>
  			</xsl:call-template>
 		</xsl:if>
    </xsl:template>
	
	
	<xsl:template name="get-exact-type">
		<xsl:param name="typeString" />
		<xsl:value-of select="substring-after($typeString, '#')" />
	</xsl:template>
	
	
</xsl:stylesheet>