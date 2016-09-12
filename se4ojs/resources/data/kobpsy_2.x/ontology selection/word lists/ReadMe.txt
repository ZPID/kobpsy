This folder contains word lists extracted from the English part of the PsychOpen corpus.
A term has been included into file
- PsychOpen_texts_bigrams_mixedCase_NP_ADJsOnly_HighestScore_Threshold3.csv
or
- PsychOpen_texts_unigrams_mixedCase_withVerbs_HighestScore_Threshold3.csv
if the TF/IDF value in at least one document of the text collection was 3 or higher (values have been computed using the DKPro Keyphrases Software and using the default TF/IDF computation algorithm and the OpenNLP Part-of-Speech Tagger for pre-processing).

The two files:
- MinusBNC_wordlist_text_bigrams_mixedCase_ADJ_N_HighestScore_Threshold21.txt and
- MinusBNC_wordlist_text_unigrams_mixedCase_withVerbs_HighestScore_Threshold21.txt
 contain a subset of the former word-lists: Only terms with TF/IDF score higher than 20 are included. From this list, words occurring very frequently in the British National Corpus have been deleted. The terms are in a format ready to be consumed by the BioPortal Recommender Tool (i.e. a comma-separated list of terms, separated into chunks to be sent by an HTTP request).
