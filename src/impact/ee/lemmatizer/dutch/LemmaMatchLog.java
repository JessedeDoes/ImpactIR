package impact.ee.lemmatizer.dutch;
import impact.ee.lemmatizer.dutch.LemmaMatch.MatchType;

import java.util.*;

public class LemmaMatchLog 
{
	Map<String, Set<LemmaMatch>> log = new HashMap<String, Set<LemmaMatch>>();
	void addToLog(String wordform, String lemma, String lexiconTag, String corpusTag, MatchType type)
	{
		String key = wordform + ":" + corpusTag;
		//Stru.toString();
		Set<LemmaMatch> V = log.get(key + "");
		if (V == null)
		{
			V = new HashSet<LemmaMatch>();
			log.put(key, V);
		} else
		{
			//System.err.println("matches for " + key +  " retrieved: " + V);
			//System.exit(1);
		}
		LemmaMatch lm = new LemmaMatch();
		lm.wordform = wordform;
		lm.lemma = lemma;
		lm.corpusTag = corpusTag;
		lm.lexiconTag = lexiconTag;
		lm.type = type;
		V.add(lm);
		//System.err.println("matches for " + wordform +  " now " + V);
	}
	
	Set<LemmaMatch> getLoggedMatches(String wordform, String corpusTag)
	{
		String key = wordform + ":" + corpusTag;
		return log.get(key);
	}
}
