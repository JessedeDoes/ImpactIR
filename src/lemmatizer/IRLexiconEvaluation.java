package lemmatizer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IRLexiconEvaluation 
{
	public List<Item> items = new ArrayList<Item>();
	
	public int averageRank =0;
	public int nCorrectSuggestions = 0;
	public int nHistoricalExact = 0;
	public int nModernExact = 0;
	public int nHypothetical = 0;

	public int nSuggestions=0;
	public int sumOfRanks=0;
	public int nItemsWithACorrectSuggestion = 0;
	
	double avgRank = 0; 
	double recall = 0;
	double historicalLexiconCoverage=0;
	double hypotheticalLexiconCoverage=0;
	double modernLexiconCoverage = 0;
	
	Map<MatchType, Integer> typeHash = new HashMap<MatchType, Integer> ();


	
	public void incrementCount(MatchType m)
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
	
	public static class Item
	{
		public String partOfSpeech;
		public String lemma;
		public Set<String> lemmata;
		public String wordForm;
		public List<WordMatch> matches;
		public int rankOfCorrectSuggestion;
		public boolean hasCorrectMatch = false;
		
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
		n.wordForm = wordform;
		n.lemmata = lemmata;
		n.lemma = util.StringUtils.join(lemmata, "|");
		items.add(n);
		return n;
	}
	
	public void calculate()
	{
		double N = items.size();
		
		avgRank = sumOfRanks / (double) nItemsWithACorrectSuggestion;
		recall = nItemsWithACorrectSuggestion / N;
		historicalLexiconCoverage = this.nHistoricalExact / N;
		modernLexiconCoverage = this.nModernExact / N;
		hypotheticalLexiconCoverage = this.nHypothetical / N;
	}

	public void print(PrintStream p) 
	{
		calculate();
		p.println("####\nItems " + items.size() + ", recall:" + recall);
		p.println("Average rank of first correct suggestion: " + avgRank  +  " total # suggestions " + nSuggestions);
		p.println("\nHistorical lexicon coverage: " + historicalLexiconCoverage);
		p.println("Modern lexicon coverage: " + modernLexiconCoverage);
		p.println("Hypothetical lexicon coverage: " + hypotheticalLexiconCoverage);
	}
	
	public void matchItem(Item item, List<WordMatch> unsimplifiedMatches)
	{
		ArrayList<WordMatch> wordMatchListUnsimplified = new ArrayList<WordMatch>(unsimplifiedMatches);
		Collections.sort(wordMatchListUnsimplified, new WordMatchComparator());
		List<WordMatch> wordMatchList = WordMatch.simplify(wordMatchListUnsimplified, false);
		
		item.matches = wordMatchList;
		
		//incrementCount(wordMatchList.get(0).type); // this is wrong: should increase count for 'correct' match...
		
		int k=1;
		HashSet<String> seenLemmata = new HashSet<String>();
		boolean germanWildCard = item.lemmata.contains("*****") || item.lemmata.contains("*****");
		
		
		for (WordMatch wordMatch: unsimplifiedMatches)
		{
			//candidateList += "\t" + wf + "\n";
			String lcLemma = wordMatch.wordform.lemma.toLowerCase();
			if (germanWildCard || item.lemmata.contains(lcLemma)) //  && !seenLemmata.contains(lcLemma)))
			{
				
				if (wordMatch.type==MatchType.HistoricalExact)
					item.inHistoricalLexicon = true;
				if (wordMatch.type==MatchType.ModernExact)
					item.inModernLexicon = true;
				if (wordMatch.type==MatchType.ModernWithPatterns)
					item.inHypotheticalLexicon = true;		
				//incrementCount(wordMatch.type);
			}
		}
		
		for (WordMatch wordMatch: wordMatchList)
		{
			//candidateList += "\t" + wf + "\n";
			String lcLemma = wordMatch.wordform.lemma.toLowerCase();
			nSuggestions++;
			if (germanWildCard || item.lemmata.contains(lcLemma)) //  && !seenLemmata.contains(lcLemma)))
			{
				if (!seenLemmata.contains(lcLemma))
				{
					nCorrectSuggestions++; 
					sumOfRanks += k;
				}
				
				wordMatch.correct = true;
				item.hasCorrectMatch = true;
			}
			seenLemmata.add(lcLemma);
			k++;
		}
		
		if (item.inHistoricalLexicon)
			this.nHistoricalExact++;
		if (item.inModernLexicon)
			this.nModernExact++;
		if (item.inHypotheticalLexicon)
			this.nHypothetical++;
		
		if (item.hasCorrectMatch)
			nItemsWithACorrectSuggestion++;
	}
	
	
	public int size()
	{
		return items.size();
	}
}
