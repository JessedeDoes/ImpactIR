package nl.namescape.stats;

import impact.ee.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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




/**
 * Prints a type frequency list of names entities to stdout and
 * examples (all occurrences) to stderr
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
		return e.getAttribute("type") + "\t"  + StringUtils.join(parts, " ");
	}

	public void print()
	{
		tf.sortByFrequency();
		int idNo=1;
		int exNo=1;
		for (WordList.TypeFrequency x: tf.keyList(true))
		{
			String justName = x.type.replaceAll(".*\t", "");
			System.out.println(idNo + "\t" + x.type + "\t" + justName +  "\t" +  x.frequency);
			Set<Element> e = examples.get(x.type);
			if (e != null)
			{
				Iterator<Element> i = e.iterator();
				while (i.hasNext())
				{
					Element s = i.next();
					String sentence = s.getTextContent().replaceAll("\\s+", " ").trim();
					System.err.println(exNo + "\t" + idNo + "\t"  + sentence);
					exNo++;
				}
			}
			idNo++;
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
