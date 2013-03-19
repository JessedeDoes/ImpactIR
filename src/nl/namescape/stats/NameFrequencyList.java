package nl.namescape.stats;

import impact.ee.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import nl.namescape.evaluation.Counter;
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
	Map<String, Counter<String>> typeMap = new HashMap<String, Counter<String>>();
	public boolean typeSensitive = true;
	
	public void handleFile(String fileName) 
	{
		System.err.println(fileName);
		try
		{
			Document d = XML.parse(fileName);
			processDocument(d);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void processDocument(Document d) 
	{
		List<Element> sentences = TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
			List<Element> namez = XML.getElementsByTagname(s, "ns:ne", false);
			for (Element e: namez)
			{
				nTokens++;
				String wordform = getNameText(e);
				String type = e.getAttribute("type");
				if (!typeSensitive)
				{
					Counter<String> types = typeMap.get(wordform);
					if (types == null)
					{
						types = new Counter<String>();
						typeMap.put(wordform, types);
					}
					types.increment(type);
				}
				examples.putValue(wordform, s);
				tf.incrementFrequency(wordform, 1); 
			}
		}
	}

	public String getNameText(Element e)
	{
		List<String> parts = new ArrayList<String>();
		for (Element w: XML.getElementsByTagname(e, "w", false))
		{
			parts.add(w.getTextContent().trim());
		}
		if (typeSensitive)
			return e.getAttribute("type") + "\t"  + StringUtils.join(parts, " ");
		else
			return  StringUtils.join(parts, " ");
	}

	public int getFrequency(String name)
	{
		return tf.getFrequency(name);
	}
	
	public Set<String> keySet()
	{
		return tf.keySet(true);
	}
	
	public Counter<String> getTypes(String name)
	{
		return typeMap.get(name);
	}
	
	public Set<Element> getExamples(String name)
	{
		return this.examples.get(name);
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
