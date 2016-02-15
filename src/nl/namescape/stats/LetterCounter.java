package nl.namescape.stats;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nl.openconvert.filehandling.DirectoryHandling;
import nl.openconvert.filehandling.DoSomethingWithFile;
import nl.openconvert.filehandling.MultiThreadedFileHandler;
import nl.openconvert.tei.TEITagClasses;
import nl.openconvert.util.XML;

import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * Counts "w" tags in tokenized XML files.
 * 
 * @author does
 *
 */
public class LetterCounter extends DefaultHandler implements DoSomethingWithFile
{
	int nWords=0;
	int nFiles=0;
	int nParseErrors=0;
	Set<String> filesWithParseError = new HashSet<String>();
	SAXParserFactory factory = SAXParserFactory.newInstance();
   int[] letterCounts = new int[Character.MAX_CODE_POINT+1];
   
	public LetterCounter()
	{
		try 
		{
			//saxParser = factory.newSAXParser();
			for (int i=0; i < letterCounts.length; i++)
				letterCounts[i] = 0;
		} catch (Throwable err) 
		{
			err.printStackTrace ();
		}
	}

	public  void startElement(String uri, String localName, String qName, Attributes attributes) 
	{
		
		if (qName.equals("w"))
		{	
			//incrementWordCount(w);
		}
	}

	private synchronized void incrementWordCount(String w)
	{
		nWords++;
	}

	private synchronized void incrementFileCount()
	{
		nFiles++;
	}
	
	private synchronized void addToErrors(String fileName)
	{
		filesWithParseError.add(fileName);
	}


	private synchronized void increaseLetterCount(char b)
	{
		letterCounts[b]++;
		nWords++;
	}
	
	@Override
	public void handleFile(String fileName) 
	{
		// TODO Auto-generated method stub
		incrementFileCount();
		if (nFiles % 1000 == 0)
			System.err.println(nFiles + " "+ fileName);
		try 
		{
			//SAXParser saxParser = factory.newSAXParser();
			//saxParser.parse(new File(fileName), this);
			Document d = XML.parse(fileName);
			List<Element> words = TEITagClasses.getWordElements(d.getDocumentElement());
			for (Element e: words)
			{
				String w = e.getTextContent();
				if (w != null && w.length() > 0)
				{
					char b = w.toLowerCase().charAt(0);
					increaseLetterCount(b);
				}
			}
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			addToErrors(fileName);
			e.printStackTrace();
		} 
	}

	public static void main(String[] args)
	{
		LetterCounter x = new LetterCounter();
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(x,Runtime.getRuntime().availableProcessors());
		DirectoryHandling.traverseDirectory(m, args[0]);
		m.shutdown();
		System.out.println(x.nWords + " words in "  + x.nFiles + " files");
		for (char c='a'; c <= 'z'; c++)
			System.out.println(c + "\t" + x.letterCounts[c]);
		
		System.err.println(x.filesWithParseError.size() + " parse errors");
		for (String s: x.filesWithParseError)
		{
			System.err.println("\t" + s);
		}
	}
}
