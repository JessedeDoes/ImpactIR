package nl.namescape.stats;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.MultiThreadedFileHandler;
import nl.namescape.sentence.TEISentenceSplitter;
import nl.namescape.util.Options;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;




/*
 * ARF:
 * v = N / f
 * ARF = 1/v * sum(i=1..f) min(di,v)
 * Maar wat is di met verschillende documenten? zet gelijk aan v...
 
 * d1 = n1 + (N-nf)
 */

public class MakeFrequencyList implements nl.namescape.filehandling.DoSomethingWithFile
{
	WordList tf = new WordList();
	int nTokens = 0;
	
	enum Type {word, lemma, lwt};
	Type type = Type.word;

	public void handleFile(String fileName) 
	{
		try
		{
			Document d = XML.parse(fileName);
			if (!TEISentenceSplitter.allWordsAreInSentences(d))
			{
				System.err.println("Unwrapped words in " + fileName);
			}
			List<Element> tokens = nl.namescape.tei.TEITagClasses.getWordElements(d.getDocumentElement());
			for (Element e: tokens)
			{
				handleToken(e);
			}
		} catch (Exception e)
		{
			System.err.println("Parse error in " + fileName);
			e.printStackTrace();
		}
	}

	private synchronized void handleToken(Element e) 
	{
		nTokens++;
		
		String lemma = e.getAttribute("lemma");
		String wordform = e.getTextContent();
		String tag = e.getAttribute("type");
		String lwt = wordform + "\t" + tag + "\t" + lemma;
		
		switch (type)
		{
			case word: tf.incrementFrequency(wordform, 1); break;
			case lemma: tf.incrementFrequency(lemma, 1); break;
			case lwt: tf.incrementFrequency(lwt, 1); break;
		}
	}

	public void print()
	{
		tf.sortByFrequency();
		for (WordList.TypeFrequency x: tf.keyList())
		{
			System.out.println(x.type + "\t" + x.frequency);
		}
	}

	public static void main(String[] args)
	{
		MakeFrequencyList s = new MakeFrequencyList();
		Options o = new Options(args)
		{
			public void defineOptions() 
			{
				options.addOption("t", "type", true, "word");
			}
		};
		args = o.commandLine.getArgs();
		s.type = Type.word;
		String t;
		
		if ((t = o.getOption("type")) != null)
		{
			if (t.equals("word"))
				s.type = Type.word;
			if (t.equals("lemma"))
				s.type = Type.lemma;
			if (t.equals("lwt"))
				s.type = Type.lwt;
		}
		
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(s,Runtime.getRuntime().availableProcessors()-1);
		if (args.length > 0)
		{
			for (String d: args)
				DirectoryHandling.traverseDirectory(m,d);
			m.shutdown();
			s.print();
		}
		else
			DirectoryHandling.traverseDirectory(s,"N:/Taalbank/CL-SE-Data/Corpora/GrootModernCorpus/parole-boeken");
	}
}
