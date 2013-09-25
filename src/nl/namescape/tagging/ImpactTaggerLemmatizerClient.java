package nl.namescape.tagging;

import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Element;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.MultiThreadedFileHandler;
import nl.namescape.util.Options;
import impact.ee.lemmatizer.dutch.SimplePatternBasedLemmatizer;
import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.tagger.Tagger;

public class ImpactTaggerLemmatizerClient extends ImpactTaggingClient 
{
	String taggingModel = null;
	String lexiconPath = null;
	
	public ImpactTaggerLemmatizerClient()
	{
		
	}
	
	public ImpactTaggerLemmatizerClient(Tagger tagger) 
	{
		super(tagger);
		this.tokenize = false; // Hm wil je dit wel zo?
		// TODO Auto-generated constructor stub
	}

	public void attachToElement(Element e, Map<String,String> m)
	{
		// e.setAttribute("type", tag);
		if (e.getNodeName().contains("w"))
		{
			String lemma = m.get("lemma");
			if (lemma != null)
				e.setAttribute("lemma", lemma);
			String tag = m.get("tag");
			if (tag != null)
				e.setAttribute("type", tag);
		} else // pc
		{
			e.removeAttribute("lemma");
		}
	}
	
	/**
	 * Usage: args = <taggerModel> <lexicon> <inputDir> <outputDir> 
	 * @param args
	 */
	
	public void setProperties(Properties p)
	{
		 taggingModel = p.getProperty("taggingModel");
		 lexiconPath = p.getProperty("lexiconPath");
		 Tagger taggerLemmatizer = 
					SimplePatternBasedLemmatizer.getTaggerLemmatizer(taggingModel,
							lexiconPath);
		 this.tagger = taggerLemmatizer;
	}
	
	public static void main(String[] args)
	{
		nl.namescape.util.Options options = new nl.namescape.util.Options(args);
        args = options.commandLine.getArgs();
		Tagger taggerLemmatizer = 
				SimplePatternBasedLemmatizer.getTaggerLemmatizer(args[0], args[1]);
		ImpactTaggerLemmatizerClient xmlLemmatizer = 
				new ImpactTaggerLemmatizerClient(taggerLemmatizer);
		xmlLemmatizer.tokenize = options.getOptionBoolean("tokenize", true);
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(xmlLemmatizer,3);
		System.err.println("Start tagging from " + args[2] + " to " + args[3]);
		DirectoryHandling.tagAllFilesInDirectory(m, args[2], args[3]);
		m.shutdown();
	}
}
