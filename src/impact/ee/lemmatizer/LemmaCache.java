package impact.ee.lemmatizer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LemmaCache 
{
	Map<String,String> cache = new ConcurrentHashMap<String,String>();
	String separator = "->";
	
	public synchronized String get(String wordform, String tag)
	{
		return cache.get(wordform + separator + tag);
	}
	
	public synchronized void put(String wordform, String tag, String lemma)
	{
		cache.put(wordform + separator + tag, lemma);
	}
}
