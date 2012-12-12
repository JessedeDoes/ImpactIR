package nl.namescape.stats.colloc;
import nl.namescape.evaluation.Counter;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.filehandling.MultiThreadedFileHandler;
import nl.namescape.stats.CaseProfile;
import nl.namescape.stats.WordList;
//import nl.namescape.stats.MakeFrequencyList.Type;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.util.XML;

import org.w3c.dom.*;

import java.util.*;

/**
 * !!!!!!Only for possible names, uses case information.
 * Try to use collocation information
 * For each sentence... [BLA]
 * @author does
 * should we make this n-pass (? rather NOT)
 * First a typefrequency pass; next a bigram pass, 
 * Next step: add all the rest. (?)
 * 
 * Cf: http://hlt.di.fct.unl.pt/jfs/ClustAnaClassNumEnt.pdf
 */

public class MultiwordNameExtractor implements DoSomethingWithFile
{
	WordList tf = new WordList();
	int minimumUnigramFrequency = 2;
	int minimumBigramFrequency = 2;
	CaseProfile caseProfile = new CaseProfile();
	
	enum Stage 
	{ 
		wordFrequency, 
		bigramFrequency, 
		nGrams
	}; 
	// for coherence, we would need a fourth stage to get frequencies of parts of possible multiword entities
	Stage stage= Stage.wordFrequency;
	
	enum Type {word, lemma, lwt};
	Type type = Type.word;
	long nTokens=0;
	
	
	Counter<WordNGram> bigramCounter = new Counter<WordNGram>();
	Counter<WordNGram> ngramCounter = new Counter<WordNGram>();
	int maxLeadingLowerCaseWords=0;
	
	double minimumScore=0;
	CollocationScore bigramScoreFunction = new MI();
	double portionToPrint=1.0;
	int maxPrint=100000;
	
	public void countWords(Document d)
	{
		List<Element> tokens = 
				nl.namescape.tei.TEITagClasses.getWordElements(d.getDocumentElement());
		for (Element e: tokens)
		{
			incrementWordCount();
			String it = getWordOrLemma(e);
			incrementFrequency(it, 1);
		} 
	}

	private synchronized void incrementFrequency(String s, int increment)
	{
		tf.incrementFrequency(s, 1);
	}
	
