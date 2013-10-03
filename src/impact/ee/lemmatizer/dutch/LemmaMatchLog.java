package impact.ee.lemmatizer.dutch;
import impact.ee.lemmatizer.dutch.LemmaMatch.MatchType;

import java.util.*;

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
}
