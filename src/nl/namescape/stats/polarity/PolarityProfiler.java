package nl.namescape.stats.polarity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import nl.namescape.evaluation.Counter;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.MultiThreadedFileHandler;
import nl.namescape.stats.MakeFrequencyList;
import nl.namescape.stats.WordList;
import nl.namescape.stats.WordList.TypeFrequency;
import nl.namescape.tei.Metadata;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PolarityProfiler implements nl.namescape.filehandling.DoSomethingWithFile
{
	int nTokens = 0;
	SentimentLexicon lexicon = new SentimentLexicon(SentimentLexicon.douman);
	//WordList tf = new WordList();
	Counter<String> negativeCounter = new Counter<String>();
	Counter<String> positiveCounter = new Counter<String>();
	Counter<String> wordCounter = new Counter<String>();
	int combinedNegativeFrequency=0;
	int combinedPositiveFrequency=0;
	int windowSize = 5;
	int maxWordsToPrint = 10000;
	int frequencyThreshold=20;
	boolean useLemmata = true;
	boolean lowercaseLemmata = true;
	String tagToLookFor = "ADJ" ; // "WW";
	Integer yearFrom= null; // 1850;
	Integer yearTo= null; // 1940;
	public boolean filterDocuments = false;
	
	public void print()
	{
		//tf.sortByFrequency();
		List<String> allWords = wordCounter.keyList();
		List<String> frequentWords = allWords.subList(0, Math.min(maxWordsToPrint,allWords.size()));
		PolarityComparator p = new PolarityComparator();
		Collections.sort(frequentWords, p);
		for (String w: frequentWords)
		{
			int f = wordCounter.get(w);
			if (f >= frequencyThreshold)
				System.out.println(w +  " " + wordCounter.get(w)  + " " + getPolarity(w) + " +: " + positiveCounter.get(w) + " -: " + negativeCounter.get(w));
		}
	}
	
	private synchronized void incrementWordCount()
	{
		nTokens++;
	}
	
	public double getPolarity(String lemma)
	{
		int f = wordCounter.get(lemma);
		int PLUS = this.positiveCounter.get(lemma);
		int MINUS = this.negativeCounter.get(lemma);
		double tokLog = Math.log(nTokens);
		double logPos = Math.log(combinedPositiveFrequency);
		double logNeg = Math.log(combinedNegativeFrequency);
		double logF = Math.log(f);
		
		double dPlus = 0;
		double dMin = 0 ; 
		
		if (PLUS > 0) dPlus = tokLog + Math.log(PLUS) -  logF  - logPos;
		
		if (MINUS > 0) dMin = tokLog + Math.log(MINUS) -  logF  - logNeg;
		// System.err.println(lemma  + " + " + PLUS +  " - " + MINUS);
		return dPlus - dMin;
	}
	
	boolean filter(Document d)
	{
		if (yearFrom != null && yearTo != null)
		{
			Metadata m = new Metadata(d);
			String wYearFrom = m.getValue("witnessYear_from");
			String wYearTo = m.getValue("witnessYear_from");
			try
			{
				System.err.println(wYearFrom + "--" + wYearTo);
				int yf = Integer.parseInt(wYearFrom);
				int yt = Integer.parseInt(wYearTo);
				return (yt >= yearFrom && yf <= yearTo);
			} catch (Exception e)
			{
				return false;
			}
		}
		return true;
	}
	
	public void handleFile(String fileName) 
	{
		try
		{
			Document d = XML.parse(fileName);
			if (filterDocuments && !filter(d))
			{
				return;
			} else
			{
				if (yearFrom != null) System.err.println("filter accepts " + fileName);
			}
			List<Element> sentences = nl.namescape.tei.TEITagClasses.getSentenceElements(d.getDocumentElement());
			if (sentences.size() == 0)
			{
				System.err.println("no sentences, just using element text!");
				sentences = XML.getElementsByTagname(d.getDocumentElement(), "text", false);
			}
			for (Element s: sentences)
			{
				List<Element> tokens = nl.namescape.tei.TEITagClasses.getWordElements(s);
				for (int i=0; i < tokens.size(); i++)
				{
					Element w = tokens.get(i);

					this.incrementWordCount();

					
					String wordform = w.getTextContent();
					String lemma = useLemmata?w.getAttribute("lemma"):wordform;
					if (this.lowercaseLemmata) lemma = lemma.toLowerCase();
					String tag = w.getAttribute("function");
					if (tag == null || tag.isEmpty())
					  tag = w.getAttribute("type");
				
					
					if (tag == null || tag.isEmpty())
						tag = w.getAttribute("ctag");
				
					//System.err.println(wordform + " " + lemma + " " + tag);
					SentimentLexicon.Polarity p;
					if ((p = lexicon.getPolarity(lemma)) != null)
					{
						if (p == SentimentLexicon.Polarity.PLUS)
						{
							combinedPositiveFrequency++;
						} else if ((p == SentimentLexicon.Polarity.MINUS))
						{
							combinedNegativeFrequency++;
						}
					}
					if (useLemmata && !tag.startsWith(tagToLookFor)) // ahem, this is not needed....
					{
						continue;
					}
					wordCounter.increment(lemma);
					for (int j=i-1; j >=0 && i-j <= windowSize; j--)
					{ 
						Element w1 = tokens.get(j);
						considerContextWord(lemma, w1);
					}
					
					for (int j=i+1; j < tokens.size() && j-i <= windowSize; j++)
					{ 
						Element w1 = tokens.get(j);
						considerContextWord(lemma, w1);
					}
				}
			} 

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void considerContextWord(String lemma, Element w1) 
	{
		String lemma1 = useLemmata?w1.getAttribute("lemma"):w1.getTextContent().trim();
		if (this.lowercaseLemmata) lemma1 = lemma1.toLowerCase();
		SentimentLexicon.Polarity p1;
		if ((p1 = lexicon.getPolarity(lemma1)) != null)
		{
			if (p1 == SentimentLexicon.Polarity.PLUS)
			{
				positiveCounter.increment(lemma);
			} else if ((p1 == SentimentLexicon.Polarity.MINUS))
				negativeCounter.increment(lemma);
		}
	}
	
	public class PolarityComparator implements Comparator<String> 
	{

		Map<String,Integer> base;
		
		public PolarityComparator(Map<String,Integer> _base) 
		{
			//System.err.println(_base);
			this.base = _base;
		}
		
		public PolarityComparator()
		{
			
		}

		public int compare(String a, String b) 
		{
			if (getPolarity(a) < getPolarity(b)) 
			{
				return 1;
			} else if(getPolarity(a) == getPolarity(b)) 
			{
				return a.compareTo(b);
			} else 
			{
				return -1;
			}
		}
	}
	
	public static void main(String[] args)
	{
		PolarityProfiler s = new PolarityProfiler();
		int startAt = 0;
		if (args.length > 1)
		{
		   String PoS = args[0];
		   s.useLemmata = true;
		   s.tagToLookFor = PoS;
		   startAt=1;
		}
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(s,4);
		if (args.length > 0)
		{	
			for (int i = startAt; i < args.length; i++)
			{	
				DirectoryHandling.traverseDirectory(m,args[i]);
				m.shutdown();
			}
			s.print();
		}
		else
			DirectoryHandling.traverseDirectory(s,"N:/Taalbank/CL-SE-Data/Corpora/GrootModernCorpus/parole-boeken");
	}	
}
