package nl.namescape.tagging;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Element;

import impact.ee.lemmatizer.Lemmatizer;
import impact.ee.lemmatizer.dutch.SimplePatternBasedLemmatizer;
import impact.ee.tagger.LookupLemmatizer;
import impact.ee.tagger.Tagger;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.MultiThreadedFileHandler;
import nl.namescape.util.XML;
import impact.ee.util.Options;

public class LookupLemmatizerClient extends ImpactTaggingClient
{
	LookupLemmatizer ll = null;
	Lemmatizer lemmatizer = null;
	public LookupLemmatizerClient(LookupLemmatizer ll2) 
	{
		super(ll2);
	}

	public LookupLemmatizerClient()
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
		this.tagger = ll;
		this.tokenize = true;
		//p.getProperty("tokenize").equalsIgnoreCase("true");
	}

	public void attachToElement(Element e, Map<String,String> m)
	{
		// e.setAttribute("type", tag);
		// System.err.println(m);
		String tag = m.get("tag");

		if (tag != null)
			e.setAttribute("type", tag);

		String lemma = m.get("lemma");
		if (lemma != null)
			e.setAttribute("lemma", lemma);
		String mform = m.get("mform");
		if (mform != null)
			e.setAttribute("mform", lemma);

		String allMatches = m.get("allMatches");
		if (allMatches != null)
		{
			org.w3c.dom.Document d = XML.parseString(allMatches);
			if (d != null)
			{
				List<Element> interpGrps = XML.getElementsByTagname(d.getDocumentElement(), "interpGrp", false);
				
				for (Element i: interpGrps)
				{
					org.w3c.dom.Node i1 = e.getOwnerDocument().importNode(i, true);
					e.appendChild(i1);
				}
			}
		}
	}

	public void close()
	{
		if (this.lemmatizer != null)
			this.lemmatizer.close();
	}
	
	public static void main(String[] args)
	{
		System.err.println(args[0]);
		impact.ee.util.Options options = new impact.ee.util.Options(args);
		options.list();
		args = options.commandLine.getArgs();
		System.err.println(Options.getOption("patternInput"));
		impact.ee.lemmatizer.Lemmatizer lemmatizer = new Lemmatizer(
				Options.getOption("patternInput"),
				Options.getOption("modernLexicon"), 
				Options.getOption("historicalLexicon"), 
				Options.getOption("lexiconTrie"));
		LookupLemmatizer ll = new LookupLemmatizer(lemmatizer);
		LookupLemmatizerClient x = new LookupLemmatizerClient(ll);
		x.tokenize = Options.getOptionBoolean("tokenize", true);

		//MultiThreadedFileHandler m = new MultiThreadedFileHandler(x,3);

		System.err.println("Start tagging from " + args[0] + " to " + args[1]);
		DirectoryHandling.tagAllFilesInDirectory(x, args[0], args[1]);
		//m.shutdown();
	}
}
