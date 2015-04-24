package nl.namescape.tagging;

import impact.ee.lemmatizer.dutch.SimplePatternBasedLemmatizer;
import impact.ee.lemmatizer.latin.LatinLemmatizer;
import impact.ee.tagger.Tagger;

import java.util.Properties;

public class LatinTaggerLemmatizer extends ImpactTaggerLemmatizerClient
{
	public void setProperties(Properties p)
	{
		 taggingModel = p.getProperty("taggingModel");
		 lexiconPath = p.getProperty("lexiconPath");
		 Tagger taggerLemmatizer = 
					LatinLemmatizer.getTaggerLemmatizer(taggingModel,
							lexiconPath, "impact.ee.lemmatizer.latin.LatinLemmatizer", new java.util.Properties());
		 this.tagger = taggerLemmatizer;
	}

}
