package nl.namescape.tagging;

import java.util.Map;

import org.w3c.dom.Element;

import nl.namescape.filehandling.DirectoryHandling;
import impact.ee.lemmatizer.dutch.SimplePatternBasedLemmatizer;
import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.tagger.Tagger;

public class ImpactLemmatizerClient extends ImpactTaggingClient {

	public ImpactLemmatizerClient(Tagger tagger) 
	{
		super(tagger);
		// TODO Auto-generated constructor stub
	}

	public void attachToElement(Element e, Map<String,String> m)
	{
		// e.setAttribute("type", tag);
		String tag = m.get("lemma");
		if (tag != null)
			e.setAttribute("lemma", tag);
	}
	
	public static void main(String[] args)
	{
		InMemoryLexicon l = new InMemoryLexicon();
		l.readFromFile(args[0]);
		SimplePatternBasedLemmatizer spbl = new SimplePatternBasedLemmatizer();
		spbl.train(l);
		ImpactLemmatizerClient xmlLemmatizer = new ImpactLemmatizerClient(spbl);
		DirectoryHandling.tagAllFilesInDirectory(xmlLemmatizer, args[1], args[2]);
	}
}
