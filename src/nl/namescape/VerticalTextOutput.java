package nl.namescape;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.util.Set;

public class VerticalTextOutput implements nl.namescape.filehandling.SimpleInputOutputProcess
{
	boolean tagParts = true;
	boolean useCTAG = false;
	public void printForSketchEngine(Document d, PrintStream out)
	{
		Map<String,Set<String>> metadataMap = nl.namescape.tei.Metadata.getMetadata(d);
		out.print("<doc");
		  
		out.println(">");
		List<Element> sentences = nl.namescape.tei.TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
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
