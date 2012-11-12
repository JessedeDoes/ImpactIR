package nl.namescape.tokenizer;
import impact.ee.tagger.BasicTagger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.w3c.dom.Document;

import nl.namescape.filehandling.*;
import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tagging.ImpactTaggingClient;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.util.XML;


public class TEIUntokenizer implements SimpleInputOutputProcess
{
	@Override
	public void handleFile(String in, String out) 
	{
	

		try 
		{
			Document d = XML.parse(in);
			TEITagClasses.removeTokenization(d);
			PrintStream pout = new PrintStream(new FileOutputStream(out));
			pout.print(XML.documentToString(d));
			pout.close();
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static void main(String[] args)
	{
		TEIUntokenizer b = new TEIUntokenizer();
		DirectoryHandling.tagAllFilesInDirectory(b, args[0], args[1]);
	}
}
