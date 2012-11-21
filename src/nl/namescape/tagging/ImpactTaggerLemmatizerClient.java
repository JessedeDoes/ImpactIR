package nl.namescape.tagging;

import java.util.Map;

import org.w3c.dom.Element;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.MultiThreadedFileHandler;
import impact.ee.lemmatizer.dutch.SimplePatternBasedLemmatizer;
import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.tagger.Tagger;

public class ImpactTaggerLemmatizerClient extends ImpactTaggingClient {

	public ImpactTaggerLemmatizerClient(Tagger tagger) 
	{
		super(tagger);
		this.tokenize = false;
		// TODO Auto-generated constructor stub
	}

	public void attachToElement(Element e, Map<String,String> m)
	{
		// e.setAttribute("type", tag);
		if (e.getLocalName().equals("w"))
		{
			String lemma = m.get("lemma");
			if (lemma != null)
				e.setAttribute("lemma", lemma);
			String tag = m.get("tag");
			if (tag != null)
				e.setAttribute("lemma", tag);
		}
	}
	
	public static void main(String[] args)
	{
		Tagger taggerLemmatizer = 
				SimplePatternBasedLemmatizer.getTaggerLemmatizer(args[0], args[1]);
		ImpactTaggerLemmatizerClient xmlLemmatizer = 
				new ImpactTaggerLemmatizerClient(taggerLemmatizer);
		//MultiThreadedFileHandler m = new MultiThreadedFileHandler(xmlLemmatizer,2); 
		DirectoryHandling.tagAllFilesInDirectory(xmlLemmatizer, args[1], args[2]);
		//m.shutdown();
	}
}
