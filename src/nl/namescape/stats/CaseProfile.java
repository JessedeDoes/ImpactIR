package nl.namescape.stats;

import java.util.List;

import nl.namescape.stats.MakeFrequencyList.Type;
import nl.namescape.stats.WordList.TypeFrequency;
import nl.namescape.util.XML;
import nl.openconvert.filehandling.DirectoryHandling;

import org.w3c.dom.Document;
import org.w3c.dom.Element;




import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * From a TEI tokenized corpus (with s and w tags), gather a 
 * profile of predominancy of lower/upper case usage for (almost)
 * all words.
 * 
 * @author does
 *
 */
public class CaseProfile  implements java.io.Serializable, 
	nl.openconvert.filehandling.DoSomethingWithFile
{
	WordList tf = new WordList();
	int nTokens = 0;
	Map<String,Counter> counts = new ConcurrentHashMap<String,Counter>();

	class Counter
	{
		int count=0;
		int lcCount=0;
		int ucCount=0;
		double proportion=0;
		
		String key;
		Set<String> variants = new HashSet<String>();

		public Counter(String s)
		{
			this.key = s;
		}

		public synchronized void count(String s, int f)
		{
			count += f;
			variants.add(s);
			String lower = s.toLowerCase();
			if (lower.equals(s))
			{
				lcCount+= f;
			} else
			{
				ucCount+= f;
			}
			proportion = ucCount / (double) count;
		}
		
		public String toString()
		
		{
			return key + "\t" + proportion + "\t" +  lcCount + "\t"+ ucCount + "\t"  + variants;
		}
	}

	public Double getUpperCaseProportion(String s)
	{
		Counter c = counts.get(s.toLowerCase());
		if (c == null)
			return null;
		return c.proportion;
	}
	
	public void handleFile(String fileName) 
	{
		System.err.println(fileName);
		try
		{
			Document d = XML.parse(fileName);
			handleDocument(d);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void handleDocument(Document d) 
	{
		List<Element> sentences =  nl.namescape.tei.TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
			List<Element> tokens = nl.namescape.tei.TEITagClasses.getWordElements(s);
			boolean first=true;
			String previousWord=null;
			for (Element e: tokens)
			{
				nTokens++;
				String wordform = e.getTextContent();
				if (!first)
				{
					countWord(wordform);
				}
				first = false;
				previousWord = wordform;
			}
		}
	}

	private synchronized void countWord(String wordform) 
	{
		tf.incrementFrequency(wordform, 1);
	}

	public void print()
	{
		makeProfile();
		
		for (Counter c: counts.values())
		{
			if (c.count > 2 && c.ucCount > 0) // assume lc for all types...
			{
				System.out.println(c);
			}
		}
	}

	public void makeProfile() 
	{
		List<TypeFrequency> sensitive = tf.keyList(true);
		for (TypeFrequency t: sensitive)
		{
			String lower = t.type.toLowerCase();
			// System.err.println(lower);
			Counter c = counts.get(lower);
			if (c == null)
				counts.put(lower, c = new Counter(lower));
			c.count(t.type,t.frequency);
		}
	}

	public static void main(String[] args)
	{
		CaseProfile s = new CaseProfile();

		if (args.length > 0)
		{
			for (String d: args)
				DirectoryHandling.traverseDirectory(s,d);
					//s.print();
		}
		s.print();
	}
}
