package impact.ee.lemmatizer;

import java.util.Comparator;

public class WordMatchComparator implements Comparator<WordMatch>
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