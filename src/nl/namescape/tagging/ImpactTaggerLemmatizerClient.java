package nl.namescape.tagging;

import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Element;

import nl.namescape.util.Options;
import nl.openconvert.filehandling.DirectoryHandling;
import nl.openconvert.filehandling.MultiThreadedFileHandler;
import impact.ee.lemmatizer.dutch.MultiplePatternBasedLemmatizer;
import impact.ee.lemmatizer.dutch.SimplePatternBasedLemmatizer;
import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.tagger.Tagger;

public class ImpactTaggerLemmatizerClient extends ImpactTaggingClient 
{
	String taggingModel = null;
	String lexiconPath = null;
	boolean logFeatures = false;
	
	public ImpactTaggerLemmatizerClient()
	{
		
	}
	
	public ImpactTaggerLemmatizerClient(Tagger tagger) 
	{
		super(tagger);
		this.tokenize = false; // Hm wil je dit wel zo?
		// TODO Auto-generated constructor stub
	}

	/**
	 * final boolean isOtherSymbol = 
      ( int ) Character.OTHER_SYMBOL
       == Character.getType( character.charAt( 0 ) );
    final boolean isNonUnicode = isOtherSymbol 
      && character.getBytes()[ 0 ] == ( byte ) 63;
      
      The lemmatizer sometimes inserts unicode illegal characters.
      If lemma does not pass this test, do not tag it in the TEI file
	 */
	
	public boolean CheckIllegalCharacters(String s)
	{
		for (int i=0; i < s.length(); i++)
		{
			if (Character.getType(s.charAt(i)) == Character.OTHER_SYMBOL)
			{
				nl.openconvert.log.ConverterLog.defaultLog.printf("dangerous character in %s\n", s);
				return false;
			}
		}
		return true;
	}
	
	public void attachToElement(Element e, Map<String,String> m)
	{
		// e.setAttribute("type", tag);
		if (e.getNodeName().contains("w"))
		{
			String lemma = m.get("lemma"); // to do check for illegal characters....
			if (lemma != null && CheckIllegalCharacters(lemma))
				e.setAttribute("lemma", lemma);
			else
				e.setAttribute("lemma", "_NONE_");
			String tag = m.get("tag");
			if (tag != null)
				e.setAttribute("type", tag);
			String features = m.get("features");
			if (this.logFeatures && features != null)
				e.setAttribute("features", features);
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
		  String t = p.getProperty("tokenize");
		 
		  if (t != null && t.equalsIgnoreCase( "true"))
			  this.tokenize  = true;
		  if (t != null && t.equalsIgnoreCase( "false"))
			  this.tokenize  =  false;
		  
		 Tagger taggerLemmatizer = 
					MultiplePatternBasedLemmatizer.getTaggerLemmatizer(taggingModel,
							lexiconPath, p);
		 this.tagger = taggerLemmatizer;
	}
	
	public static void main(String[] args)
	{
		nl.namescape.util.Options options = new nl.namescape.util.Options(args)
		{
			@Override
			public void defineOptions()
			{
				super.defineOptions();
				options.addOption("n",  "nThreads", true, "Number of threads");
			}
		};
		
        args = options.commandLine.getArgs();
        int nThreads = Runtime.getRuntime().availableProcessors()-1;
        if (options.getOption("nThreads") != null)
        {
        	try
        	{
        		nThreads = Integer.parseInt(options.getOption("nThreads"));
        	} catch (Exception e)
        	{
        		nl.openconvert.log.ConverterLog.defaultLog.println("Error in threads option");
        	}
        }
		Tagger taggerLemmatizer = 
				MultiplePatternBasedLemmatizer.getTaggerLemmatizer(args[0], args[1], options.properties);
		ImpactTaggerLemmatizerClient xmlLemmatizer = 
				new ImpactTaggerLemmatizerClient(taggerLemmatizer);
		xmlLemmatizer.tokenize = options.getOptionBoolean("tokenize", true);
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(xmlLemmatizer,nThreads);
		nl.openconvert.log.ConverterLog.defaultLog.println("Start tagging from " + args[2] + " to " + args[3]);
		DirectoryHandling.usePathHandler = false;
		DirectoryHandling.traverseDirectory(m, args[2], args[3], null);
		m.shutdown();
	}
}
