## KOBPSY ##
An RDF-knowledgebase for scholarly articles in psychology. The RDF representations have been generated from the articles' JATS-XML representations using the SE4OJS tool (see description below). </br>

- Article metadata, such as title, authors, keywords, references etc.

- article text structure, such as sections, paragraphs, paragraphs' textual content and in-text citations have been rdfized and the
- textual content has been semantically annotated with ontologies from bioportal and the UMLS using the **SE4OJS** tool (see description below). 

A sample of 5 articles and their RDF-representations from KOBPSY is available at: /se4ojs/resources/data/annotation.zip.

## SE4OJS User documentation ##
### Overview
The application consists of several modules, rdfizing different aspects of an xml article in JATS- 1.0 format, using a variety of ontologies, especially the [SPAR ontologies](http://sempublishing.sourceforge.net/ "SPAR ontologies") and the [Annotation Ontology](https://code.google.com/p/annotation-ontology/ "Annotation Ontology"). It was inspired by the [Biotéa](http://www.jbiomedsem.com/content/4/S1/S5) project with whom it shares similarities in workflow and output.
#####Modules
-	**client-module:** Acts as a controller, steering the  
workflow. Contains the main class "SE4OJSRdfizer".
- **jats2Spar-module:** RDFizes the article  metadata. Uses an XSLT-Stylesheet (based on: [*Peroni, S. Lapeyre D.A., and Shotton D. (2012). From Markup to Linked Data: Mapping NISO JATS v1.0 to RDF using the SPAR (Semantic Publishing and Referencing) Ontologies*](http://www.ncbi.nlm.nih.gov/books/NBK100491/)) to transform the xml into rdf.
 </br> Produces an rdf file in the output directory, containing article metadata representations, e.g. on article identifiers, journal, issue, article title, keywords, article contributors, their affiliations, article references and their authors.
- **textStructure-module:** RDFizes the textual structure, i.e. sections, paragraph, paragraph text and in-text citations. Produces the in-text citation count for each reference. Outputs a second .rdf-file to the output directory containing this information.
- **annotation-common-module:** Contains shared classes used by all content-annotation components.
- **annotation-ncbo-module:** Automatically annotates the textual content of the article with user-defined bioportal ontologies, using the [NCBO annotator web-service](http://data.bioontology.org/documentation#nav_annotator). Creates an annotation context, relating the concept to the text location where it occurs. Keeps track of the frequency of occurrence of each concept. Outputs a third rdf-file containing this information.
- **annotation-umls-module:** Automatically annotates the textual content of the article with user-defined UMLS ontologies, using the java api of the MetaMap tool. It is a prerequisite to running this module that an accessible MetaMap service is running. Produces the same annotations and output like the annotation-ncbo module.

All rdfization steps are optional. But both annotators depend on the text-structure rdfization step.
The annotators try to ignore text in other languages than English. This is achieved by checking whether the section or paragraph containing the text has a language attribute-value that specifies another language than English. Once such an attribute-value is encountered, the annotator skips this textual element including all its child-elements.   
###Project Setup###


- Currently, no user interface or packaged binary is offered for download – the sources need to be compiled and the program must run from either command-line or an IDE. 
- The project consists of several maven modules -> make sure maven is installed on your machine to compile the projects
- SE4OJS requires Java7
#####Running SE4OJS


- **Main class**. The entry point, i.e. the main method to run the application is se4ojs\sources\client\src\main\java\org\zpid\se4ojs\app\SE4OJSRdfizer.java
- Possible **commandline-arguments** are:

	-  `“-in” “path/to/dir”`	, *mandatory*. Input directory containing the JATS-xml Files (Version 1.0). The directory only processes .xml files. It may contain other files or nested directories, (e.g. the pdf representations of the articles) which will be ignored. A path to a single file is also possible.

	- `“-out” “path/to/dir”`	, *mandatory*. Output directory for the generated rdf-files

	- `“-metadata”`, *optional*. Rdfizes article-metadata (calls the jats2Spar module). Produces an rdf file in the output dir containing article metadata representations.

	- `“-structure”	`, *optional*;*mandatory if annotators are also specified*. Rdfizes the text structure. Produces an rdf file in the output dir containing article metadata representations, in text citations and citation frequency information for references.
	- `“-ncboAnnotator”`		Optional; must be used in conjunction with “-structure”
	
	- `“-umlsAnnotator”`	MetaMap must be set up and 	Optional; must be used in conjunction with “-structure”
**Configuration**
	</br> The configuration file is located at: </br>
    *client/src/main/resources/config.properties*
	- **Article language** (comma-separated list of  ISO 639 2 or character language codes). Articles in other languages are skipped and are not being processed. </br> Example: `languagesIncluded=en,de,fr`
	- **Base URI** (Base URI for all persistent and non-persistent URIs required for the creation of rdf-resources) </br>
	Example: `baseUri=http://www.zpid.de/resource`

	- **NCBO annotator**. You need to acquire an API-key in order to access the annotator. </br> *Example*:  ncbo.service.url=http://data.bioontology.org/annotator
    ncbo.annotator.url=http://bioportal.bioontology.org/annotator/
    ncbo.apikey=*[your API-key here]*
ncbo.annotator.ontologies=ONTOAD,NIFSTD,GALEN,SIO,BIOMO,AURA,RADLEX
	- **UMLS annotator**.  </br> *Example*: umls.annotator.ontologies=MSH,PSY,NCI,HL7V3.0,RCD,LNC,CSP,ICNP

### Sample Setup for Eclipse
- Import the maven projects into Eclipse (first install the projects with maven from the commandline (`mvn clean:install`); run maven's eclipse plugin  `mvn eclipse:eclipse`, then import the projects into the IDE; In Eclipse Luna: `File->Import->Existing Maven projects`
- Create an Eclipse-Run-Configuration:
#### Sample Eclipse Run-configuration (includes all possible rdfization steps).
[img1!](se4ojs/resources/doc/se4ojsRunConfig1.jpg)
[img1!](se4ojs/resources/doc/se4ojsRunConfig2.png)



