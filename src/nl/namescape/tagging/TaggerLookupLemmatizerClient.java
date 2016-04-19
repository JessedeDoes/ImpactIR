package nl.namescape.tagging;

import impact.ee.lemmatizer.Lemmatizer;
import impact.ee.lemmatizer.tagset.WNTCorpusLexiconRelation;
import impact.ee.tagger.ChainOfTaggers;
import impact.ee.tagger.LookupLemmatizer;
import impact.ee.util.Options;
import nl.openconvert.filehandling.DirectoryHandling;
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

		p.list(System.out);
		String m = p.getProperty("useMatcher");

		if (m != null)
			lemmatizer.setUseMatcher(m.equalsIgnoreCase("true"));

		this.lemmatizer = lemmatizer;
		
		LookupLemmatizer ll = new LookupLemmatizer(lemmatizer);
		ll.setTagRelation(new WNTCorpusLexiconRelation());
		
		this.tokenize = true;
		ChainOfTaggers t = new ChainOfTaggers();
		BasicTagger bTagger = new BasicTagger();
		System.err.println("load tagging model...");
		bTagger.loadModel(p.getProperty("taggingModel"));
		System.err.println("tagging model loaded...");
		t.addTagger(bTagger);
		t.addTagger(ll);
		
		this.tagger =  t;
		//p.getProperty("tokenize").equalsIgnoreCase("true");
	}
	public static void main(String[] args)
	{
		nl.openconvert.log.ConverterLog.setDefaultVerbosity(true);
		nl.openconvert.log.ConverterLog.defaultLog.println(args[0]);
		 nl.namescape.util.Options  options = new nl.namescape.util.Options(args) 
		 {
			 public void defineOptions()
			 {
				 super.defineOptions();
				 options.addOption("t", "taggingModel", true, "taggingModel");
					options.addOption("p", "patternInput", true, "patternInput");
					options.addOption("H", "historicalLexicon", true, "historicalLexicon");
					options.addOption("m", "modernLexicon", true, "modernLexicon");
					options.addOption("u", "useMatcher", true, "useMatcher");
					options.addOption("T", "tokenize", true, "tokenize");
					options.addOption("l", "lexiconTrie", true, "lexiconTrie");
					options.addOption("s", "sentences", true, "add sentence splitting to already tokenized file");
					options.addOption("c", "ctag", true, "use ctag attribute for PoS");
					options.addOption("w", "word2vecFile", true, "name of word embedding file (in wor2vec format)");
			 }
		 };
		//options.list();
		args = options.commandLine.getArgs();
		nl.openconvert.log.ConverterLog.defaultLog.println(Options.getOption("patternInput"));
		TaggerLookupLemmatizerClient  t = new TaggerLookupLemmatizerClient();
		t.setProperties(options.properties);
		//t.tokenize = Options.getOptionBoolean("tokenize", true);

		//MultiThreadedFileHandler m = new MultiThreadedFileHandler(x,3);

		nl.openconvert.log.ConverterLog.defaultLog.println("Start tagging from " + args[0] + " to " + args[1]);
		DirectoryHandling.tagAllFilesInDirectory(t, args[0], args[1]);
		//m.shutdown();
	}
}
