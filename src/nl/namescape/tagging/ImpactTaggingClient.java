package nl.namescape.tagging;

import impact.ee.tagger.*;
import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.sentence.TEITokenStream;
import nl.namescape.sentence.TEITokenStreamWithSentenceBounds;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.Options;
import nl.namescape.util.XML;

import org.w3c.dom.*;

import nl.namescape.tei.TEITagClasses;
import nl.openconvert.filehandling.ConversionException;
import nl.openconvert.filehandling.DirectoryHandling;
import nl.openconvert.filehandling.SimpleInputOutputProcess;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class ImpactTaggingClient implements SimpleInputOutputProcess, TaggerWithOptions
{
	Tagger tagger;
	public boolean tokenize = true;
	protected Properties properties;
	
	public void setTokenizing(boolean b)
	{
		this.tokenize = b;
	}
	
	public ImpactTaggingClient()
	{
		
	}
	
	public ImpactTaggingClient(Tagger tagger)
	{
		this.tagger = tagger;
	}
	
	public void attachToElement(Element e, Map<String,String> m)
	{
		// e.setAttribute("type", tag);
		String tag = m.get("tag");
		if (tag != null)
			e.setAttribute("type", tag);
	}
	
	public void postProcess(Document d)
	{
		
	}
	
	public void tagDocument(Document d)
	{
		try 
		{
			long startTime = System.currentTimeMillis();
			int nWords = 0;
			if (!tokenize)
			{
				boolean eek = TEITagClasses.fixIds(d);
				if (eek)
					nl.openconvert.log.ConverterLog.defaultLog.println("eek!!!");
			}
			TEITokenStream inputCorpus = new TEITokenStreamWithSentenceBounds(d);
			Corpus out = tagger.tag(inputCorpus);
			Map<String, Map<String,String>> tagMap = new HashMap<String, Map<String,String>>();
			for (Context c : out.enumerate()) 
			{
				Map<String,String> m = new HashMap<String,String>();
				for (String s: c.getAttributes())
				{
					m.put(s, c.getAttributeAt(s, 0));
				}
				String id = c.getAttributeAt("id", 0);
				if (id != null)
					tagMap.put(id, m);
				nWords++;
			}
			for (Element e : TEITagClasses.getTokenElements(d)) 
			{
				Map<String,String> tags = tagMap.get(e.getAttribute("xml:id"));
				if (tags != null)
					attachToElement(e,tags);
			}
			long endTime = System.currentTimeMillis();
			long interval = endTime - startTime;
			double secs = interval / 1000.0;
			double wps = nWords / secs;
			nl.openconvert.log.ConverterLog.defaultLog.println("tokens " + nWords);
			nl.openconvert.log.ConverterLog.defaultLog.println("seconds " + secs);
			nl.openconvert.log.ConverterLog.defaultLog.println("tokens per second " + wps);
		} catch (Exception e) 
		{
			e.printStackTrace();
			// System.exit(1); // why.... on earth ...
		}
	}
	
	@Override
	public void handleFile(String in, String out) throws ConversionException
	{
		Document d = null;
		if (tokenize)
		{
			try
			{
				TEITokenizer tok = new TEITokenizer();
				d = tok.getTokenizedDocument(in, true);
				new TEISentenceSplitter(new JVKSentenceSplitter()).splitSentences(d);
				nl.openconvert.log.ConverterLog.defaultLog.println("document has " + TEITagClasses.getNumberOfWords(d) + " words ");
			} catch (Exception e)
			{
				throw new ConversionException(e);
			}
		} else
		{
			nl.openconvert.log.ConverterLog.defaultLog.println("NOT tokenizing " + in);
			try 
			{
				d = XML.parse(in);
			} catch (Exception e) 
			{
				throw new ConversionException(e);
			} 
		}
		
		try 
		{
			PrintStream pout = new PrintStream(new FileOutputStream(out));
			tagDocument(d);
			postProcess(d);
			pout.print(XML.documentToString(d));
			pout.close();
		} catch (Exception e) 
		{
			throw new ConversionException(e);
			//e.printStackTrace();
		}
	}
	
	@Override
	public void setProperties(Properties properties) 
	{
		// TODO Auto-generated method stub
		this.properties = properties;
		 BasicTagger b = new BasicTagger();
                this.tagger = b;
                b.loadModel(properties.getProperty("taggingModel"));
	}
	
	public static void main(String[] args)
	{
		nl.namescape.util.Options options = new nl.namescape.util.Options(args);
        args = options.commandLine.getArgs();
		BasicTagger b = new BasicTagger();
		b.loadModel(args[0]);
		ImpactTaggingClient xmlTagger = new ImpactTaggingClient(b);
		xmlTagger.tokenize = options.getOptionBoolean("tokenize", true);
		DirectoryHandling.usePathHandler = false;
		DirectoryHandling.tagAllFilesInDirectory(xmlTagger, args[1], args[2]);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
