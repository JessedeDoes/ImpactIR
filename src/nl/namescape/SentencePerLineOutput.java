package nl.namescape;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import nl.namescape.sentence.JVKSentenceSplitter;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.XML;
import nl.openconvert.filehandling.DoSomethingWithFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.util.Set;

public class SentencePerLineOutput implements nl.openconvert.filehandling.SimpleInputOutputProcess, DoSomethingWithFile
{
	boolean tagParts = true;
	private Properties properties;
	PrintStream stdout = new PrintStream(System.out);
	boolean printLemmata = false;
	boolean printIds = false;
	boolean onlyDecentSentences = false;
	
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
				String lemma = t.getAttribute("lemma");
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
				
				outLine += (first? "":" ") + ((printLemmata && lemma != null && lemma.length()>0)?lemma:token);
				first = false;
			}
			
			if (!onlyDecentSentences || firstIsUpper && nLowercase / (double) nCharacters > 0.7)
			{
				if (printIds)
				{
					out.print(s.getAttribute("xml:id") + "|");
				}
				out.println(outLine);
			} else
				nSkippedLines++;
			nLines++;
		}
		System.err.println("skipped " + nSkippedLines + " of " + nLines);
		
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
			pout.close();
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
		nl.namescape.util.Options options = new nl.namescape.util.Options(args)
		{
			@Override
			public void defineOptions()
			{
				super.defineOptions();
				options.addOption("n", "nThreads", true, "Number of threads");
				options.addOption("i", "printIds", true, "Print Ids");
			}
		};
		SentencePerLineOutput spl = new SentencePerLineOutput();
		spl.printIds = options.getOptionBoolean("printIds", false);
        args = options.commandLine.getArgs();
		if (args.length > 1)
		{
			nl.openconvert.filehandling.DirectoryHandling.tagAllFilesInDirectory(spl, args[0], 
				args[1]);
		} else
		{
			nl.openconvert.filehandling.DirectoryHandling.traverseDirectory(spl, args[0]);
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
