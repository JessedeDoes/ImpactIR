package nl.namescape.tagging;

import impact.ee.tagger.*;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.SimpleInputOutputProcess;
import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.sentence.TEITokenStream;

import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.XML;

import org.w3c.dom.*;

import nl.namescape.tei.TEITagClasses;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class ImpactTaggingClient implements SimpleInputOutputProcess
{
	Tagger tagger;
	
	
	public ImpactTaggingClient(Tagger tagger)
	{
		this.tagger = tagger;
	}
	
	public void tagDocument(Document d)
	{
		try 
		{
			long startTime = System.currentTimeMillis();
			int nWords = 0;
			TEITokenStream inputCorpus = new TEITokenStream(d);
			Corpus out = tagger.tag(inputCorpus);
			Map<String, String> tagMap = new HashMap<String, String>();
			for (Context c : out.enumerate()) {
				String tag = c.getAttributeAt("tag", 0);
				String id = c.getAttributeAt("id", 0);
				//System.err.println(id + "->"  + tag);
				tagMap.put(id, tag);
				nWords++;
			}
			for (Element e : TEITagClasses.getTokenElements(d)) {
				String tag = tagMap.get(e.getAttribute("xml:id"));
				if (tag != null)
					e.setAttribute("type", tag);
			}
			long endTime = System.currentTimeMillis();
			long interval = endTime - startTime;
			double secs = interval / 1000.0;
			double wps = nWords / secs;
			System.err.println("tokens " + nWords);
			System.err.println("seconds " + secs);
			System.err.println("tokens per second " + wps);
		} catch (Exception e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void handleFile(String in, String out) 
	{
		boolean tokenize = true;
		Document d = null;
		if (tokenize)
		{
			TEITokenizer tok = new TEITokenizer();
			d = tok.getTokenizedDocument(in, true);
			new TEISentenceSplitter(new JVKSentenceSplitter()).splitSentences(d);
		} else
		{
			try 
			{
				d = XML.parse(in);
			} catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		try 
		{
			PrintStream pout = new PrintStream(new FileOutputStream(out));
			tagDocument(d);
			pout.print(XML.documentToString(d));
		} catch (FileNotFoundException e) 
		{
			
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		BasicTagger b = new BasicTagger();
		b.loadModel(args[0]);
		ImpactTaggingClient xmlTagger = new ImpactTaggingClient(b);
		DirectoryHandling.tagAllFilesInDirectory(xmlTagger, args[1], args[2]);
	}
}
