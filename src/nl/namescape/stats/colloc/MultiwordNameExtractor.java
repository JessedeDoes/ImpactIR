package nl.namescape.stats.colloc;
import nl.namescape.SentencePerLineOutput;
import nl.namescape.evaluation.Counter;
import nl.namescape.stats.CaseProfile;
import nl.namescape.stats.WordList;
//import nl.namescape.stats.MakeFrequencyList.Type;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.util.Util;
import nl.namescape.util.XML;
import nl.openconvert.filehandling.DirectoryHandling;
import nl.openconvert.filehandling.DoSomethingWithFile;
import nl.openconvert.filehandling.MultiThreadedFileHandler;

import org.w3c.dom.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * !!!!!!Only for possible names, uses case information.
 * Try to use collocation information
 * For each sentence... [BLA]
 * @author does
 * should we make this n-pass (? rather NOT)
 * First a typefrequency pass; next a bigram pass, 
 * Next step: add all the rest. (?)
 * 
 * Cf: http://hlt.di.fct.unl.pt/jfs/ClustAnaClassNumEnt.pdf *
 *  
 * En Extraction of Multi-Word Collocations Using Syntactic Bigram
Composition Violeta Seretan, Luka Nerima, Eric Wehrli

---
Locating Complex Named Entities in Web Text

Doug Downey, Matthew Broadhead, and Oren Etzioni
 */


/**
 * TODO:
 * - volgens mij gaat er iets mis waardoor voorkomens van combinaties gemist worden? (done)
 * - Bigrammen overhevelen naar ngram lijst (done)
 * - Zinnen met teveel hoofdletters eruit laten
 * - 
 * @author does
 *
 */
public class MultiwordNameExtractor implements DoSomethingWithFile
{
	WordList tfx = new WordList();
	int minimumUnigramFrequency = 2;
	int minimumBigramFrequency = 2;
	int maxLeadingLowerCaseWords=0;

	CaseProfile caseProfile = new CaseProfile();

	enum Stage 
	{ 
		wordFrequency, 
		bigramFrequency, 
		nGrams,
		getPartsOfNGrams,
		countPartsOfNGrams,
		applyLocalMaxs
	}; 

	// for coherence, we would need a fourth stage to get frequencies of parts of possible multiword entities
	Stage stage= Stage.wordFrequency;

	enum Type {word, lemma, lwt};
	Type type = Type.word;
	long nTokens=0;


	Counter<WordNGram> bigramCounter = new Counter<WordNGram>();
	Counter<WordNGram> nameLikeBigramCounter = new Counter<WordNGram>();
	Counter<WordNGram> ngramCounter = new Counter<WordNGram>();
	Counter<WordNGram> otherNgramCounter = new Counter<WordNGram>();

	double minimumScore=0;
	CollocationScore bigramScoreFunction = new MI();
	double portionToPrint=1.0;
	int maxPrint = 100000;

