package nl.namescape.stats.colloc;
import nl.namescape.evaluation.Counter;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.filehandling.MultiThreadedFileHandler;
import nl.namescape.stats.WordList;
//import nl.namescape.stats.MakeFrequencyList.Type;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.util.XML;

import org.w3c.dom.*;

import java.util.*;

/**
 * Try to use collocation information
 * For each sentence... [BLA]
 * @author does
 * should we make this n-pass (? rather NOT)
 * First a typefrequency pass; next a bigram pass, 
 * Next step: add all the rest. (?)
 */

public class MultiwordExtractor implements DoSomethingWithFile
{
	WordList tf = new WordList();
	int minimumUnigramFrequency = 2;
	int minimumBigramFrequency = 2;
	enum Type {word, lemma, lwt};
	Type type = Type.word;
	long nTokens=0;
	int stage=1;
	Counter<WordNGram> bigramCounter = new Counter<WordNGram>();
	Counter<WordNGram> ngramCounter = new Counter<WordNGram>();
	
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
				int foundFirstUpperCaseAt = Integer.MAX_VALUE;
				int foundLastUpperCaseAt = Integer.MAX_VALUE;
				for (int j=i; j < tokens.size(); j++)
				{
					Element e = tokens.get(j);
					if (TEITagClasses.isWord(e))
					{
						String it = getWordOrLemma(e);
						if (it.matches("^[A-Z].*"))
						{
							if (foundFirstUpperCaseAt == Integer.MAX_VALUE) 
								foundFirstUpperCaseAt = j-i;
							foundLastUpperCaseAt = j-i;
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
				if (foundFirstUpperCaseAt < Integer.MAX_VALUE) 
				{
					int startAt = Math.min(3, foundFirstUpperCaseAt+1);
					if (nGram.size() < startAt)
						continue; // next i
					for (int j=startAt; j < nGram.size() && j <= foundLastUpperCaseAt+1; j++)
					{
						ngramCounter.increment(new WordNGram(nGram,j));
					}
				}
			}
		}
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
				case 1: countWords(d); break;
				case 2: countBigrams(d); break;
				case 3: extendBigrams(d); break;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	public static void main(String[] args)
	{
		int processors = Runtime.getRuntime().availableProcessors();
		System.err.println("Processors: " + processors);
		MultiwordExtractor mwe = new MultiwordExtractor();
		MultiThreadedFileHandler m = new MultiThreadedFileHandler(mwe,processors);
		DirectoryHandling.traverseDirectory(m, args[0]);
		m.shutdown();
		mwe.stage=2;
		m = new MultiThreadedFileHandler(mwe,processors);
		DirectoryHandling.traverseDirectory(m, args[0]);
		m.shutdown();
		mwe.scoreBigrams();
		mwe.stage = 3;
		m = new MultiThreadedFileHandler(mwe,processors);
		DirectoryHandling.traverseDirectory(m, args[0]);
		m.shutdown();
		mwe.scoreNgrams();
		
	}


}
