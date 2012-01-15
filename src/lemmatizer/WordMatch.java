package lemmatizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lexicon.InMemoryLexicon;

enum MatchType 
{
	HistoricalExact, 
	ModernExact, 
	ModernWithPatterns, 
	HistoricalWithPatterns, 
	ModernHypotheticalExact,   
	ModernHypotheticalWithPatterns,
	None
};

class WordMatchComparator implements Comparator<WordMatch>
{
	public int compare(WordMatch arg0,  WordMatch arg1)
	{
		if (arg0.type == MatchType.HistoricalExact && arg1.type == MatchType.HistoricalExact)
		{
			if (arg0.wordformFrequency != arg1.wordformFrequency)
				return arg1.wordformFrequency - arg0.wordformFrequency;
			else
				return arg1.lemmaFrequency - arg0.lemmaFrequency;
		} else if (arg0.type==MatchType.HistoricalExact)
		{
			return -1;
		} else if (arg1.type==MatchType.HistoricalExact)
		{
			return 1; 
		}
		
		if (arg0.type == MatchType.ModernExact && arg1.type == MatchType.ModernExact)
		{
			if (arg0.wordformFrequency != arg1.wordformFrequency)
				return arg1.wordformFrequency - arg0.wordformFrequency;
			else
				return arg1.lemmaFrequency - arg0.lemmaFrequency;
		} else if (arg0.type==MatchType.ModernExact)
		{
			return -1;
		} else if (arg1.type==MatchType.ModernExact)
		{
			return 1; 
		}
		// how to balance score and frequency....
		
		if (arg0.matchScore < arg1.matchScore) 
			return 1;
		if (arg1.matchScore > arg0.matchScore) 
			return -1;
		return 0;
	}
}

public class WordMatch
{
	lexicon.WordForm wordform;
	double matchScore;
	String target;
	String alignment = ""; 
	MatchType type;
	InMemoryLexicon lexicon;
	int lemmaFrequency=0;
	int wordformFrequency=0;
	boolean correct = false;
	
	public String toString()
	{
		if (type == MatchType.ModernWithPatterns)
			return "{" + wordform + ", " + type + ", " + String.format("%2.2e", matchScore) + ", " + alignment + "}";
		else
			return "{" + wordform + ", " + type + "}";
	}

	public int hashCode()
	{
		return wordform.hashCode() + alignment.hashCode();
	}

	public boolean equals(Object o)
	{
		try
		{
			WordMatch wm = (WordMatch) (o);
			return (wm.wordform.equals(wordform) && wm.type == type && wm.alignment.equals(alignment));
		} catch (Exception e)
		{
			return false;
		}
	}
	
	public static List<WordMatch> simplify(List<WordMatch> set, boolean usePartOfSpeech)
	{
		List<WordMatch> simple = new ArrayList<WordMatch>();
		for (WordMatch wm: set)
		{
			boolean found = false;
			for (WordMatch wm1: simple)
			{
				if (wm1.wordform.lemma.equals(wm.wordform.lemma) && 
						(!usePartOfSpeech || wm1.wordform.lemmaPoS.equals(wm.wordform.lemmaPoS)))
				{
					wm1.lemmaFrequency += wm.lemmaFrequency;
					wm1.wordformFrequency += wm.wordformFrequency;
					found = true;
					break;
				}  
			}
			if (!found)
			{
				simple.add(wm);
			}
		}
		return simple;
	}
}