	public void countWords(Document d)
	{
		List<Element> sentences = TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
			if (!SentencePerLineOutput.sentenceHasEnoughLowercaseCharacters(s))
				continue;
			List<Element> tokens = 
					nl.namescape.tei.TEITagClasses.getWordElements(s);
			for (Element e: tokens)
			{
				incrementWordCount();
				String it = getWordOrLemma(e);
				incrementFrequency(it, 1);
			} 
		}
	}

	private synchronized void incrementFrequency(String s, int increment)
	{
		tfx.incrementFrequency(s, 1);
	}

	private synchronized int getFrequency(String s)
	{
		return tfx.getFrequency(s,true);
	}

	public void countBigrams(Document d)
	{
		List<Element> sentences = TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
			if (!SentencePerLineOutput.sentenceHasEnoughLowercaseCharacters(s))
				continue;
			String previous = null;
			int i=0;
			List<Element> tokens = nl.namescape.tei.TEITagClasses.getTokenElements(s);
			for (Element e: tokens)
			{
				if (TEITagClasses.isWord(e))
				{
					String it = getWordOrLemma(e);
					storeBigram(i,previous, it);
					previous = it;
					i++;
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

	private synchronized void storeBigram(int positionOfSecondPart, String w1, String w2)
	{
		if (w1==null || w2 == null) return;
		if (getFrequency(w1) < minimumUnigramFrequency || 
				getFrequency(w2) < minimumUnigramFrequency)
			return;
		String[]  parts = {w1,w2};
		WordNGram biGram = new WordNGram(parts);
		if (nGramCouldBeName(positionOfSecondPart-1, biGram))
		{
			this.nameLikeBigramCounter.increment(biGram);
		}
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
			int f1 = getFrequency(wn.parts.get(0));
			int f2 = getFrequency(wn.parts.get(1));
			double score = this.score(nTokens,f, f1, f2);
			double SCPScore = this.SCP(wn);
			wn.score = SCPScore;
		}
		//Collections.sort(bigrams, new ScoreComparator());
		int k=0;
		for (WordNGram wn: bigrams)
		{
			if (k >= maxPrint || k > portionToPrint * bigrams.size())
				break;
			if (this.nGramCouldBeName(wn))
			{
				int f1 = getFrequency(wn.parts.get(0));
				int f2 = getFrequency(wn.parts.get(1));
				System.out.println(wn.score +  "\t" + bigramCounter.get(wn) + "\t" + wn + "\t" + f1 + "\t" + f2);
			}
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

	public void extendBigramsToNGrams(Document d)
	{
		List<Element> sentences = TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
			if (!SentencePerLineOutput.sentenceHasEnoughLowercaseCharacters(s))
				continue;
			List<Element> tokens = nl.namescape.tei.TEITagClasses.getTokenElements(s);
			int position = 0;
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
						// BUG: what if first token in sentence is interpunction ...
						boolean upperCase = isReallyUppercase(position, it);
						position++;
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

					//if (nGram.toString().contains("Beusekom"))
					//System.err.println("looking in "  + nGram  + " minSize " + minSize + " maxSize " + maxSize);

					if (nGram.size() < minSize || maxSize < minSize)
						continue; // next i
					for (int j=minSize; j <= nGram.size() && j <= maxSize; j++)
					{
						if (isCapitalized(nGram.get(j-1)))
						{
							WordNGram wng  = new WordNGram(nGram,j);
							// System.err.println("Consider: " + wng + " in " + Util.join(nGram, ", "));
							ngramCounter.increment(wng);
						}
					}
				}
			}
		}
	}

	/**
	 * This is silly - can be done from the ngram hash!!
	 * @param d
	 */

	public void getPartsOfNgrams(Document d)
	{
		List<Element> sentences = TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
			if (!SentencePerLineOutput.sentenceHasEnoughLowercaseCharacters(s))
				continue;
			List<Element> tokens = nl.namescape.tei.TEITagClasses.getTokenElements(s);
			int position = 0;
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
						boolean upperCase = isReallyUppercase(position, it);
						position++;
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

					int lengthOfLongestStoredNGram=0;
					for (int j=minSize; j <= nGram.size() && j <= maxSize; j++)
					{
						if (isCapitalized(nGram.get(j-1)))
						{
							WordNGram wn = new WordNGram(nGram,j);

							if (ngramCounter.containsKey(wn))
							{
								//System.err.println("look at parts of " + wn);
								lengthOfLongestStoredNGram = j;
								// store right chunks
								for (int l=1; l < j; l++)
								{
									String wl = nGram.get(l);
									if (!isCapitalized(wl) && l < j-2)
									{
										WordNGram rightPart = new WordNGram(nGram, l, j);
										//System.err.println("store right part: " + rightPart);
										this.otherNgramCounter.increment(rightPart);
									}
								}
							}
						} 
					}
					// store left chunks for SCP
					//System.err.println("longest at i=" + i + ": " + lengthOfLongestStoredNGram);
					for (int j=3; j < lengthOfLongestStoredNGram; j++)
					{
						String wj = nGram.get(j-1);
						if (!isCapitalized(wj))
						{
							WordNGram leftPart = new WordNGram(nGram,j);
							//System.err.println("store left part: " + leftPart);
							this.otherNgramCounter.increment(leftPart);
						}
					}
				}
			}
		}
	}

	public void countPartsOfNgrams(Document d)
	{
		List<Element> sentences = TEITagClasses.getSentenceElements(d);
		for (Element s: sentences)
		{
			if (!SentencePerLineOutput.sentenceHasEnoughLowercaseCharacters(s))
				continue;
			List<Element> tokens = nl.namescape.tei.TEITagClasses.getTokenElements(s);

			for (int i=0; i < tokens.size(); i++)
			{
				String previous = null;
				List<String> nGram = new ArrayList<String>();


				for (int j=i; j < tokens.size(); j++)
				{
					Element e = tokens.get(j);
					if (TEITagClasses.isWord(e))
					{
						String it = getWordOrLemma(e);
						nGram.add(it);
						if (previous != null)
						{
							WordNGram bi = new WordNGram(previous,it);
							if (bigramCounter.get(bi) < minimumBigramFrequency)
								break;
						}
						previous=it;

						if (j -i > 1)
						{
							WordNGram wn = new  WordNGram(nGram,j-i+1);
							// if (wn.toString().contains("Postma")) System.err.println("count ngram: "  + wn);
							if (this.otherNgramCounter.containsKey(wn))
							{
								//System.err.println("Seen again:" + wn);
								this.otherNgramCounter.increment(wn);
							} else
							{
								// if (wn.parts.get(0).equals("Dirk"))
								//	System.err.println("Nope " + wn);
							}
						}
					} else break;
				}
			}
		}
	}

	private boolean isCapitalized(String s)
	{
		return s.matches("^[A-Z].*");
	}

	private boolean isReallyUppercase(int i, String it) 
	{
		boolean upperCase = false;
		if (isCapitalized(it))
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

	private void pruneNgrams() 
	{
		// TODO: first add all bigrams to the ngram hash ?
		List<WordNGram> ngrams = ngramCounter.keyList();
		for (WordNGram wn: ngrams)
		{
			int f = ngramCounter.get(wn);
			if (f < minimumBigramFrequency)
			{
				ngramCounter.remove(wn);
			} 
		}
		System.err.println("We have "  + ngramCounter.size() + " ngrams! ");
	}

	private void scoreNgrams() 
	{
		// TODO: first add all bigrams to the ngram hash ?
		List<WordNGram> ngrams = ngramCounter.keyList();
		for (WordNGram wn: ngrams)
		{
			int f = ngramCounter.get(wn);
			double d = SCP(wn);
			wn.score = d;
			//System.err.println(f + " " + wn  +  " SCP: " + d);
		}

		// add the bigrams ...

		for (WordNGram bi: this.nameLikeBigramCounter.keySet())
		{
			int f = nameLikeBigramCounter.get(bi);
			if (f > 1)
			{
				ngramCounter.increment(bi,nameLikeBigramCounter.get(bi));
				bi.score = SCP(bi);
			}
		}

		List<WordNGram> nGramList = ngramCounter.keyList();
		Collections.sort(nGramList, new ScoreComparator());
		for (WordNGram wn: nGramList)
		{
			if (filterLowerCaseAndStuff(wn))
			{
				int f = ngramCounter.get(wn);
				System.err.println(f + " " + wn  +  " SCP: " + wn.score);
			}
		}
		System.err.println("We have "  + ngramCounter.size() + " ngrams! ");
	}

	private boolean filterLowerCaseAndStuff(WordNGram wng)
	{
		for (String s: wng.parts)
		{
			if (isCapitalized(s) || NameParticles.isNameParticle(s))
			{
				
			} else
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Look for the most frequently occurring lowercase parts of multiword names
	 * other words are most likely NOT part of names
	 */

	private Counter<String> getListOfPossibleNameFunctionWords()
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
		return lowerCaseParts;
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
				case wordFrequency: 
					countWords(d); 
					caseProfile.handleDocument(d); break;
				case bigramFrequency: countBigrams(d); break;
				case nGrams: extendBigramsToNGrams(d); break;
				case getPartsOfNGrams: getPartsOfNgrams(d); break;
				case countPartsOfNGrams: countPartsOfNgrams(d); break;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *  Da Silva et. al. Coherence
	 *  
	 *  Cluster Analysis and Classification of Named Entities

<pre>
Joaquim F. Ferreira da Silva

Departamento de Informática, Faculdade de Ciências e Tecnologia, Universidade Nova de Lisboa
Quinta da Torre, 2725 Monte da Caparica, Portugal
jfs@di.fct.unl.pt

Zornitsa Kozareva

Faculty of Mathematics and Informatics, Plovdiv University
236, Bulgaria blvd., Plovdiv, Bulgaria
zkozareva@hotmail.com

José Gabriel Pereira Lopes

Departamento de Informática, Faculdade de Ciências e Tecnologia, Universidade Nova de Lisboa
Quinta da Torre, 2725 Monte da Caparica, Portugal
gpl@di.fct.unl.pt
</pre>

<p>
Three tools working together, are used for extracting MWUs
from any corpus: the LocalMaxs algorithm, the Symmetric
Conditional Probability (SCP) statistical measure and the
Fair Dispersion Point Normalization (FDPN) (Silva &
Lopes, 1999). Thus, let us take an n-gram as a string of n
words in any text. So, isolated words are 1-grams and the
string President of the Republic is a 4-gram. One can
intuitively accept that there is a strong cohesion within the
4-gram United Nations General Assembly, but not in the 4-
gram of that but not. LocalMaxs algorithm is based on the
idea that a MWU should be an n-gram whose cohesion is
higher than any (n-1)-gram contained in the n-gram; and
should also be higher than the cohesion of all the (n+1)-
grams containing that n-gram. Thus, LocalMaxs needs to
compare cohesions of n-grams having different sizes: (n+1),
n and (n-1) and sharing all but one word in the borders, as
we are interested on sequential n-grams. Then FDPN
concept is applied to the SCP(.) measure in order to
“transform” every n-gram of any length (n) in a pseudo-
bigram, and then a new measure, SCP_f(.), is obtained
(Silva & Lopes, 1999).
</p>
<pre>
SCP_f ( w1 .. wn ) = p(w1..wn)^2 / Avp

where

Avp = 1 / (n-1) * ∑(i=1..n-1) p(w1..wi) p(w_(i+1)..wn)
</pre>
<p>
where p(w1…wj) is the probability of the n-gram w1…wj in
the corpus. So, SCP_f(.) reflects the average cohesion
between any two adjacent contiguous sub-n-gram of the
original n-gram.
<p>
Also:

Silva, J. F. & Dias, G. & Guilloré, S. & Lopes, G. P.
(1999). Using LocalMaxs Algorithm for the Extraction of
Contiguous and Non-contiguous Multiword Lexical Units.
In Lectures Notes in Artificial Intelligence, Springer-
Verlag, volume 1695, (pp 113--132).


<p>

Ik zie niet hoe dit bijdraagt aan
oplossing van UC -- sequence of LC -- UC
omdat "Jan naar Parijs" ging
Jan // naar Parijs ....
Pfft.

<p>
Bijvoorbeeld: algemene voornaam / zeldzame achternaam
Dan SCP niet zo hoog ...
Ook bijvoorbeeld "Van + zeldzame achternaam"
*/

	private double SCP(WordNGram w)
	{
		int f = (w.size() == 2?bigramCounter:ngramCounter).get(w);
		double p = f / (double) nTokens;
		int n = w.size();
		double Tp=0;
		double K=0;
		for (int i=1; i < n; i++)
		{
			int f1 = getSpanFrequency(w,0,i);
			int f2 = getSpanFrequency(w,i,n);
			if (f1 > 0 && f2 > 0)
			{
				double p1 = f1 / (double) nTokens;
				double p2 = f2 / (double) nTokens;
				Tp += p1 * p2;
				K++;
			} else
			{
				System.err.println("!!Problem at i=" + i + " with " + w + " f1 = "  + f1 + " f2 = "  + f2);
			}
		}
		//double Avp = Tp / (double) (n-1); 
		double Avp = Tp / K; 
		return p*p / Avp;
	}

	boolean nGramCouldBeName(WordNGram w)
	{
		boolean firstOK = isCapitalized(w.parts.get(0));
		boolean lastOK = isCapitalized(w.parts.get(w.size()-1));
		return firstOK && lastOK;
	}

	boolean nGramCouldBeName(int i, WordNGram w)
	{
		boolean firstOK = isReallyUppercase(i,w.parts.get(0));
		boolean lastOK = isCapitalized(w.parts.get(w.size()-1));
		return firstOK && lastOK;
	}

	private int getSpanFrequency(WordNGram w, int start, int end)
	{
		if (start == end-1)
			return getFrequency(w.parts.get(start));

		WordNGram wng = w.span(start, end);

		if (start == end-2)
		{
			// only lookup if we have a name-like bigram
			return this.bigramCounter.get(wng);
		}

		if (this.nGramCouldBeName(wng))
		{
			//System.err.println("Try as name " + wng);
			return this.ngramCounter.get(wng);
		}
		else
		{
			//System.err.println("Try as nonname " + wng);
			int f = this.otherNgramCounter.get(wng);
			if (f == 0)
				System.err.println("Huh... " + wng  + " in other Ngram hash?: " + otherNgramCounter.containsKey(wng));
			return f;
		}
	}

	private void resetOtherNGramCounters() 
	{
		for (Entry<WordNGram,Integer> e: this.otherNgramCounter.entrySet())
		{
			e.setValue(0);
			//System.err.println("Whoops " + e);
		}
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
		mwe.pruneNgrams();	
		mwe.getListOfPossibleNameFunctionWords();

		mwe.stage = Stage.getPartsOfNGrams;
		m = new MultiThreadedFileHandler(mwe,processors);
		DirectoryHandling.traverseDirectory(m, args[0]);
		m.shutdown();

		mwe.resetOtherNGramCounters();

		mwe.stage = Stage.countPartsOfNGrams;
		m = new MultiThreadedFileHandler(mwe,processors);
		DirectoryHandling.traverseDirectory(m, args[0]);
		m.shutdown();

		mwe.scoreNgrams();		
	}	
}
