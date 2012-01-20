package lemmatizer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IRLexiconEvaluation 
{

	@XmlElement(name="item")
	public List<Item> items = new ArrayList<Item>();

	@XmlElement
	//public int averageRank =0;
	public int nCorrectSuggestions = 0;
	@XmlElement
	public int nHistoricalExact = 0;
	@XmlElement
	public int nModernExact = 0;
	@XmlElement
	public int nHypothetical = 0;
	@XmlElement
	public int totalNumberOfSuggestions=0;
	@XmlElement
	public double averageNumberOfSuggestions = 0;
	@XmlElement
	public int sumOfRanks=0;
	@XmlElement
	public int nItemsWithACorrectSuggestion = 0;
	public int nItemsWithLemmaInModernLexicon = 0;
	public int nItemsWithLemmaInHistoricalLexicon = 0;

	public int nItemsWithLemmaInHistoricalLexiconWithCorrectMatch=0;
	public int nItemsWithLemmaInModernLexiconWithCorrectMatch=0;
	public double recallOnItemsWithLemmaInModernLexicon=0;
	public double recallOnItemsWithLemmaInHistoricalLexicon=0;
	public double averageRankOfFirstCorrectSuggestion = 0; 

	public double recall = 0;
	public double unrankedPrecision = 0;
	public double historicalLexiconCoverage=0;
	public double hypotheticalLexiconCoverage=0;
	public double modernLexiconCoverage = 0;
	public double historicalLexiconLemmaCoverage=0;
	public double modernLexiconLemmaCoverage=0;

	PrintStream report=System.out;

	Map<MatchType, Integer> typeHash = new HashMap<MatchType, Integer> ();
	Map<String, List<Item>> lemma2item = new  HashMap<String, List<Item>>();
	Map<String, Integer> lemmaFrequency = new HashMap<String, Integer>();

	private boolean precisionComputationPrepared = false;
	
	private void incrementCount(MatchType m)
	{
		Integer z = typeHash.get(m);
		if (z == null)
			typeHash.put(m,1);
		else
			typeHash.put(m,z+1);
	}

	public void matchTypeStatistics()
	{
		for (MatchType m: typeHash.keySet())
		{
			System.err.println(m + ": " + typeHash.get(m));
		}
	}

	@XmlElement
	public double getMeanAveragePrecision()
	{
		prepareForPrecision();
		Set<String> queries = new HashSet<String>();
		queries.addAll(lemmaFrequency.keySet());
		queries.addAll(lemma2item.keySet());
		return computeAveragePrecision(queries);
	}
	
	public double computeAveragePrecision(Set<String> lemmaQueries)
	{
		/*
		 * Precompute hit lists and true lemma frequencies
		 */
		
		prepareForPrecision();

		double d=0;
		
		for (String q: lemmaQueries)
		{
			d += averagePrecision(q);
		}
		return d / (double) lemmaQueries.size();
	}

	/*
	 * make hashes 
	 * from lemma to tokens with that assigned lemma by matcher
	 * from lemma to number of tokens with that lemma as "true" lemma
	 */
	private void prepareForPrecision() 
	{
		if (precisionComputationPrepared)
			return;
		for (Item i: items)
		{
			String lemma = i.lemma.toLowerCase();
			if (!lemmaFrequency.containsKey(lemma))
				lemmaFrequency.put(lemma, 1);
			else
				lemmaFrequency.put(lemma, lemmaFrequency.get(lemma) + 1);

			for (WordMatch wm: i.matches)
			{
				List<Item> l = lemma2item.get(wm.wordform.lemma.toLowerCase());
				if (l == null)
				{
					l = new ArrayList<Item>();
					lemma2item.put(wm.wordform.lemma.toLowerCase(), l);
				}
				l.add(i);
			}
		}

		for (String lemma: lemma2item.keySet())
		{
			List<Item> l = lemma2item.get(lemma);
			Collections.sort(l, new ItemComparator(lemma));
		}
		
		System.err.println("Total distinct TRUE lemmata " + lemmaFrequency.keySet().size());
		System.err.println("Total distinct assigned lemmata " + lemma2item.keySet().size());
		precisionComputationPrepared = true;
	}
	/*
	 * 
	 */
	public class ItemComparator implements Comparator<Item>
	{
		private String lemma;
		public ItemComparator(String l)
		{
			lemma = l;
		}

		private int minRank(Item i)
		{
			for (WordMatch w: i.matches)
			{
				if (w.wordform.lemma.equalsIgnoreCase(lemma))
					return w.rank;
			}
			return Integer.MAX_VALUE;
		}

		@Override
		public int compare(Item arg0, Item arg1) 
		{
			if (minRank(arg0) < minRank(arg1))
				return -1;
			if (minRank(arg0) > minRank(arg1))
				return 1;
			if (arg0.matches.size() < arg1.matches.size())
				return -1;
			if (arg0.matches.size() > arg1.matches.size())
				return 1;
			return 0;
		}
	}

	public double averagePrecision(String lemmaQuery) 
	{
		lemmaQuery = lemmaQuery.toLowerCase();
		List<Item> queryResults = lemma2item.get(lemmaQuery);
		if (queryResults == null)
			return 0;
		
		double d = 0;
		if (!lemmaFrequency.containsKey(lemmaQuery)) // no true hits, return 0
			return d;

		double dR = 1 / (double) lemmaFrequency.get(lemmaQuery);
		double k=1;
		int positives=0;
		for (Item i: queryResults)
		{
			if (i.lemma.equalsIgnoreCase(lemmaQuery))
			{
				positives++;
				d += (positives / k) * dR;
			}
			k++;
		}
		System.err.println("precision for " + lemmaQuery + ": " + d);
		return d;
	}

	@XmlRootElement
	public static class Item
	{
		@XmlAttribute(name="n")
		public int n;
		public String partOfSpeech;
		public String lemma;
		public Set<String> lemmata;
		public String wordForm;
		@XmlElement(name="match")
		public List<WordMatch> matches;
		public int rankOfCorrectSuggestion;
		public boolean hasCorrectMatch = false;
		public boolean lemmaInHistoricalLexicon=false;
		public boolean lemmaInModernLexicon = false;
		boolean inModernLexicon=false;
		boolean inHistoricalLexicon=false;
		boolean inHypotheticalLexicon=false; 

		public String matchesAsString()
		{
			List<String> l = new ArrayList<String>();
			for (WordMatch w: matches)
			{
				l.add(w.toString());
			}
			return util.StringUtils.join(l, " || ");
		}
	};

	public Item addItem(String wordform, Set<String> lemmata)
	{
		Item n = new Item();
		n.n = items.size();
		n.wordForm = wordform;
		n.lemmata = lemmata;
		n.lemma = util.StringUtils.join(lemmata, "|");
		items.add(n);
		return n;
	}

	public void calculate()
	{
		double N = items.size();

		this.averageRankOfFirstCorrectSuggestion = sumOfRanks / (double) nItemsWithACorrectSuggestion;
		recall = nItemsWithACorrectSuggestion / N;
		unrankedPrecision = 
				this.nItemsWithACorrectSuggestion / (double) this.totalNumberOfSuggestions;
		this.averageNumberOfSuggestions = this.totalNumberOfSuggestions / N;
		
		
		historicalLexiconCoverage = this.nHistoricalExact / N;
		modernLexiconCoverage = this.nModernExact / N;
		hypotheticalLexiconCoverage = this.nHypothetical / N;

		if (this.nItemsWithLemmaInHistoricalLexicon > 0)
			this.recallOnItemsWithLemmaInHistoricalLexicon = this.nItemsWithLemmaInHistoricalLexiconWithCorrectMatch 
			/ (double) this.nItemsWithLemmaInHistoricalLexicon;

		if (this.nItemsWithLemmaInModernLexicon > 0)
			this.recallOnItemsWithLemmaInModernLexicon = this.nItemsWithLemmaInModernLexiconWithCorrectMatch 
			/ (double) this.nItemsWithLemmaInModernLexicon;

		this.historicalLexiconLemmaCoverage = this.nItemsWithLemmaInHistoricalLexicon / N;
		this.modernLexiconLemmaCoverage = this.nItemsWithLemmaInModernLexicon / N;
	}

	public void print(PrintStream p) 
	{
		calculate();
		p.println("####\nItems " + items.size() + ", recall:" + recall);
		p.println("Average rank of first correct suggestion: " + this.averageRankOfFirstCorrectSuggestion  +  " total # suggestions " + this.totalNumberOfSuggestions);
		p.println("\nHistorical lexicon coverage: " + historicalLexiconCoverage);
		p.println("Modern lexicon coverage: " + modernLexiconCoverage);
		p.println("Hypothetical lexicon coverage: " + hypotheticalLexiconCoverage);
	}

	public void matchItem(Item item, List<WordMatch> unsimplifiedMatches)
	{
		ArrayList<WordMatch> wordMatchListUnsimplified = new ArrayList<WordMatch>(unsimplifiedMatches);
		Collections.sort(wordMatchListUnsimplified, new WordMatchComparator());
		List<WordMatch> simplifiedMatchList = WordMatch.simplify(wordMatchListUnsimplified, false);

		item.matches = simplifiedMatchList;

		int k=1;
		HashSet<String> seenLemmata = new HashSet<String>();
		boolean germanWildCard = item.lemmata.contains("*****") || item.lemmata.contains("*****");

		// loop over the unsimplified match list to find out about coverage
		for (WordMatch wordMatch: unsimplifiedMatches)
		{
			String lcLemma = wordMatch.wordform.lemma.toLowerCase();
			if (germanWildCard || item.lemmata.contains(lcLemma)) //  && !seenLemmata.contains(lcLemma)))
			{

				if (wordMatch.type==MatchType.HistoricalExact)
					item.inHistoricalLexicon = true;
				if (wordMatch.type==MatchType.ModernExact)
					item.inModernLexicon = true;
				if (wordMatch.type==MatchType.ModernWithPatterns)
					item.inHypotheticalLexicon = true;		
			}
		}

		// to check the average rank of correct matches, we need the simplified list

		for (WordMatch wordMatch: simplifiedMatchList)
		{
			String lcLemma = wordMatch.wordform.lemma.toLowerCase();
			this.totalNumberOfSuggestions++;
			if (germanWildCard || item.lemmata.contains(lcLemma)) //  && !seenLemmata.contains(lcLemma)))
			{
				if (!seenLemmata.contains(lcLemma))
				{
					nCorrectSuggestions++; 
					sumOfRanks += k;
					item.rankOfCorrectSuggestion = k;
				}

				wordMatch.correct = true;
				item.hasCorrectMatch = true;
			}
			seenLemmata.add(lcLemma);
			wordMatch.rank=k++;
		}

		if (item.inHistoricalLexicon)
			this.nHistoricalExact++;
		if (item.inModernLexicon)
			this.nModernExact++;
		if (item.inHypotheticalLexicon)
			this.nHypothetical++;

		if (item.hasCorrectMatch)
			nItemsWithACorrectSuggestion++;

		if (item.lemmaInHistoricalLexicon)
		{
			nItemsWithLemmaInHistoricalLexicon++;
			if (item.hasCorrectMatch)
				nItemsWithLemmaInHistoricalLexiconWithCorrectMatch++;
		}
		if (item.lemmaInModernLexicon)
		{
			nItemsWithLemmaInModernLexicon++;
			if (item.hasCorrectMatch)
				nItemsWithLemmaInModernLexiconWithCorrectMatch++;
		}
	}

	public void marshal()
	{
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {
					IRLexiconEvaluation.class,
					Item.class,
					WordMatch.class,
					lexicon.WordForm.class});


			Marshaller marshaller=jaxbContext.createMarshaller();
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty( Marshaller.JAXB_ENCODING,"UTF-8");
			marshaller.setProperty( Marshaller.JAXB_FRAGMENT, true);

			report.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

			marshaller.marshal( this, report);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@XmlElement(name="nItems")
	public int getSize()
	{
		return items.size();
	}

}
