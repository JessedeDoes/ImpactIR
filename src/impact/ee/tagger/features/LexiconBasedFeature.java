package impact.ee.tagger.features;


import impact.ee.classifier.*;
import impact.ee.lexicon.*;
import impact.ee.ner.gazetteer.Gazetteer;

import java.io.ObjectInputStream;
import java.util.*;



public class LexiconBasedFeature extends StochasticFeature
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ILexicon lexicon = null; // why transient??
	private boolean initialized = false;

	//static final String databaseHost = "svowim02";
	//static final String database = "EE3_5";
	//static final String JVKLex = "resources/exampledata/type_lemma_pos.tab";

	static Map<String, ILexicon> lexiconMap = 
			new  HashMap<String, ILexicon>();

	public void initLexicon()
	{
		//lexicon = new LexiconDatabase(databaseHost, database);
		initLexicon(TaggerFeatures.getLexiconFileName());
	}

	public void initLexicon(String fileName)
	{
		if (lexicon != null || initialized)
			return;
		if (lexiconMap.containsKey(fileName))
			lexicon = lexiconMap.get(fileName);
		else
		{
			System.err.println("Reading lexicon...");
			InMemoryLexicon iml = new InMemoryLexicon();
			iml.readFromFile(fileName);
			lexiconMap.put(fileName, iml);
			TaggerFeatures.putNamedObject("tagLexicon", iml); // not really needed....
			lexicon = iml;
		}
		initialized = true;
	}

	private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		TaggerFeatures.putNamedObject("tagLexicon", this.lexicon);
		initLexicon(); // hoeft niet meer
	}
}
