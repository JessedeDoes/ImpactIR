package nl.namescape;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.Util;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.util.Set;

public class VerticalTextOutput implements nl.namescape.filehandling.SimpleInputOutputProcess
{
	boolean tagParts = true;
	boolean useCTAG = false;
	boolean speakerDocs = true;
	
	public void printForSketchEngine(Document d, PrintStream out)
	{
		Element e = d.getDocumentElement();
		out.print("<file");
		  
		out.println(">");
		if (speakerDocs)
		{
			List<Element> sps = XML.getElementsByTagname(e, "sp", false);
			for (Element sp: sps)
			{
				Map<String,String> metadata = new HashMap<String,String>();
				
				try
				{
					Element speaker = XML.getElementsByTagname(sp, "speaker", false).get(0);
					metadata.put("speaker", getLemmaContent(speaker));
				} catch (Exception ex)
				{
					
				}
				printForSketchEngine(sp, out, metadata);
			}
		} else
		printForSketchEngine(e, out, null);
		out.println("</file>");
	}
	
	public String getLemmaContent(Element e)
	{
		if (e == null)
			return null;
		List<Element> words = 	nl.namescape.tei.TEITagClasses.getWordElements(e);
		if (words.size() == 0)
			return null;
		List<String> lemmata = new ArrayList<String>();
		for (Element w: words)
		{
			lemmata.add(w.getAttribute("lemma"));
		}
		return Util.join(lemmata, "_");
	}
	
	public void printForSketchEngine(Element e, PrintStream out, Map<String,String> metadata)
	{
		//Map<String,Set<String>> metadataMap = nl.namescape.tei.Metadata.getMetadata(d);
		out.print("<doc");
		if (metadata != null) for (String k: metadata.keySet())
			out.print(" " + k + "=" + "\"" + metadata.get(k) + "\"");
		out.println(">");
		List<Element> sentences = nl.namescape.tei.TEITagClasses.getSentenceElements(e);
		for (Element s: sentences)
		{
			List<Element> words = 	nl.namescape.tei.TEITagClasses.getWordElements(s);
			if (words.size() == 0)
				continue;
			out.println("<s>");
			List<Element> tokens = 	nl.namescape.tei.TEITagClasses.getTokenElements(s);
			for (Element t: tokens)
			{

				String tag = t.getAttribute("type");
				if (useCTAG)
					tag = t.getAttribute("ctag");
				
				String lemma = t.getAttribute("lemma");
				String word = t.getTextContent();
				
				out.println(word + "\t" + tag + "\t"  + lemma);
			}
			out.println("</s>");
		}
		out.println("</doc>");
	}
	
	
	@Override
	public void handleFile(String in, String out) 
	{
		try 
		{
			Document d = XML.parse(in);
			PrintStream pout = new PrintStream(new FileOutputStream(out));
			printForSketchEngine(d, pout);
		} catch (Exception e) 
		{
			
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		nl.namescape.util.Options options = new nl.namescape.util.Options(args);
        args = options.commandLine.getArgs();
		VerticalTextOutput v = new VerticalTextOutput();
		v.useCTAG = options.getOptionBoolean("ctag", false);
		nl.namescape.filehandling.DirectoryHandling.tagAllFilesInDirectory(v, args[0], 
				args[1]);
	}
}
