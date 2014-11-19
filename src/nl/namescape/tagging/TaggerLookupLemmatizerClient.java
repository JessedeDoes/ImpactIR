package nl.namescape.tagging;

import impact.ee.lemmatizer.Lemmatizer;
import impact.ee.tagger.ChainOfTaggers;
import impact.ee.tagger.LookupLemmatizer;
import impact.ee.tagger.BasicTagger;

import java.util.Properties;

public class TaggerLookupLemmatizerClient extends LookupLemmatizerClient 
{
	public TaggerLookupLemmatizerClient()
	{
		
	}
	
	public void setProperties(Properties p)
	{
		impact.ee.lemmatizer.Lemmatizer lemmatizer = new Lemmatizer(
				p.getProperty("patternInput"),
				p.getProperty("modernLexicon"), 
				p.getProperty("historicalLexicon"), 
				p.getProperty("lexiconTrie"));

		String m = p.getProperty("useMatcher");

		if (m != null)
			lemmatizer.setUseMatcher(m.equalsIgnoreCase("true"));

		this.lemmatizer = lemmatizer;
		
		LookupLemmatizer ll = new LookupLemmatizer(lemmatizer);
		
		this.tokenize = true;
		ChainOfTaggers t = new ChainOfTaggers();
		BasicTagger bTagger = new BasicTagger();
		bTagger.loadModel(p.getProperty("taggingModel"));
		t.addTagger(bTagger);
		t.addTagger(ll);
		
		this.tagger =  t;
		//p.getProperty("tokenize").equalsIgnoreCase("true");
	}
}