	public void countBigrams(Document d)
	{
		List<Element> sentences = TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
			String previous = null;
			List<Element> tokens = nl.namescape.tei.TEITagClasses.getTokenElements(s);
			for (Element e: tokens)
			{
				if (TEITagClasses.isWord(e))
				{
					String it = getWordOrLemma(e);
					storeBigram(previous, it);
					previous = it;
				} else
					previous = null;
			} 
		}
	}

	private String getWordOrLemma(Element e) 
	{
		String lemma = e.getAttribute("lemma");
		String wordform = e.getTextContent();
		String tag = e.getAttribute("function");
		String lwt = wordform + "\t" + tag + "\t" + lemma;

		String it = "";
		switch (type)
		{
			case word:  it = wordform; break;
			case lemma: it = lemma; break;
			case lwt: it = lwt; break;
		}
		return it;
	}

	private synchronized void storeBigram(String w1, String w2)
	{
		if (w1==null || w2 == null) return;
		if (tf.getFrequency(w1) < minimumUnigramFrequency || 
				tf.getFrequency(w2) < minimumUnigramFrequency)
			return;
		String[]  parts = {w1,w2};
		WordNGram biGram = new WordNGram(parts);
		bigramCounter.increment(biGram);
	}

	public void scoreBigrams()
	{
		List<WordNGram> bigrams = bigramCounter.keyList();
		for (WordNGram wn: bigrams)
		{
			int f = bigramCounter.get(wn);
			if (f < minimumBigramFrequency)
			{
				bigramCounter.remove(wn);
			}
		}
		bigrams = bigramCounter.keyList();
		for (WordNGram wn: bigrams)
		{
			
			int f = bigramCounter.get(wn);
			int f1 = tf.getFrequency(wn.parts.get(0));
			int f2 = tf.getFrequency(wn.parts.get(1));
			double score = this.score(nTokens,f, f1, f2);
			wn.score = score;
		}
		Collections.sort(bigrams, new ScoreComparator());
		int k=0;
		for (WordNGram wn: bigrams)
		{
			if (k >= maxPrint || k > portionToPrint * bigrams.size())
				break;
			System.out.println(wn.score +  "\t" + bigramCounter.get(wn) + "\t" + wn);
			k++;
		}
		System.out.println("Bigram score function used: "  + this.bigramScoreFunction.getClass().getName());
		System.out.println("Corpus has " + nTokens +  " tokens ");
		System.out.println("We have " + bigrams.size() +  " bigrams! ");
	}

	/* possible additional filter: all lower case words in an
	 * entity are high frequency....
	 * (and so are all lowercase bigrams in an entity)
	 */
	
	public void extendBigrams(Document d)
	{
		List<Element> sentences = TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
			List<Element> tokens = nl.namescape.tei.TEITagClasses.getTokenElements(s);
			for (int i=0; i < tokens.size(); i++)
			{
				String previous = null;
				List<String> nGram = new ArrayList<String>();
				int indexOfFirstCapitalizedWord = Integer.MAX_VALUE;
				int indexOfLastCapitalizedWord = Integer.MAX_VALUE;
				for (int j=i; j < tokens.size(); j++)
				{
					Element e = tokens.get(j);
					if (TEITagClasses.isWord(e))
					{
						String it = getWordOrLemma(e);
						boolean upperCase = isReallyUppercase(i, it);
						if (upperCase)
						{
							if (indexOfFirstCapitalizedWord == Integer.MAX_VALUE) 
								indexOfFirstCapitalizedWord = j-i;
							indexOfLastCapitalizedWord = j-i;
						}
						nGram.add(it);
						if (previous != null)
						{
							WordNGram bi = new WordNGram(previous,it);
							if (bigramCounter.get(bi) < minimumBigramFrequency)
								break;
						}
						previous=it;
					} else break;
				}
				if (indexOfFirstCapitalizedWord < Integer.MAX_VALUE && 
						indexOfFirstCapitalizedWord < maxLeadingLowerCaseWords+1) 
				{
					int minSize = Math.max(3, indexOfFirstCapitalizedWord+1);
					int maxSize = indexOfLastCapitalizedWord + 1;
					if (nGram.size() < minSize || maxSize < minSize)
						continue; // next i
					for (int j=maxSize; j < nGram.size() && j <= maxSize; j++)
					{
						ngramCounter.increment(new WordNGram(nGram,j));
					}
				}
			}
		}
	}

	private boolean isReallyUppercase(int i, String it) 
	{
		boolean upperCase = false;
		if (it.matches("^[A-Z].*"))
		{
			if (i==0)
			{
				Double p = caseProfile.getUpperCaseProportion(it);
				if (p == null || p > 0.5)
					upperCase = true;
			} else
				upperCase = true;
		}
		return upperCase;
	}
	
	private void scoreNgrams() 
	{
		// TODO: first add all bigrams to the ngram hash ?
		List<WordNGram> ngrams = ngramCounter.keyList();
		for (WordNGram wn: ngrams)
		{
			int f = ngramCounter.get(wn);
			if (f < minimumBigramFrequency)
			{
				ngramCounter.remove(wn);
			} else
			{
				System.err.println(f + " " + wn);
			}
		}
		System.err.println("We have "  + ngramCounter.size() + " ngrams! ");
	}
	
	/**
	 * Look for the most frequently occurring lowercase parts of multiword names
	 * other words are most likely NOT part of names
	 */
	
	private void getListOfPossibleNameFunctionWords()
	{
		boolean useFrequency = false;
		Counter<String> lowerCaseParts = new Counter<String>();
		
		for (WordNGram wn: ngramCounter.keySet())
		{
			int f = ngramCounter.get(wn);
			for (String s: wn.parts)
			{
				if (s.toLowerCase().equals(s))
					lowerCaseParts.increment(s,useFrequency?f:1);
			}
		}
		
		System.err.println("LIST OF TOP LOWER CASE PARTS");
		for (String s: lowerCaseParts.keyList())
		{
			System.err.println(lowerCaseParts.get(s) + "\t" + s);
		}
	}
	
	private double score(long nTokens, int f, int f1, int f2) 
	{
		return bigramScoreFunction.score(nTokens, f, f1, f2);
	}


	private synchronized void incrementWordCount()
	{
		nTokens++;
	}

	public void handleFile(String fileName) 
	{
		System.err.println("Stage " + stage + ": "  + fileName);
		try
		{
			Document d = XML.parse(fileName);
			switch (stage)
			{
				case wordFrequency: countWords(d); caseProfile.handleDocument(d); break;
				case bigramFrequency: countBigrams(d); break;
				case nGrams: extendBigrams(d); break;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *  Da Silva et. al. Coherence
	 */
	
	private double SCP(WordNGram w)
	{
		int f = ngramCounter.get(w);
		double p = f/ (double) nTokens;
		int n = w.size();
		double Tp=0;
		for (int i=1; i < n; i++)
		{
			int f1 = ngramCounter.get(w.span(0,i)); // unless single word...
			int f2 = ngramCounter.get(w.span(i,n));
			double p1 = f1 / (double) nTokens;
			double p2 = f2 / (double) nTokens;
			Tp += p1 * p2;
		}
		double Avp = Tp / (double) (n-1);
		return p*p / Avp;
	}
	
	public static void main(String[] args)
	{
		int processors = Runtime.getRuntime().availableProcessors();
		System.err.println("Processors: " + processors);
		MultiwordNameExtractor mwe = new MultiwordNameExtractor();
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(mwe,processors);
		DirectoryHandling.traverseDirectory(m, args[0]);
		m.shutdown();
		mwe.caseProfile.makeProfile();
		
		mwe.stage=Stage.bigramFrequency;
		m = new MultiThreadedFileHandler(mwe,processors);
		DirectoryHandling.traverseDirectory(m, args[0]);
		m.shutdown();
		mwe.scoreBigrams();
		
		mwe.stage = Stage.nGrams;
		m = new MultiThreadedFileHandler(mwe,processors);
		DirectoryHandling.traverseDirectory(m, args[0]);
		m.shutdown();
		mwe.scoreNgrams();	
		mwe.getListOfPossibleNameFunctionWords();
	}
}
