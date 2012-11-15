package nl.namescape.tagging;

import java.util.Map;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.MultiThreadedFileHandler;
import nl.namescape.tei.TEINameTagging;
import nl.namescape.util.Options;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import impact.ee.tagger.NamePartTagger;
import impact.ee.tagger.Tagger;

public class ImpactNERTaggingClient extends ImpactTaggingClient 
{

	public ImpactNERTaggingClient(Tagger tagger) 
	{
		super(tagger);
	}
	
	@Override
	public void attachToElement(Element e, Map<String,String> m)
	{
		// e.setAttribute("type", tag);
		String tag = m.get("tag");
		if (tag != null)
			e.setAttribute("neLabel", tag);
		tag = m.get("namePartTag");
		if (tag != null)
			e.setAttribute("nePartLabel", tag);
	}
	
	@Override 
	public void postProcess(Document d)
	{
		(new TEINameTagging()).realizeNameTaggingInTEI(d);
	}
	
	public static void main(String[] args)
	{
		nl.namescape.util.Options options = 
				new nl.namescape.util.Options(args);
		args = options.commandLine.getArgs();

		Tagger namePartTagger = 
				NamePartTagger.getNamePartTagger(args[0], args[1]);
		ImpactNERTaggingClient x = new ImpactNERTaggingClient(namePartTagger);
		x.tokenize = Options.getOptionBoolean("tokenize", true);
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(x,4);
		DirectoryHandling.tagAllFilesInDirectory(m, args[2], args[3]);
		m.shutdown();
	}
}
