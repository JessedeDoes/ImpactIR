package nl.namescape.tagging;

import java.util.Map;

import org.w3c.dom.Element;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.MultiThreadedFileHandler;
import nl.namescape.util.Options;
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
	
	/**
	 * Usage: args = <taggerModel> <lexicon> <inputDir> <outputDir> 
	 * @param args
	 */
	public static void main(String[] args)
	{
		nl.namescape.util.Options options = new nl.namescape.util.Options(args);
        args = options.commandLine.getArgs();
		Tagger taggerLemmatizer = 
				SimplePatternBasedLemmatizer.getTaggerLemmatizer(args[0], args[1]);
		ImpactTaggerLemmatizerClient xmlLemmatizer = 
				new ImpactTaggerLemmatizerClient(taggerLemmatizer);
		xmlLemmatizer.tokenize = Options.getOptionBoolean("tokenize", true);
		//MultiThreadedFileHandler m = new MultiThreadedFileHandler(xmlLemmatizer,2); 
		DirectoryHandling.tagAllFilesInDirectory(xmlLemmatizer, args[2], args[3]);
		//m.shutdown();
	}
}
