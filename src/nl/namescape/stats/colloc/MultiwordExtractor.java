package nl.namescape.stats.colloc;
import nl.namescape.evaluation.Counter;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.stats.WordList;
import nl.namescape.stats.MakeFrequencyList.Type;
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
	int minimumFrequency = 2;
	enum Type {word, lemma, lwt};
	Type type = Type.word;
	long nTokens=0;
	int Stage=1;
	Counter<WordNGram> bigramCounter = new Counter<WordNGram>();
	double minimumScore=0;
	CollocationScore scoreFunction = new  MI();

	public void countWords(Document d)
	{
		List<Element> tokens = nl.namescape.tei.TEITagClasses.getWordElements(d.getDocumentElement());
		for (Element e: tokens)
		{
			nTokens++;

			String lemma = e.getAttribute("lemma");
			String wordform = e.getTextContent();
			String tag = e.getAttribute("function");
			String lwt = wordform + "\t" + tag + "\t" + lemma;
			
			switch (type)
			{
				case word: tf.incrementFrequency(wordform, 1); break;
				case lemma: tf.incrementFrequency(lemma, 1); break;
				case lwt: tf.incrementFrequency(lwt, 1); break;
			}
		} 
	}

	public void countBigrams(Document d)
	{
		List<Element> sentences = TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
			String previous = null;
			List<Element> tokens = nl.namescape.tei.TEITagClasses.getWordElements(s);
			for (Element e: tokens)
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
				storeBigram(previous, it);
				previous = it;
			} 
		}
	}

	private void storeBigram(String w1, String w2)
	{
		if (w1==null || w2 == null) return;
		if (tf.getFrequency(w1) < minimumFrequency || tf.getFrequency(w2) < minimumFrequency)
			return;
		String[]  parts = {w1,w2};
		WordNGram biGram = new WordNGram(parts);
		//System.err.println(biGram);
		bigramCounter.increment(biGram);
	}

	public void scoreBigrams()
	{
		for (WordNGram wn: bigramCounter.keyList())
		{
			
			int f = bigramCounter.get(wn);
			if (f < minimumFrequency)
			{
				bigramCounter.remove(wn);
				continue;
			}
			int f1 = tf.getFrequency(wn.parts.get(0));
			int f2 = tf.getFrequency(wn.parts.get(1));
			double score = this.score(nTokens,f, f1, f2);
			wn.score = score;
			System.err.println(wn.score +  "\t" + bigramCounter.get(wn) + "\t" + wn);
		}
	}

	private double score(long nTokens, int f, int f1, int f2) 
	{
		return scoreFunction.score(nTokens, f, f1, f2);
	}

	public void handleFile(String fileName) 
	{
		try
		{
			Document d = XML.parse(fileName);
			switch (Stage)
			{
				case 1: countWords(d); break;
				case 2: countBigrams(d); break;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	public static void main(String[] args)
	{
		MultiwordExtractor mwe = new MultiwordExtractor();
		DirectoryHandling.traverseDirectory(mwe, args[0]);
		mwe.Stage=2;
		DirectoryHandling.traverseDirectory(mwe, args[0]);
		mwe.scoreBigrams();
	}
}
