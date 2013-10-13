package nl.namescape.stats;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.filehandling.MultiThreadedFileHandler;

import org.w3c.dom.Element;
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
public class WordCounter extends DefaultHandler implements DoSomethingWithFile
{
	int nWords=0;
	int nFiles=0;
	int nParseErrors=0;
	Set<String> filesWithParseError = new HashSet<String>();
	SAXParserFactory factory = SAXParserFactory.newInstance();

	public WordCounter()
	{
		try 
		{
			//saxParser = factory.newSAXParser();
		} catch (Throwable err) 
		{
			err.printStackTrace ();
		}
	}

	public  void startElement(String uri, String localName, String qName, Attributes attributes) 
	{
		
		if (qName.equals("w"))
		{	
			incrementWordCount();
		}
	}

	private synchronized void incrementWordCount()
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


	@Override
	public void handleFile(String fileName) 
	{
		// TODO Auto-generated method stub
		incrementFileCount();
		if (nFiles % 1000 == 0)
			System.err.println(nFiles + " "+ fileName);
		try 
		{
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse( new File(fileName), this);
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			addToErrors(fileName);
			e.printStackTrace();
		} 
	}

	public static void main(String[] args)
	{
		WordCounter x = new WordCounter();
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(x,Runtime.getRuntime().availableProcessors());
		DirectoryHandling.traverseDirectory(m, args[0]);
		m.shutdown();
		System.out.println(x.nWords + " words in "  + x.nFiles + " files");
		System.err.println(x.filesWithParseError.size() + " parse errors");
		for (String s: x.filesWithParseError)
		{
			System.err.println("\t" + s);
		}
	}
}
