<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (c) 2010-2013, Silvio Peroni <essepuntato@gmail.com>

Permission to use, copy, modify, and/or distribute this software for anyi
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY biro "http://purl.org/spar/biro/">
    <!ENTITY cito "http://purl.org/spar/cito/">
    <!ENTITY co "http://purl.org/co/">
    <!ENTITY datacite "http://purl.org/spar/datacite/">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dcterms "http://purl.org/dc/terms/">
    <!ENTITY deo "http://purl.org/spar/deo/">
    <!ENTITY fabio "http://purl.org/spar/fabio/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY frapo "http://purl.org/cerif/frapo/">
    <!ENTITY frbr "http://purl.org/vocab/frbr/core#">
    <!ENTITY literal "http://www.essepuntato.it/2010/06/literalreification/">
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">
    <!ENTITY prism "http://prismstandard.org/namespaces/basic/2.0/">
    <!ENTITY pro "http://purl.org/spar/pro/">
    <!ENTITY prov "http://www.w3.org/ns/prov#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY scoro "http://purl.org/spar/scoro/">
    <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
    <!ENTITY swanrel "http://purl.org/swan/2.0/discourse-relationships/">
    <!ENTITY trait "http://contextus.net/ontology/ontomedia/ext/common/trait#">
    <!ENTITY tvc "http://www.essepuntato.it/2012/04/tvc/">
    <!ENTITY vcard "http://www.w3.org/2006/vcard/ns#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:biro="&biro;" 
    xmlns:cito="&cito;"
    xmlns:co="&co;"
    xmlns:datacite="&datacite;"
    xmlns:dc="&dc;"
    xmlns:dcterms="&dcterms;"
    xmlns:deo="&deo;"
    xmlns:fabio="&fabio;"
    xmlns:foaf="&foaf;"
    xmlns:frapo="&frapo;"
    xmlns:frbr="&frbr;" 
    xmlns:literal="&literal;"
    xmlns:owl="&owl;"
    xmlns:prism="&prism;"
    xmlns:pro="&pro;"
    xmlns:prov="&prov;"
    xmlns:rdfs="&rdfs;"
    xmlns:rdf="&rdf;" 
    xmlns:scoro="&scoro;"
    xmlns:skos="&skos;" 
    xmlns:tvc="&tvc;" 
    xmlns:vcard="&vcard;"
    xmlns:xsd="&xsd;"
    xmlns:f="http://www.essepuntato.it/xslt/function/"
    xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xs f xlink" version="2.0"
    xmlns:fn="http://www.w3.org/2005/xpath-functions">

    <xsl:param name="baseUri" select="''"/>
    <xsl:param name="doi" select="//article-id[@pub-id-type  = 'doi']"/>
    <xsl:param name="default" select="concat($baseUri, $doiUriInfix)"/>
    <xsl:param name="doiUriInfix" select="concat('doi/', $doi)"/>
    <xsl:param name="doiOrgUri" select="'http://dx.doi.org/'"/>
    <xsl:param name="issn" select="//journal-meta/issn"/>
    <xsl:param name="journalUriInfix" select="concat($baseUri,'journal/')"/>
    <xsl:param name="nlmmcatUri" select="'http://www.ncbi.nlm.nih.gov/nlmcatalog?term='"/>
    <xsl:param name="pubmedNcbiUrl" select="'http://www.ncbi.nlm.nih.gov/pubmed/'"/>
    <xsl:param name="pubmedIdentifiersUrl" select="'http://identifiers.org/pubmed/'"/>
    <xsl:template match="/">
        <rdf:RDF xml:base="resource/">
            <xsl:call-template name="goahead">

                <xsl:with-param name="w" select="concat($default, '/conceptual-work')" tunnel="yes"/>
                <xsl:with-param name="e" select="concat($default, '/textual-entity')" tunnel="yes"/>
                <xsl:with-param name="m" select="concat($default, '/digital-embodiment')"
                    tunnel="yes"/>
                <xsl:with-param name="i" select="concat($default, '/digital-item')" tunnel="yes"/>
                <xsl:with-param name="issue" select="'periodical-issue'" tunnel="yes"/>
                <xsl:with-param name="collection" select="'conceptual-papers-collection'"
                    tunnel="yes"/>
                <xsl:with-param name="volume" select="'periodical-volume'" tunnel="yes"/>
                <xsl:with-param name="journal" select="$journalUriInfix" tunnel="yes"/>
                <xsl:with-param name="journalOfArticle"
                    select="f:getJournalName(//journal-meta/journal-title-group/journal-title)"
                    tunnel="yes"/>
                <xsl:with-param name="stem"
                    select="/article/front/article-meta/article-id[@pub-id-type='publisher-id']/text()"
                    tunnel="yes"/>
                <xsl:with-param name="s" select="''" tunnel="yes"/>
                <xsl:with-param name="p" select="''" tunnel="yes"/>
                <xsl:with-param name="o" select="''" tunnel="yes"/>
                <xsl:with-param name="p2" select="''" tunnel="yes"/>
                <xsl:with-param name="o2" select="''" tunnel="yes"/>
                <xsl:with-param name="lang" select="''" tunnel="yes"/>
            </xsl:call-template>
        </rdf:RDF>
    </xsl:template>

    <xsl:template match="element()">
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="text()|attribute()|body|abstract"/>

    <!-- BEGIN - Mapping elements -->
    <xsl:template match="abbrev-journal-title">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasShortTitle'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
            <xsl:with-param name="lang" select="$lang" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="address">
        <xsl:message>TRANS: unprocessed address tag encountered</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="addr-line">
        <xsl:message>TRANS: unprocessed address-line tag encountered</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <!-- This to prevent affiliations outside contrib will be executed -->
    <xsl:template match="aff|aff-alternatives"/>

    <xsl:template match="xref[@ref-type = 'aff']">
        <xsl:variable name="type" select="@rid"/>
        <xsl:apply-templates select="//aff[@id = $type]" mode="xref"/>
    </xsl:template>

    <xsl:template match="xref[@ref-type = 'corresp']">
        <xsl:variable name="type" select="@rid"/>
        <xsl:apply-templates select="//author-notes/corresp[@id = $type]" mode="xref"/>
    </xsl:template>

    <xsl:template match="aff[parent::contrib or parent::aff-alternatives]">
        <xsl:call-template name="affiliation"/>
    </xsl:template>

    <xsl:template match="aff-alternatives[parent::contrib]">
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="aff" mode="xref">
        <xsl:call-template name="affiliation"/>
    </xsl:template>

    <xsl:template match="//author-notes/corresp" mode="xref">
        <xsl:call-template name="correspondent"/>
    </xsl:template>

    <xsl:template match="aff-alternatives" mode="xref">
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template name="correspondent">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>
        <xsl:variable name="email" select="concat('&quot;',email[1]/text(),'&quot;')"/>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$s" tunnel="yes"/>
            <xsl:with-param name="p" select="'foaf:mbox'" tunnel="yes"/>
            <xsl:with-param name="o" select="$email" tunnel="yes"/>
        </xsl:call-template>

    </xsl:template>

    <xsl:template name="affiliation">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>
        <xsl:variable name="orgaName">
            <xsl:choose>
                <xsl:when test="string-length(label/text()) != 0">
                    <xsl:variable name="strippedOrgaName" select="replace(., '[\[\]]', '')"/>
                    <xsl:variable name="strippedLabel" select="replace(label/text(), '[\[\]]', '')"/>
                    <xsl:sequence
                        select="replace($strippedOrgaName, concat('^[\s]*',$strippedLabel), '')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="."/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="organization"
            select="concat($baseUri, 'organization/', f:urlEncode($orgaName))"/>
        <xsl:variable name="current-affiliation"
            select="concat('affiliation-',$organization,'-',$s)"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'pro:holdsRoleInTime','',
                    'pro:withRole','scoro:affiliate',
                    'pro:relatesToOrganization',$organization,
                    'pro:relatesToDocument',$w,
                    'dcterms:description',concat('&quot;',$orgaName,'&quot;'))"
            />
        </xsl:call-template>

        <xsl:variable name="contact-info" select="concat($organization, '/contact')"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $organization,'tvc:hasValueInTime','',
                'rdf:type','&tvc;ValueInTime',
                'tvc:withinContext',$w,
                'tvc:withValue',$contact-info)"
            />
        </xsl:call-template>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$contact-info" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&vcard;VCard'" tunnel="yes"/>
        </xsl:call-template>

        <xsl:choose>
            <xsl:when test="institution">
                <xsl:call-template name="goahead">
                    <xsl:with-param name="s" select="$contact-info" tunnel="yes"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="assert">
                    <xsl:with-param name="triples"
                        select="(
                        $contact-info,'vcard:org','',
                        'rdf:type','&vcard;Organization',
                        'vcard:organization-name',concat('&quot;', $orgaName, '&quot;', if (@xml:lang) then @xml:lang else ''))"
                    />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="alt-title">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'prism:alternateTitle'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
            <xsl:with-param name="lang" select="$lang" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="anonymous">
        <xsl:message>TRANS: unprocessed anonymous-tag encountered</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="article">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>
        <xsl:param name="m" tunnel="yes"/>
        <xsl:param name="i" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="($e,'rdf:type','&fabio;Expression','frbr:realizationOf',$w,'frbr:embodiment',$m,'fabio:hasRepresentation',$i)"
            />
        </xsl:call-template>
        <xsl:call-template name="goahead">
            <xsl:with-param name="s" select="$e" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="article-id[not(@pub-id-type)]">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'dcterms:identifier'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="article-title|jorunal-title">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'dcterms:title'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
            <xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"
                tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="award-id">
        <xsl:message>"TRANS: Found unprocessed award-id tag.</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>


    <xsl:template match="bio">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="bio" select="concat($s,'/biography')"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $bio,'frbr:realiazationOf','&fabio;Biography',
                'frbr:partOf',$e,
                'dcterms:description',concat('&quot;',.,'&quot;'),
                'frbr:subject','',
                'rdf:type','&foaf;Person')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="chapter-title">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="source" select="concat($e,'-source')"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $e,'rdf:type','&fabio;BookChapter',
                'dcterms:title',concat('&quot;',.,'&quot;'),
                'frbr:partOf',$source)"
            />
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$source" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Book'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="collab">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'foaf:name'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
            <xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"
                tunnel="yes"/>
        </xsl:call-template>

        <xsl:if test="not(parent::collab-alternatives)">
            <xsl:call-template name="single">
                <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
                <xsl:with-param name="o" select="'&foaf;Group'" tunnel="yes"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="collab-alternatives">
        <xsl:message>TRANS: Unprocessed collab-alternatives tag found.</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="conference">
        <xsl:message>TRANS: Unprocessed conference tag found.</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="conf-acronym">
        <xsl:message>TRANS: Unprocessed conf-acronym tag found.</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="conf-date">
        <xsl:message>TRANS: Unprocessed conf-date tag found.</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="conf-name">
        <xsl:message>TRANS: Unprocessed conf-name tag found.</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="conf-num">
        <xsl:message>TRANS: Unprocessed conf-num tag found.</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="conf-loc">
        <xsl:message>TRANS: Unprocessed conf-loc tag found.</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="conf-sponsor">
        <xsl:message>TRANS: Unprocessed conf-sponsor tag found.</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="conf-theme">
        <xsl:message>TRANS: Unprocessed conf-theme tag found.</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="contrib">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:variable name="agent" select="concat($w,'/agent-',count(preceding-sibling::contrib)+1)"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'dcterms:contributor'" tunnel="yes"/>
            <xsl:with-param name="o" select="$agent" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$agent" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&foaf;Agent'" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="goahead">
            <xsl:with-param name="s" select="$agent" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="contrib-id[not(@contrib-id-type)]">
        <xsl:message>TRANS: Unprocessed tag contrib-id found</xsl:message>
    </xsl:template>

    <xsl:template match="copyright-holder">
        <xsl:message>TRANS: Unprocessed tag copyright-holder found</xsl:message>
    </xsl:template>

    <xsl:template match="copyright-statement">
        <xsl:message>TRANS: Unprocessed tag copyright-statement found</xsl:message>
    </xsl:template>

    <xsl:template match="copyright-year">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasCopyrightYear'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="country">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="address" select="concat('address-',$s)"/>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'vcard:address'" tunnel="yes"/>
            <xsl:with-param name="o" select="$address" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$address" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&vcard;Address'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="attribute">
            <xsl:with-param name="s" select="$address" tunnel="yes"/>
            <xsl:with-param name="p" select="'vcard:country-name'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="date-in-citation">
        <xsl:message>TRANS: Found unprocessed tag date-in-citation</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="degree">
        <xsl:message>TRANS: Found unprocessed tag degree</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="element-citation|mixed-citation">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>
        <xsl:variable name="cited-document" select="concat($s,'/textual-entity')"/>
        <xsl:variable name="cited-work" select="concat($s,'/conceptual-work')"/>

        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'biro:references'" tunnel="yes"/>
            <xsl:with-param name="o" select="$cited-document" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$e" tunnel="yes"/>
            <xsl:with-param name="p" select="'cito:cites'" tunnel="yes"/>
            <xsl:with-param name="o" select="$cited-document" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="double">
            <xsl:with-param name="s" select="$cited-document" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Expression'" tunnel="yes"/>
            <xsl:with-param name="p2" select="'frbr:realizationOf'" tunnel="yes"/>
            <xsl:with-param name="o2" select="$cited-work" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="goahead">
            <xsl:with-param name="s" select="$cited-document" tunnel="yes"/>
            <xsl:with-param name="e" select="$cited-document" tunnel="yes"/>
            <xsl:with-param name="w" select="$cited-work" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="elocation-id">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $e,'frbr:embodiment','',
                    'rdf:type','&fabio;Manifestation',
                    'fabio:hasDigitalArticleIdentifier',concat('&quot;',.,'&quot;'))"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="edition">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="source" select="concat($e,'-source')"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="s" select="$source" tunnel="yes"/>
            <xsl:with-param name="p" select="'prism:edition'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'frbr:partOf'" tunnel="yes"/>
            <xsl:with-param name="o" select="$source" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="email[not(parent::corresp)]">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:variable name="email" select="concat('&quot;',email[1]/text(),'&quot;')"/>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'vcard:email'" tunnel="yes"/>
            <xsl:with-param name="o" select="$email" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="email[parent::contrib]">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:variable name="email" select="concat('&quot;',email[1]/text(),'&quot;')"/>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'foaf:mbox'" tunnel="yes"/>
            <xsl:with-param name="o" select="$email" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <!-- Temporarily commented to understand how to find out authors from a reference
    <xsl:template match="etal">
        <xsl:param name="e" tunnel="yes" />
        <xsl:call-template name="assert">
            <xsl:with-param name="triples" select="(
                $e,'rdf:type',''
                    'rdf:type','&owl;Restriction',
                    'owl:onProperty','&dcterms;creator',
                    'owl:minCardinality',concat('&quot;',.,'&quot;'))" />
        </xsl:call-template>
    </xsl:template>
    -->

    <xsl:template match="ext-link">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'dcterms:relation'" tunnel="yes"/>
            <xsl:with-param name="o" select="if (@xlink:href) then @xlink:href else ." tunnel="yes"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="fax">
        <xsl:message>TRANS: Found unprocessed tag fax</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="funding-source">
        <xsl:message>TRANS: Found unprocessed tag funding-source</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="given-names">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'foaf:givenName'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
            <xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"
                tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="goahead">
            <xsl:with-param name="p" select="'frapo:givenNameInitial'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="gov">
        <xsl:message>TRANS: Found unprocessed tag gov</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="institution">
        <xsl:message>TRANS: Found unprocessed tag institution</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="isbn[parent::article-meta]">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:param name="issue" tunnel="yes"/>
        <xsl:param name="volume" tunnel="yes"/>
        <xsl:param name="journalOfArticle" tunnel="yes"/>
        <xsl:variable name="issue"
            select="concat($journalOfArticle, '/', $issue, //article-meta/issue/text())"/>
        <xsl:variable name="volume"
            select="concat($journalOfArticle, '/', $volume, //article-meta/volume/text())"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                if (//article-meta/issue) then $issue else if (//article-meta/volume) then $volume else $e,'frbr:embodiment','',
                    'rdf:type','&fabio;Manifestation',
                    'prism:isbn',.)"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="issn">
        <xsl:param name="issnValue" select="."/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'prism:issn'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdfs:seeAlso'" tunnel="yes"/>
            <xsl:with-param name="o" select="concat($nlmmcatUri,$issnValue)" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template
        match="issue[parent::article-meta or parent::mixed-citation or parent::element-citation or parent::nlm-citation]">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>
        <xsl:param name="issue" tunnel="yes"/>
        <xsl:param name="collection" tunnel="yes"/>
        <xsl:param name="journalOfArticle" tunnel="yes"/>
        <xsl:variable name="check" select="parent::mixed-citation[@publication-type]"/>
        <xsl:variable name="issue">
            <xsl:choose>
                <xsl:when test="parent::article-meta">
                    <xsl:sequence select="concat($journalOfArticle, '/', $issue, '/issue_', .)"/>
                </xsl:when>
                <xsl:when test="parent::mixed-citation/@publication-type= 'journal'">
                    <xsl:variable name="uripart" select="f:urlEncode(../source)"/>
                    <xsl:sequence select="concat(f:getJournalName(../source), '/issue_', .)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="concat($e, '/', f:urlEncode(../source), '/issue_', .) "/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="collection" select="concat($w, '/', $collection)"/>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'frbr:partOf'" tunnel="yes"/>
            <xsl:with-param name="o" select="$issue" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $issue,'rdf:type','&fabio;PeriodicalIssue',
                    'frbr:realizationOf',$collection,
                    'prism:issueIdentifier',.)"
            />
        </xsl:call-template>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$collection" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;WorkCollection'" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="goahead">
            <xsl:with-param name="s" select="$issue" tunnel="yes"/>
            <xsl:with-param name="w" select="$collection" tunnel="yes"/>
            <xsl:with-param name="e" select="$issue" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="issue-id">
        <xsl:message>TRANS: Found unprocessed tag issue-id</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="issue-title">
        <xsl:message>TRANS: Found unprocessed tag issue-title</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="issue-sponsor">
        <xsl:message>TRANS: Found unprocessed tag issue-sponsor</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="issue-part">
        <xsl:message>TRANS: Found unprocessed tag issue-part</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="journal-id[not(@journal-id-type)]">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'dcterms:identifier'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
            <xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"
                tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="journal-meta">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:param name="issue" tunnel="yes"/>
        <xsl:param name="volume" tunnel="yes"/>
        <xsl:param name="journal" tunnel="yes"/>
        <xsl:param name="journalOfArticle" tunnel="yes"/>

        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'frbr:partOf'" tunnel="yes"/>
            <xsl:with-param name="o" select="$journalOfArticle" tunnel="yes"/>
        </xsl:call-template>

        <xsl:variable name="volume"
            select="concat($journalOfArticle, '/', $volume, '/volume_', //article-meta/volume/text())"/>
        <xsl:variable name="issue"
            select="concat($journalOfArticle,'/',$issue,'issue_', //article-meta/issue/text())"/>


        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $journalOfArticle,'rdf:type','&fabio;Journal','frbr:realizationOf','',
                    'rdf:type','&fabio;WorkCollection')"
            />
        </xsl:call-template>

        <xsl:call-template name="goahead">
            <xsl:with-param name="s" select="$journalOfArticle" tunnel="yes"/>
            <xsl:with-param name="e" select="$journalOfArticle" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="kwd">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'prism:keyword'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="license[@xlink:href]">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'dcterms:license'" tunnel="yes"/>
            <xsl:with-param name="o" select="@xlink:href" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template
        match="name[not(parent::name-alternatives)]|string-name[not(parent::name-alternatives)]|name-alternatives">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:choose>
            <xsl:when test="parent::person-group">
                <xsl:variable name="agent"
                    select="concat($s,'-agent-',count(preceding-sibling::name|preceding-sibling::string-name)+1)"/>
                <xsl:call-template name="single">
                    <xsl:with-param name="p" select="'foaf:member'" tunnel="yes"/>
                    <xsl:with-param name="o" select="$agent" tunnel="yes"/>
                </xsl:call-template>
                <xsl:call-template name="single">
                    <xsl:with-param name="s" select="$agent" tunnel="yes"/>
                    <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
                    <xsl:with-param name="o" select="'&foaf;Person'" tunnel="yes"/>
                </xsl:call-template>

                <xsl:call-template name="goahead">
                    <xsl:with-param name="s" select="$agent" tunnel="yes"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="single">
                    <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
                    <xsl:with-param name="o" select="'&foaf;Person'" tunnel="yes"/>
                </xsl:call-template>

                <xsl:call-template name="goahead"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template
        match="name[not(parent::name-alternatives)][parent::mixed-citation or parent::element-citation]|string-name[not(parent::name-alternatives)][parent::mixed-citation or parent::element-citation]|name-alternatives[parent::mixed-citation or parent::element-citation]">
        <xsl:param name="w" tunnel="yes"/>

        <xsl:variable name="agent"
            select="concat('reference-',count(ancestor::ref/preceding-sibling::ref)+1,'-agent-',count(preceding-sibling::name|preceding-sibling::name-alternatives|preceding-sibling::string-name)+1)"/>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'dcterms:creator'" tunnel="yes"/>
            <xsl:with-param name="o" select="$agent" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$agent" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&foaf;Person'" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="goahead">
            <xsl:with-param name="s" select="$agent" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>
    <!-- This to prevent products will be executed -->
    <xsl:template match="product"/>

    <xsl:template match="page-range|fpage|lpage">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="me" select="replace($e, 'textual-entity', 'digital-embodiment')"/>
        <xsl:if
            test="not(following-sibling::page-range|following-sibling::fpage|following-sibling::lpage)">
            <xsl:call-template name="single">
                <xsl:with-param name="p" select="'frbr:embodiment'" tunnel="yes"/>
                <xsl:with-param name="o" select="$me" tunnel="yes"/>
            </xsl:call-template>
            <xsl:if test="preceding-sibling::page-range">
                <xsl:call-template name="single">
                    <xsl:with-param name="s" select="$me" tunnel="yes"/>
                    <xsl:with-param name="p" select="'prism:pageRange'" tunnel="yes"/>
                    <xsl:with-param name="o" select="preceding-sibling::page-range[1]" tunnel="yes"
                    />
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="preceding-sibling::fpage">
                <xsl:call-template name="single">
                    <xsl:with-param name="s" select="$me" tunnel="yes"/>
                    <xsl:with-param name="p" select="'prism:startingPage'" tunnel="yes"/>
                    <xsl:with-param name="o" select="preceding-sibling::fpage[1]" tunnel="yes"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="self::lpage">
                <xsl:call-template name="single">
                    <xsl:with-param name="s" select="$me" tunnel="yes"/>
                    <xsl:with-param name="p" select="'prism:endingPage'" tunnel="yes"/>
                    <xsl:with-param name="o" select="self::lpage[1]" tunnel="yes"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template match="part-title">
        <xsl:message>TRANS: Found unprocessed tag part-title</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="patent">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;PatentDocument'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasPatentNumber'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="person-group[not(@person-group-type)]">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="group" select="concat($e,'/group-',count(preceding::person-group)+1)"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                    $group,'rdf:type','&foaf;Group',
                    'pro:holdsRoleInTime','',
                        'pro:withRole','&pro;contributor',
                        'pro:relatesToDocument',$e)"
            />
        </xsl:call-template>

        <xsl:if test="empty(name)">
            <xsl:call-template name="attribute">
                <xsl:with-param name="s" select="$group" tunnel="yes"/>
                <xsl:with-param name="p" select="'foaf:name'" tunnel="yes"/>
                <xsl:with-param name="o" select=".." tunnel="yes"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="person-group">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="group" select="concat($e,'/group-',count(preceding::person-group)+1)"/>
        <xsl:call-template name="goahead">
            <xsl:with-param name="s" select="$group" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="phone">
        <xsl:message>TRANS: Found unprocessed tag phone</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="prefix">
        <xsl:message>TRANS: Found unprocessed tag prefix</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="principal-award-recipient">
        <xsl:message>TRANS: Found unprocessed tag principal-award-recipient</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="principal-investigator">
        <xsl:message>TRANS: Found unprocessed tag principal-investigator</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template
        match="publisher|publisher-name[parent::mixed-citation or parent::element-citation]">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="publisher" select="concat($e,'/publisher')"/>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'dcterms:publisher'" tunnel="yes"/>
            <xsl:with-param name="o" select="$publisher" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$publisher" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&foaf;Organization'" tunnel="yes"/>
        </xsl:call-template>

        <xsl:choose>
            <xsl:when test="self::publisher-name">
                <xsl:call-template name="attribute">
                    <xsl:with-param name="p" select="'foaf:name'" tunnel="yes"/>
                    <xsl:with-param name="o" select="." tunnel="yes"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="goahead">
                    <xsl:with-param name="s" select="$publisher" tunnel="yes"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="publisher-name">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'foaf:name'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
        <xsl:if test="self::publisher-name='PsychOpen'">
            <xsl:call-template name="single">
                <xsl:with-param name="p" select="'rdfs:seeAlso'" tunnel="yes"/>
                <xsl:with-param name="o" select="'http://www.psychopen.eu/'" tunnel="yes"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="publisher-loc">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:param name="s" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'tvc:hasValueInTime','',
                    'rdf:type','&tvc;ValueInTime',
                    'tvc:withinContext',$e,
                    'tvc:withValue','',
                        'rdf:type','&vcard;VCard',
                        'vcard:addr','',
                            'rdf:type','&vcard;Address',
                            'vcard:locality',concat('&quot;',.,'&quot;'))"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="pub-date|date">
        <xsl:choose>
            <xsl:when test="@date-type">
                <xsl:apply-templates select="@date-type"/>
            </xsl:when>
            <xsl:when test="@publication-format">
                <xsl:apply-templates select="@publication-format"/>
            </xsl:when>
            <xsl:when test="@pub-type">
                <xsl:apply-templates select="@pub-type"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="set-date">
                    <xsl:with-param name="p" select="'dcterms:date'" tunnel="yes"/>
                    <xsl:with-param name="o" select="'&dcterms;date'" tunnel="yes"/>
                    <xsl:with-param name="el" select="."/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="ref">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="i" select="count(preceding-sibling::ref)+1"/>
        <xsl:variable name="iref" select="concat($default,'/reference-item-',$i)"/>
        <xsl:variable name="node" select="."/>
        <xsl:variable name="ref" select="concat($default,'/reference-', @id[1])"/>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'co:item'" tunnel="yes"/>
            <xsl:with-param name="o" select="$iref" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="double">
            <xsl:with-param name="s" select="$iref" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&co;ListItem'" tunnel="yes"/>
            <xsl:with-param name="p2" select="'co:itemContent'" tunnel="yes"/>
            <xsl:with-param name="o2" select="$ref" tunnel="yes"/>
        </xsl:call-template>

        <xsl:if test="following-sibling::ref">
            <xsl:call-template name="single">
                <xsl:with-param name="s" select="$iref" tunnel="yes"/>
                <xsl:with-param name="p" select="'co:nextItem'" tunnel="yes"/>
                <xsl:with-param name="o" select="concat($default,'/reference-item-',$i+1)"
                    tunnel="yes"/>
            </xsl:call-template>
        </xsl:if>

        <xsl:call-template name="attribute">
            <xsl:with-param name="s" select="$iref" tunnel="yes"/>
            <xsl:with-param name="p" select="'co:index'" tunnel="yes"/>
            <xsl:with-param name="o" select="xs:string($i)" tunnel="yes"/>
            <xsl:with-param name="type" select="'&xsd;integer'" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="attribute">
            <xsl:with-param name="s" select="$ref" tunnel="yes"/>
            <xsl:with-param name="p" select="'dcterms:bibliographicCitation'" tunnel="yes"/>
            <xsl:with-param name="o" select="normalize-space(.)" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$ref" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&biro;BibliographicReference'" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="goahead">
            <xsl:with-param name="s" select="$ref" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="ref-list">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="reference-list" select="concat($e,'/reference-list')"/>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'frbr:part'" tunnel="yes"/>
            <xsl:with-param name="o" select="$reference-list" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$reference-list" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&biro;ReferenceList'" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="goahead">
            <xsl:with-param name="s" select="$reference-list" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="related-article">
        <xsl:message>TRANS: Found unprocessed tag related-article</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="related-object">
        <xsl:message>TRANS: Found unprocessed tag related-object</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="role">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'pro:holdsRoleInTime','',
                'pro:relatesToDocument',$w,
                'pro:withRole','',
                'rdf:type','&pro;Role',
                'rdfs:label',concat('&quot;',.,'&quot;'))"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="role[. = 'editor-in-chief']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'pro:holdsRoleInTime','',
                'pro:relatesToDocument',$w,
                'pro:withRole','&pro;editor-in-chief')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="role[. = 'chief scientist']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'pro:holdsRoleInTime','',
                'pro:relatesToDocument',$w,
                'pro:withRole','&scoro;chief-scientist')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="role[. = 'photographer']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'pro:holdsRoleInTime','',
                'pro:relatesToDocument',$w,
                'pro:withRole','&scoro;photographer')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="role[. = 'research associate']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'pro:holdsRoleInTime','',
                'pro:relatesToDocument',$w,
                'pro:withRole','&scoro;postdoctoral-researcher')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="season">
        <xsl:message>TRANS: Found unprocessed tag season</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="self-uri[@xlink:href]">
        <xsl:message>TRANS: Found unprocessed tag self-uri</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="source">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:param name="journal" tunnel="yes"/>
        <xsl:variable name="source">
            <xsl:choose>
                <xsl:when test="(parent::mixed-citation/@publication-type='journal')">
                    <xsl:sequence select="concat($journal,f:urlEncode(.))"/>
                </xsl:when>
                <xsl:when test="parent::mixed-citation/@publication-type='book'">
                    <xsl:sequence select="concat($baseUri,'/book/', f:urlEncode(.))"/>
                </xsl:when>
                <xsl:when test="parent::mixed-citation/@publication-type='thesis'">
                    <xsl:sequence select="concat($baseUri,'/thesis/',f:urlEncode(.))"/>
                </xsl:when>
                <xsl:when test="parent::mixed-citation/@publication-type='confproc'">
                    <xsl:sequence select="concat($baseUri,'/confproc/',f:urlEncode(.))"/>
                </xsl:when>
                <xsl:when test="parent::mixed-citation/@publication-type='web'">
                    <xsl:sequence select="concat($baseUri,'/web/',f:urlEncode(.))"/>
                </xsl:when>
                <xsl:when test="parent::mixed-citation/@publication-type='other'">
                    <xsl:sequence select="concat($baseUri, '/other/', f:urlEncode(.)) "/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="concat($baseUri, '/other/', f:urlEncode(.)) "/>
                    <xsl:message>TRANS: Found source element that has publication type without
                        specified URI. Pub-type: <xsl:value-of
                            select="parent::mixed-citation/@publication-type/."/>
                    </xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:call-template name="attribute">
            <xsl:with-param name="s" select="$source" tunnel="yes"/>
            <xsl:with-param name="p" select="'dcterms:title'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'frbr:partOf'" tunnel="yes"/>
            <xsl:with-param name="o" select="$source" tunnel="yes"/>
        </xsl:call-template>

    </xsl:template>

    <xsl:template match="std">
        <xsl:message>TRANS: Found unprocessed tag std</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="std-organization">
        <xsl:message>TRANS: Found unprocessed tag std-organization</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="sub-article">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>
        <xsl:param name="i" tunnel="yes"/>
        <xsl:variable name="prefix" select="concat('/sub',count(preceding::sub-article)+1)"
            as="xs:string"/>
        <xsl:variable name="sw" select="concat($w,$prefix)" as="xs:string"/>
        <xsl:variable name="se" select="concat($e,$prefix)" as="xs:string"/>
        <xsl:variable name="si" select="concat($i,$prefix)" as="xs:string"/>
        <xsl:variable name="me" select="replace($se, 'textual-entity', 'digital-embodiment')"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="($se,'rdf:type','&fabio;Expression','frbr:realizationOf',$sw,'frbr:embodiment',$me,'fabio:hasRepresentation',$si,'frbr:partOf',$e)"
            />
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$sw" tunnel="yes"/>
            <xsl:with-param name="p" select="'frbr:partOf'" tunnel="yes"/>
            <xsl:with-param name="o" select="$w" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="goahead">
            <xsl:with-param name="w" select="$sw" tunnel="yes"/>
            <xsl:with-param name="e" select="$se" tunnel="yes"/>
            <xsl:with-param name="s" select="$se" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="subj-group">
        <xsl:param name="s" tunnel="yes"/>

        <xsl:for-each select="subject">
            <xsl:variable name="subject-counting" select="count(preceding::subject)+1"/>
            <xsl:variable name="subject-uri" select="concat($s,'/term',$subject-counting)"/>
            <xsl:call-template name="single">
                <xsl:with-param name="p" select="'fabio:hasSubjectTerm'" tunnel="yes"/>
                <xsl:with-param name="o" select="$subject-uri" tunnel="yes"/>
            </xsl:call-template>

            <xsl:call-template name="apply">
                <xsl:with-param name="s" select="$subject-uri" tunnel="yes"/>
            </xsl:call-template>
        </xsl:for-each>

        <xsl:if test="subj-group">
            <xsl:for-each select="subject">
                <xsl:variable name="subject-counting" select="count(preceding::subject)+1"/>
                <xsl:variable name="subject-uri" select="concat($s,'-term',$subject-counting)"/>
                <xsl:call-template name="single">
                    <xsl:with-param name="s" select="$subject-uri" tunnel="yes"/>
                    <xsl:with-param name="p" select="'skos:narrower'" tunnel="yes"/>
                    <xsl:with-param name="o" select="concat($s,'-term',count(preceding::subject)+1)"
                        tunnel="yes"/>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template match="subject">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;SubjectTerm'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'rdfs:label'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="subtitle|journal-subtitle">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasSubtitle'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
            <xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"
                tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="suffix">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'frapo:hasNameSuffix'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="supplementary-material">
        <xsl:message>TRANS: Found unprocessed tag supplementary-material</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="surname">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'foaf:familyName'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
            <xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"
                tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="goahead">
            <xsl:with-param name="p" select="'frapo:givenNameInitial'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="trans-title">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasTranslatedTitle'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
            <xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"
                tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="trans-subtitle">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasTranslatedSubtitle'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
            <xsl:with-param name="lang" select="if (@xml:lang) then @xml:lang else $lang"
                tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="uri">
        <xsl:message>TRANS: Found unprocessed tag uri</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="volume">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>
        <xsl:param name="issue" tunnel="yes"/>
        <xsl:param name="volume" tunnel="yes"/>
        <xsl:param name="journalOfArticle" tunnel="yes"/>
        <xsl:variable name="issue">
            <xsl:choose>
                <xsl:when test="parent::article-meta">
                    <xsl:sequence
                        select="concat($journalOfArticle, '/', $issue, '/issue_', ../issue/text())"
                    />
                </xsl:when>
                <xsl:when test="parent::mixed-citation/@publication-type= 'journal'">
                    <xsl:variable name="uripart" select="f:urlEncode(../source)"/>
                    <xsl:sequence
                        select="concat(f:getJournalName(../source), '/issue_', ../issue/text() )"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence
                        select="concat($e, '/', f:urlEncode(../source), '/issue_', ../issue/text()) "
                    />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="volume">
            <xsl:choose>
                <xsl:when test="parent::article-meta">
                    <xsl:sequence select="concat($journalOfArticle, '/', $volume, '/volume_', .)"/>
                </xsl:when>
                <xsl:when test="parent::mixed-citation/@publication-type= 'journal'">
                    <xsl:variable name="uripart" select="f:urlEncode(../source)"/>
                    <xsl:sequence select="concat(f:getJournalName(../source), '/volume_', .)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="concat($e, '/', f:urlEncode(../source), '/volume_', .) "/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="if (../issue) then $issue else $e" tunnel="yes"/>
            <xsl:with-param name="p" select="'frbr:partOf'" tunnel="yes"/>
            <xsl:with-param name="o" select="$volume" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $volume,'rdf:type','&fabio;PeriodicalVolume',
                'prism:volume',concat('&quot;',.,'&quot;'),
                'frbr:partOf','',
                'rdf:type','&fabio;Periodical')"
            />
        </xsl:call-template>
        <xsl:call-template name="goahead">
            <xsl:with-param name="s" select="$volume" tunnel="yes"/>
            <xsl:with-param name="e" select="$volume" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="volume-id">
        <xsl:message>TRANS: Found unprocessed tag volume-id</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="volume-series">
        <xsl:message>TRANS: Found unprocessed tag volume-series</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="year[parent::element-citation|parent::mixed-citation]">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasPublicationYear'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
            <!--xsl:with-param name="type" select="'&xsd;'"/-->
        </xsl:call-template>

        <xsl:call-template name="goahead"/>
    </xsl:template>
    <!-- END - Mapping elements -->

    <!-- BEGIN - Mapping attributes -->
    <xsl:template match="@abbrev-type">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'fabio:hasShortTitle','',
                'rdf:type',datacite:Identifier,
                'literal:hasLiteralValue',concat('&quot;',..,'&quot;'),
                '&prov;wasAttributedTo','',
                'rdf:type','&prov;Agent',
                'rdfs:label',concat('&quot;',.,'&quot;'))"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'abstract']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="($s,'rdf:type','&fabio;Abstract','frbr:summarizationOf','','rdf:type','&fabio;Expression')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'addendum']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Addendum'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'announcement']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Announcement'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'article-commentary']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="($s,'rdf:type','&fabio;Comment','cito:discusses','','rdf:type','&fabio;Article')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'book-review']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="($s,'rdf:type','&fabio;BookReview','cito:reviews','','rdf:type','&fabio;Book')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'books-received']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="($w,'rdf:type','&fabio;NotificationOfReceipt','swanrel:relatesTo','','rdf:type','&fabio;Book')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'brief-report']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;BriefReport'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'calendar']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;TimeTable'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'case-report']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;ReportDocument'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;CaseReport'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'collection']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;ExpressionCollection'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'correction']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Correction'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'discussion']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Opinion'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'dissertation']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Thesis'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'editorial']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Editorial'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'in-brief']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>
        <xsl:param name="issue" tunnel="yes"/>
        <xsl:variable name="issue" select="concat($e, '/', $issue)"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="($s,'rdf:type','&fabio;InBrief','frbr:partOf',$issue,'rdf:summarizationOf','','rdf:type','&fabio;Article','frbr:partOf',$issue)"
            />
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$issue" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;PeriodicalIssue'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'introduction']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&deo;Introduction'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'letter']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Letter'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'meeting-report']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;ReportDocument'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;MeetingReport'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'news']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;NewsItem'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'obituary']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Obituary'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'oration']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Oration'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'partial-retraction']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Retraction'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="($s,'cito:retracts','','frbr:partOf','','rdf:type','&owl;Thing')"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'product-review']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;ProductReview'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="double">
            <xsl:with-param name="p" select="'cito:reviews'" tunnel="yes"/>
            <xsl:with-param name="p2" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o2" select="'&owl;Thing'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'rapid-communication']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;RapidCommunication'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'reprint']">
        <xsl:param name="m" tunnel="yes"/>
        <xsl:call-template name="double">
            <xsl:with-param name="s" select="$m" tunnel="yes"/>
            <xsl:with-param name="p" select="'frbr:reproductionOf'" tunnel="yes"/>
            <xsl:with-param name="p2" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o2" select="'&fabio;Manifestation'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'research-article']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Article'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;RapidCommunication'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'retraction']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Retraction'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="double">
            <xsl:with-param name="p" select="'cito:retracts'" tunnel="yes"/>
            <xsl:with-param name="p2" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o2" select="'&owl;Thing'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'review-article']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="($s,'rdf:type','&fabio;ReviewArticle','cito:reviews','','rdf:type','&owl;Thing')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@article-type[. = 'translation']">
        <xsl:call-template name="double">
            <xsl:with-param name="p" select="'frbr:translationOf'" tunnel="yes"/>
            <xsl:with-param name="p2" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o2" select="'&fabio;Expression'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@calendar">
        <xsl:message>TRANS: Found unprocessed attribute "@calendar"</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@collab-type">
        <xsl:message>TRANS: Found unprocessed attribute "@collab-type"</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@contrib-type|@collab-type">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'pro:holdsRoleInTime','',
                    'pro:relatesToDocument',$w,
                    'pro:withRole','',
                        'rdf:type','&pro;Role',
                        'rdf:label',.)"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@contrib-type[. = 'author']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'dcterms:creator'" tunnel="yes"/>
            <xsl:with-param name="o" select="$s" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'pro:holdsRoleInTime','',
                    'pro:relatesToDocument',$w,
                    'pro:withRole','&pro;author')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@contrib-type[. = 'editor']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'dcterms:contributor'" tunnel="yes"/>
            <xsl:with-param name="o" select="$s" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'pro:holdsRoleInTime','',
                'pro:relatesToDocument',$w,
                'pro:withRole','&pro;editor')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@contrib-type[. = 'translator']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="w" tunnel="yes"/>

        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'dcterms:contributor'" tunnel="yes"/>
            <xsl:with-param name="o" select="$s" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'pro:holdsRoleInTime','',
                'pro:relatesToDocument',$w,
                'pro:withRole','&pro;translator')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@contrib-id-type">
        <xsl:message>TRANS: Found unprocessed attribute "@contrib-id-type"</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@corresp[. = 'yes']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="correspNode" select="../xref[@ref-type = 'corresp']"/>
        <xsl:variable name="correspRid" select="$correspNode/@rid"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'pro:holdsRoleInTime','',
                'pro:relatesToDocument',$e,
                'pro:withRole','&scoro;corresponding-author')"
            />
        </xsl:call-template>

    </xsl:template>

    <xsl:template match="@country">
        <xsl:message>TRANS: Found unprocessed attribute "@country"</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@date-type[. = 'accepted']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'frbr:realization'" tunnel="yes"/>
            <xsl:with-param name="o" select="$e" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$e" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Expression'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="set-date">
            <xsl:with-param name="s" select="$e" tunnel="yes"/>
            <xsl:with-param name="p" select="'dcterms:dateAccepted'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&dcterms;dateAccepted'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:apply-templates select="../@publication-format">
            <xsl:with-param name="e" select="$e" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="@date-type[. = 'corrected']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="date"
            select="concat('&quot;',f:getDate(..),'&quot;^^',f:getDatetype(..))"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'frbr:realization'" tunnel="yes"/>
            <xsl:with-param name="o" select="$e" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="set-date">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'fabio:hasCorrectionDate'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;hasCorrectionDate'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $e,'rdf:type','&fabio;Expression',
                'frbr:revision','',
                    'rdf:type','&fabio;Expression')"
            />
        </xsl:call-template>
        <xsl:apply-templates select="../@publication-format">
            <xsl:with-param name="e" select="$e" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="@date-type[. = 'preprint']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Preprint'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="set-date">
            <xsl:with-param name="p" select="'fabio:hasDistributionDate'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;hasDistributionDate'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:apply-templates select="../@publication-format"/>
    </xsl:template>

    <xsl:template match="@date-type[. = 'retracted']">
        <xsl:call-template name="set-date">
            <xsl:with-param name="p" select="'fabio:hasRetractionDate'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;hasRetractionDate'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:apply-templates select="../@publication-format"/>
    </xsl:template>

    <xsl:template match="@date-type[. = 'received']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'frbr:realization'" tunnel="yes"/>
            <xsl:with-param name="o" select="$e" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$e" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Expression'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="set-date">
            <xsl:with-param name="s" select="$e" tunnel="yes"/>
            <xsl:with-param name="p" select="'fabio:hasDateReceived'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;hasDateReceived'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:apply-templates select="../@publication-format">
            <xsl:with-param name="e" select="$e" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="@date-type[. = 'rev-recd']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $w,'frbr:realization','',
                    'rdf:type','&fabio;Expression',
                    'frbr:revision',$e)"
            />
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$e" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Expression'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="set-date">
            <xsl:with-param name="s" select="$e" tunnel="yes"/>
            <xsl:with-param name="p" select="'fabio:hasDateReceived'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;hasDateReceived'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:apply-templates select="../@publication-format">
            <xsl:with-param name="e" select="$e" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="@date-type[. = 'rev-request']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="set-date">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'fabio:hasRevisionRequestDate'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;hasRevisionRequestDate'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@date-type[. = 'pub']">
        <xsl:message>TRANS: Found unprocessed attribute ""@date-type[. = 'pub']"</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@deceased[. = 'yes']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&trait;Dead'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@equal-contrib">
        <xsl:message>TRANS: Found unprocessed attribute "@equal-contrib"</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>



    <xsl:template match="@id|@object-id[@object-id-type != 'doi']">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'dcterms:identifier'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@initials">
        <xsl:call-template name="attribute">
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@mimetype">
        <xsl:message>TRANS: Found unprocessed attribute @mimetype</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@object-id-type[. = 'doi']">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'prism:doi'" tunnel="yes"/>
            <xsl:with-param name="o" select="../@object-id" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template
        match="@person-group-type[some $translator in ('translator','translators') satisfies . = $translator]">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'rdf:type','&foaf;Group',
                'pro:holdsRoleInTime','',
                'pro:withRole','&pro;translator',
                'pro:relatesToDocument',$e)"
            />
        </xsl:call-template>

        <xsl:if test="empty(../name)">
            <xsl:call-template name="attribute">
                <xsl:with-param name="p" select="'foaf:name'" tunnel="yes"/>
                <xsl:with-param name="o" select=".." tunnel="yes"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template
        match="@person-group-type[some $author in ('author','authors') satisfies . = $author]">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'rdf:type','&foaf;Group',
                'pro:holdsRoleInTime','',
                'pro:withRole','&pro;author',
                'pro:relatesToDocument',$e)"
            />
        </xsl:call-template>

        <xsl:if test="empty(../name)">
            <xsl:call-template name="attribute">
                <xsl:with-param name="p" select="'foaf:name'" tunnel="yes"/>
                <xsl:with-param name="o" select=".." tunnel="yes"/>
            </xsl:call-template>
        </xsl:if>

    </xsl:template>

    <xsl:template match="@person-group-type">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="e" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'rdf:type','&foaf;Group',
                'pro:holdsRoleInTime','',
                'pro:relatesToDocument',$e,
                'pro:withRole','',
                'rdf:type','&pro;Role',
                'rdfs:label',concat('&quot;',.,'&quot;'))"
            />
        </xsl:call-template>

        <xsl:if test="empty(../name)">
            <xsl:call-template name="attribute">
                <xsl:with-param name="p" select="'foaf:name'" tunnel="yes"/>
                <xsl:with-param name="o" select=".." tunnel="yes"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@publication-format">
        <xsl:message>TRANS: Found unprocessed attribute @publication-format</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>
    <xsl:template match="@publication-type[. = 'book']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Book'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@publication-type[. = 'letter']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;Letter'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@publication-type[. = 'journal']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;JournalArticle'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@publication-type[. = 'confproc']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;ConferencePaper'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@publication-type[. = 'patent']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;PatentDocument'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@publication-type[. = 'report']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;ReportDocument'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@publication-type[. = 'web']">
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;WebPage'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@publication-type">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $e,'rdf:type','',
                'rdf:type','&owl;Class',
                'rdfs:label', concat('&quot;',.,'&quot;'),
                'rdfs:subClassOf','&fabio;Expression')"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'art-access-id']">
        <xsl:message>TRANS: Found unprocessed attribute value "@pub-id-type[. =
            'art-access-id']</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'arxiv']">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasArXivId'" tunnel="yes"/>
            <xsl:with-param name="o" select=".." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'coden']">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasCODEN'" tunnel="yes"/>
            <xsl:with-param name="o" select=".." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'doi']">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'prism:doi'" tunnel="yes"/>
            <xsl:with-param name="o" select=".." tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'owl:sameAs'" tunnel="yes"/>
            <xsl:with-param name="o" select="concat($doiOrgUri, ..)" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'doaj']">
        <xsl:message>TRANS: Found unprocessed attribute value "@pub-id-type[. =
            'doaj']</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'isbn']">
        <xsl:message>TRANS: Found unprocessed attribute value "@pub-id-type[. =
            'isbn']</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'manuscript']">
        <xsl:message>TRANS: Found unprocessed attribute value "@pub-id-type[. =
            'manuscript']</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>
    <xsl:template match="@pub-id-type[. = 'medline']">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasPubMedId'" tunnel="yes"/>
            <xsl:with-param name="o" select=".." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'other']">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'dcterms:identifier'" tunnel="yes"/>
            <xsl:with-param name="o" select=".." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'pii']">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasPII'" tunnel="yes"/>
            <xsl:with-param name="o" select=".." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'pmcid']">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasPubMedCentralId'" tunnel="yes"/>
            <xsl:with-param name="o" select=".." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'pmid']">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasPubMedId'" tunnel="yes"/>
            <xsl:with-param name="o" select=".." tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'rdfs:seeAlso'" tunnel="yes"/>
            <xsl:with-param name="o" select="concat($pubmedNcbiUrl, ..)" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="single">
            <xsl:with-param name="p" select="'owl:sameAs'" tunnel="yes"/>
            <xsl:with-param name="o" select="concat($pubmedIdentifiersUrl, ..)" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'publisher-id']">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="(
                $s,'datacite:hasIdentifier','',
                    'rdf:type','&datacite;Identifier',
                    'datacite:usesIdentifierScheme','&datacite;local-resource-identifier-scheme',
                    'literal:hasLiteralValue',concat('&quot;',..,'&quot;'),
                    'prov:wasAttributedTo','',
                        'rdf:type','&prov;Agent',
                        'rdf:type','&foaf;Organization',
                        'rdfs:label',concat('&quot;','PsychOpen','&quot;'))"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'sici']">
        <xsl:message>TRANS: Found unprocessed attribute value "@pub-id-type[. =
            'sici']</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@pub-id-type[. = 'std-designation']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:call-template name="double">
            <xsl:with-param name="s" select="$w" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;TechnicalStandard'" tunnel="yes"/>
            <xsl:with-param name="p2" select="'fabio:hasStandardNumber'" tunnel="yes"/>
            <xsl:with-param name="o2" select=".." tunnel="yes"/>
        </xsl:call-template>
        <xsl:message>TRANS: Found unprocessed attribute value "@pub-id-type[. =
            'std-designation']</xsl:message>
    </xsl:template>

    <xsl:template match="@pub-type[. = 'epub']">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="me" select="replace($e, 'textual-entity', 'digital-embodiment')"/>
        <xsl:call-template name="set-manifestation">
            <xsl:with-param name="s" select="$e" tunnel="yes"/>
            <xsl:with-param name="p" select="'frbr:embodiment'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;DigitalManifestation'" tunnel="yes"/>
            <xsl:with-param name="m" select="$me" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="set-date">
            <xsl:with-param name="s" select="$me" tunnel="yes"/>
            <xsl:with-param name="p" select="'prism:publicationDate'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&prism;publicationDate'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-type[. = 'ppub']">
        <xsl:param name="e" tunnel="yes"/>
        <xsl:variable name="me" select="replace($e, 'textual-entity', 'digital-embodiment')"/>

        <xsl:call-template name="set-manifestation">
            <xsl:with-param name="s" select="$e" tunnel="yes"/>
            <xsl:with-param name="p" select="'frbr:embodiment'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&fabio;PrintObject'" tunnel="yes"/>
            <xsl:with-param name="m" select="$me" tunnel="yes"/>
        </xsl:call-template>

        <xsl:call-template name="set-date">
            <xsl:with-param name="s" select="$me" tunnel="yes"/>
            <xsl:with-param name="p" select="'prism:publicationDate'" tunnel="yes"/>
            <xsl:with-param name="o" select="'&prism;publicationDate'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@pub-type[. = 'epub-ppub']">
        <xsl:message>TRANS: Found unprocessed attribute value "@pub-type[. =
            'epub-ppub']</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@pub-type[. = 'epreprint']">
        <xsl:message>TRANS: Found unprocessed attribute value "@pub-type[. =
            'epreprint']</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@pub-type[. = 'ppreprint']">
        <xsl:message>TRANS: Found unprocessed attribute value "@pub-type[. =
            'ppreprint']</xsl:message>
    </xsl:template>

    <xsl:template match="@pub-type[. = 'ecorrected']">
        <xsl:message>TRANS: Found unprocessed attribute value "@pub-type[. =
            'ecorrected']</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@pub-type[. = 'pcorrected']">
        <xsl:param name="w" tunnel="yes"/>
        <xsl:message>TRANS: Found unprocessed attribute value "@pub-type[. =
            'pcorrected']</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@pub-type[. = 'eretracted']">
        <xsl:message> TRANS: Found unprocessed attribute value "@pub-type[. =
            'eretracted']</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template match="@pub-type[. = 'pretracted']">
        <xsl:message> TRANS: Found unprocessed attribute value "@pub-type[. =
            'pretracted']</xsl:message>
        <xsl:call-template name="goahead"/>
    </xsl:template>

    <xsl:template
        match="@rid[parent::contrib and (some $a in //(aff|aff-alternatives) satisfies $a/@id = .)]">
        <xsl:variable name="type" select="."/>
        <xsl:apply-templates select="//(aff|aff-alternatives)[@id = $type]" mode="xref"/>
    </xsl:template>

    <xsl:template match="@seq">
        <xsl:call-template name="attribute">
            <xsl:with-param name="p" select="'fabio:hasSequenceIdentifier'" tunnel="yes"/>
            <xsl:with-param name="o" select="." tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="@xml:lang[parent::article]">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="($s,'dcterms:language','','rdf:type','&dcterms;LinguisticSystem','dcterms:description',concat('&quot;',.,'&quot;','^^&dcterms;RFC5646'))"
            />
        </xsl:call-template>
    </xsl:template>
    <!-- END - Mapping attributes -->

    <!-- BEGIN - Named templates -->
    <!-- It takes as input a sequence of strings like (s,p1,o1,p2,o2,...) where s is the subjects of the statements while p_i and o_i represents respectively the predicates and the objects of each statement. It is possible to activate a reification strategy using the o_i as empty strings. -->
    <xsl:template name="assert">
        <xsl:param name="triples" as="xs:string*"/>
        <xsl:param name="prev-subject" as="xs:string*"/>
        <xsl:param name="reification" select="false()" as="xs:boolean"/>

        <!-- Go ahead only if the have three resources to make the statement -->
        <xsl:if test="count($triples) >= 3">
            <xsl:variable name="subject" select="$triples[1]" as="xs:string"/>
            <xsl:choose>
                <!-- Use the parent description -->
                <xsl:when test="$prev-subject = $subject and not($reification)">
                    <xsl:call-template name="create-structure">
                        <xsl:with-param name="triples" select="$triples"/>
                    </xsl:call-template>
                </xsl:when>
                <!-- Create a new description for the subject -->
                <xsl:otherwise>
                    <rdf:Description>
                        <!-- Add @rdf:about if the subject is not a blank node (i.e. an empty string) -->
                        <xsl:if test="$subject">
                            <xsl:attribute name="rdf:about">
                                <xsl:value-of select="$subject"/>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:call-template name="create-structure">
                            <xsl:with-param name="triples" select="$triples"/>
                        </xsl:call-template>
                    </rdf:Description>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template name="create-structure">
        <xsl:param name="triples" as="xs:string*"/>

        <xsl:variable name="subject" select="$triples[1]" as="xs:string"/>
        <xsl:variable name="predicate" select="$triples[2]" as="xs:string"/>
        <xsl:variable name="object" select="$triples[3]" as="xs:string"/>


        <xsl:element name="{$predicate}">
            <xsl:choose>
                <!-- Create the object as a literal -->
                <xsl:when test="starts-with($object,'&quot;')">
                    <xsl:variable name="value"
                        select="substring-before(substring-after($object,'&quot;'),'&quot;')"/>
                    <xsl:variable name="lang" select="substring-after($object,'@')"/>
                    <xsl:variable name="type" select="substring-after($object,'^^')"/>
                    <xsl:if test="string-length($lang)=2">
                        <xsl:attribute name="xml:lang">
                            <xsl:value-of select="$lang"/>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$type">
                        <xsl:attribute name="rdf:datatype" select="$type"/>
                    </xsl:if>
                    <xsl:value-of select="$value"/>
                </xsl:when>
                <!-- Create the object as a resource -->
                <xsl:when test="$object">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="$object"/>
                    </xsl:attribute>
                </xsl:when>
                <!-- Create a reification (i.e. the object is an empty string) -->
                <xsl:otherwise>
                    <xsl:call-template name="assert">
                        <xsl:with-param name="triples" select="('',subsequence($triples,4))"
                            as="xs:string*"/>
                        <xsl:with-param name="prev-subject" select="$subject"/>
                        <xsl:with-param name="reification" select="true()"/>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
        <!-- If the object exists, create another statement (according to $triples) using the same subject -->
        <xsl:if test="$object">
            <xsl:call-template name="assert">
                <xsl:with-param name="triples" select="($subject,subsequence($triples,4))"
                    as="xs:string*"/>
                <xsl:with-param name="prev-subject" select="$subject"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="attribute">
        <xsl:param name="s" as="xs:string" tunnel="yes"/>
        <xsl:param name="p" as="xs:string" tunnel="yes"/>
        <xsl:param name="o" as="xs:string" tunnel="yes"/>
        <xsl:param name="type" as="xs:string" select="''" tunnel="yes"/>
        <xsl:param name="lang" as="xs:string" select="''" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples"
                select="($s,$p,concat('&quot;',$o,'&quot;',if ($lang) then concat('@',$lang) else '', if ($type) then concat('^^',$type) else ''))"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="author-comment">
        <xsl:message>TRANS: unprocessed address-tag encountered</xsl:message>
    </xsl:template>

    <xsl:template name="date-assert">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="p" tunnel="yes"/>
        <xsl:param name="o" tunnel="yes"/>
        <xsl:param name="date"/>
        <xsl:choose>
            <xsl:when test="@calendar | season">
                <xsl:call-template name="assert">
                    <xsl:with-param name="triples"
                        select="(
                        $s,'literal:hasLiteral','',
                        'rdf:type',$o,
                        'literal:hasLiteralValue',concat('&quot;',$date,'&quot;'))"
                    />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="assert">
                    <xsl:with-param name="triples" select="($s,$p,$date)"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="double">
        <xsl:param name="s" as="xs:string" tunnel="yes"/>
        <xsl:param name="p" as="xs:string" tunnel="yes"/>
        <xsl:param name="o" as="xs:string" tunnel="yes"/>
        <xsl:param name="p2" as="xs:string" tunnel="yes"/>
        <xsl:param name="o2" as="xs:string" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples" select="($s,$p,$o, if ($p2 = '') then $p else $p2, $o2)"
            />
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="apply">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:apply-templates select="attribute()"/>
        <xsl:apply-templates select=".">
            <xsl:with-param name="lang"
                select="if (not(self::article) and @xml:lang) then @xml:lang else $lang"
                tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template name="goahead">
        <xsl:param name="lang" tunnel="yes"/>
        <xsl:param name="nodes" select="."/>
        <xsl:apply-templates select="$nodes/attribute()"/>
        <xsl:apply-templates select="$nodes/element()">
            <xsl:with-param name="lang"
                select="if (not($nodes/self::article) and $nodes/@xml:lang) then $nodes/@xml:lang else $lang"
                tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template name="set-date">
        <xsl:param name="s" tunnel="yes"/>
        <xsl:param name="p" tunnel="yes"/>
        <xsl:param name="o" tunnel="yes"/>
        <xsl:param name="el" select=".."/>
        <xsl:choose>
            <xsl:when test="$el/(season|@calendar)">
                <xsl:variable name="date" select="concat($s,'-date')"/>
                <xsl:call-template name="single">
                    <xsl:with-param name="p" select="'literal:hasLiteral'" tunnel="yes"/>
                    <xsl:with-param name="o" select="$date" tunnel="yes"/>
                </xsl:call-template>
                <xsl:call-template name="attribute">
                    <xsl:with-param name="p" select="'literal:hasLiteralValue'" tunnel="yes"/>
                    <xsl:with-param name="o" select="f:getDate($el)" tunnel="yes"/>
                    <xsl:with-param name="type" select="f:getDatetype($el)" tunnel="yes"/>
                </xsl:call-template>
                <xsl:apply-templates select="@calendar|season">
                    <xsl:with-param name="s" select="$date" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="attribute">
                    <xsl:with-param name="o" select="f:getDate($el)" tunnel="yes"/>
                    <xsl:with-param name="type" select="f:getDatetype($el)" tunnel="yes"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="set-manifestation">
        <xsl:param name="m" tunnel="yes"/>
        <!-- current manifestation -->
        <xsl:call-template name="single">
            <xsl:with-param name="s" select="$m" tunnel="yes"/>
            <xsl:with-param name="p" select="'rdf:type'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:if test="../@date-type != 'pub'">
            <xsl:call-template name="single">
                <!-- set Work/Expression relation to the current manifestation -->
                <xsl:with-param name="o" select="$m" tunnel="yes"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="set-manifestation-date">
        <xsl:param name="m" tunnel="yes"/>
        <!-- current manifestation -->
        <xsl:call-template name="set-manifestation"/>
        <xsl:if test="empty(../@data-type)">
            <xsl:call-template name="set-date">
                <xsl:with-param name="s" select="$m" tunnel="yes"/>
                <xsl:with-param name="p" select="dcterms:date" tunnel="yes"/>
                <xsl:with-param name="o" select="'&dcterms;date'" tunnel="yes"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="single">
        <xsl:param name="s" as="xs:string" tunnel="yes"/>
        <xsl:param name="p" as="xs:string" tunnel="yes"/>
        <xsl:param name="o" as="xs:string" tunnel="yes"/>

        <xsl:call-template name="assert">
            <xsl:with-param name="triples" select="($s,$p,$o)"/>
        </xsl:call-template>
    </xsl:template>
    <!-- END - Named templates -->

    <!-- BEGIN: functions -->
    <xsl:function name="f:getDatetype" as="xs:string">
        <xsl:param name="el" as="element()"/>
        <xsl:choose>
            <xsl:when test="$el/month">
                <xsl:choose>
                    <xsl:when test="$el/day">
                        <xsl:value-of select="'&xsd;date'"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="'&xsd;gYearMonth'"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'&xsd;gYear'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="f:getDate" as="xs:string">
        <xsl:param name="el" as="element()"/>
        <xsl:variable name="date">
            <xsl:if test="$el/year">
                <xsl:value-of select="$el/year"/>
            </xsl:if>
            <xsl:if test="$el/month">
                <xsl:text>-</xsl:text>
                <xsl:value-of select="$el/month"/>
            </xsl:if>
            <xsl:if test="$el/day">
                <xsl:text>-</xsl:text>
                <xsl:value-of select="$el/day"/>
            </xsl:if>
        </xsl:variable>
        <xsl:value-of select="$date"/>
    </xsl:function>

    <xsl:function name="f:getJournalName" as="xs:string">
        <xsl:param name="jTitle" as="xs:string"/>
        <xsl:value-of select="concat($journalUriInfix,f:urlEncode($jTitle))"/>
    </xsl:function>

    <xsl:function name="f:urlEncode" as="xs:string">
        <xsl:param name="str" as="xs:string"/>
        <xsl:value-of select="replace(encode-for-uri($str), '%20', '')"/>
    </xsl:function>
    <!-- END: functions -->
</xsl:stylesheet>
