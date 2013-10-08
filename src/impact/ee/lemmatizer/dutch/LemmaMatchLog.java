package impact.ee.lemmatizer.dutch;
import impact.ee.lemmatizer.dutch.LemmaMatch.MatchType;

import java.util.*;

import nl.namescape.evaluation.Counter;

public class LemmaMatchLog 
{
	Map<String, Set<LemmaMatch>> log = new HashMap<String, Set<LemmaMatch>>();
	
	void addToLog(String wordform, String lemma, String lexiconTag, String corpusTag, MatchType type)
	{
		String key = wordform + ":" + corpusTag;
		
		Set<LemmaMatch> V = log.get(key + "");
		
		if (V == null)
		{
			V = new HashSet<LemmaMatch>();
			log.put(key, V);
		} else
		{
			
		}
		LemmaMatch lm = new LemmaMatch();
		
		lm.wordform = wordform;
		lm.lemma = lemma;
		lm.corpusTag = corpusTag;
		lm.lexiconTag = lexiconTag;
		lm.type = type;
		
		V.add(lm);
	}
	
	Set<LemmaMatch> getLoggedMatches(String wordform, String corpusTag)
	{
		String key = wordform + ":" + corpusTag;
		return log.get(key);
	}
	
	Counter<String> getGuesserPoSMatches()
	{
		Counter<String> c = new Counter<String>();
		for (Set<LemmaMatch> V: this.log.values())
		{
			for (LemmaMatch lm: V)
			{
			   if (lm.type == MatchType.Guesser)
				   c.increment(lm.corpusTag  + "~" + lm.lexiconTag);   
			}
		}
		return c;
	}
}
