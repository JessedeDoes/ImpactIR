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

public class SentencePerLineOutput implements nl.namescape.filehandling.SimpleInputOutputProcess
{
	boolean tagParts = true;

	public void printSentences(Document d, PrintStream out)
	{
		Map<String,Set<String>> metadataMap = nl.namescape.tei.Metadata.getMetadata(d);
		
		List<Element> sentences = nl.namescape.tei.TEITagClasses.getSentenceElements(d);
		int nLines=0;
		int nSkippedLines=0;
		for (Element s: sentences)
		{
			
			List<Element> tokens = 	nl.namescape.tei.TEITagClasses.getTokenElements(s);
			boolean first = true;
			String outLine = "";
			int nLowercase=0;
			int nCharacters=0;
			boolean firstIsUpper=false;
			for (Element t: tokens)
			{
				String token = t.getTextContent();
				nCharacters += token.length();
				
				for (int i=0; i < token.length(); i++)
				{
					char c  = token.charAt(i);
					if (first && i==0)
					{
						firstIsUpper = Character.isLetter(c) && Character.isUpperCase(c);
					}
					if (Character.isLetter(c) && Character.isLowerCase(c))
						nLowercase++;
				}
				
				outLine += (first? "":" ") + token;
				first = false;
			}
			
			if (firstIsUpper && nLowercase / (double) nCharacters > 0.7)
			{
				out.println(outLine);
			} else
				nSkippedLines++;
			nLines++;
		}
		System.err.println("skipped " + nSkippedLines + " of " + nLines);
		
	}
	
	
	@Override
	public void handleFile(String in, String out) 
	{
		try 
		{
			Document d = XML.parse(in);
			PrintStream pout = new PrintStream(new FileOutputStream(out));
			printSentences(d, pout);
		} catch (Exception e) 
		{
			
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		nl.namescape.filehandling.DirectoryHandling.tagAllFilesInDirectory(new SentencePerLineOutput(), args[0], 
				args[1]);
	}
}
