package nl.namescape.stats;

import impact.ee.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.util.MultiMap;
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

public class NameFrequencyList implements nl.namescape.filehandling.DoSomethingWithFile
{
	WordList tf = new WordList();
	int nTokens = 0;
	
	enum Type {word, lemma, lwt};
	Type type = Type.word;
	MultiMap<String,Element> examples = new MultiMap<String,Element> ();
	
	public void handleFile(String fileName) 
	{
		System.err.println(fileName);
		try
		{
			Document d = XML.parse(fileName);
			List<Element> sentences = TEITagClasses.getSentenceElements(d);
			for (Element s: sentences)
			{
				List<Element> namez = XML.getElementsByTagname(s, "ns:ne", false);
				for (Element e: namez)
				{
					nTokens++;

				
					String wordform = getNameText(e);
					examples.putValue(wordform, s);
					tf.incrementFrequency(wordform, 1); 
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String getNameText(Element e)
	{
		List<String> parts = new ArrayList<String>();
		for (Element w: XML.getElementsByTagname(e, "w", false))
		{
			parts.add(w.getTextContent().trim());
		}
		return e.getAttribute("type") + ": "  + StringUtils.join(parts, " ");
	}
	public void print()
	{
		tf.sortByFrequency();
		for (WordList.TypeFrequency x: tf.keyList(true))
		{
			System.out.println(x.type + "\t" + x.frequency);
			Set<Element> e = examples.get(x.type);
			System.out.println("\t"  + e.iterator().next().getTextContent().replaceAll("\\s+", " ").trim());
		}
	}

	public static void main(String[] args)
	{
		NameFrequencyList s = new NameFrequencyList();
		s.type = Type.word;
		
		if (args.length > 0)
		{
			for (String d: args)
				DirectoryHandling.traverseDirectory(s,d);
			s.print();
		}
		else
		{
			DirectoryHandling.traverseDirectory(s,
					"N:/Taalbank/Namescape/Corpus-Sanders/Data/NER");
			s.print();
		}
	}
}
