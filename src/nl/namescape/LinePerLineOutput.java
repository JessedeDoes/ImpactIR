package nl.namescape;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.util.Set;

public class LinePerLineOutput implements nl.namescape.filehandling.SimpleInputOutputProcess, DoSomethingWithFile
{
	boolean tagParts = true;
	private Properties properties;
	PrintStream stdout = new PrintStream(System.out);
	boolean printLemmata = false;
	int lineLength = 10;
	
	public LinePerLineOutput(int len) 
	{
		this.lineLength = len;
		// TODO Auto-generated constructor stub
	}

	public void printSentences(Document d, PrintStream out)
	{
		//Map<String,Set<String>> metadataMap = nl.namescape.tei.Metadata.getMetadata(d);
		
		//List<Element> sentences = nl.namescape.tei.TEITagClasses.getSentenceElements(d);
		int nLines=0;
		int nSkippedLines=0;
		
		List<Element> tokens = 	
				nl.namescape.tei.TEITagClasses.getTokenElements(d.getDocumentElement());
		
		int k = 0;
		while (k < tokens.size())
		{
			int variation = (int) (Math.round(0.5 * (-1 * lineLength + Math.random() * lineLength)));
			int chunkLength = lineLength + variation;
			boolean printed = false;
			for (int i=k; i < k+chunkLength && i < tokens.size(); i++)
			{
				stdout.print(tokens.get(i).getTextContent() + " ");
				printed = true;
			}
			k += chunkLength;
			if (printed && chunkLength > 0)
			{
				stdout.print("\n");
			}
		}
		
		//System.err.println("skipped " + nSkippedLines + " of " + nLines);
		
	}
	
	public static boolean sentenceHasEnoughLowercaseCharacters(Element s)
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
		
		return ( nLowercase / (double) nCharacters > 0.7);
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

	@Override
	public void setProperties(Properties properties) 
	{
		// TODO Auto-generated method stub
		this.properties = properties;
	}
	

	@Override
	public void handleFile(String fileName) 
	{
		try 
		{
			Document d = XML.parse(fileName);
			
			printSentences(d, stdout);
			stdout.flush();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		int len = Integer.parseInt(args[0]);
		if (args.length > 2)
		{
			nl.namescape.filehandling.DirectoryHandling.tagAllFilesInDirectory(new LinePerLineOutput(len), args[0], 
				args[2]);
		} else
		{
			nl.namescape.filehandling.DirectoryHandling.traverseDirectory(new LinePerLineOutput(len), args[1]);
		}
	}
}
