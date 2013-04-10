package impact.ee.tagger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import impact.ee.lemmatizer.Lemmatizer;
import impact.ee.lemmatizer.WordMatch;
import impact.ee.lemmatizer.WordMatchComparator;

public class LookupLemmatizer implements Tagger 
{
	private Lemmatizer baseLemmatizer;
	boolean simplify = true;
	
	public LookupLemmatizer(impact.ee.lemmatizer.Lemmatizer baseLemmatizer)
	{
		this.baseLemmatizer = baseLemmatizer;
	}
	
	@Override
	public HashMap<String, String> apply(Context c) 
	{

		HashMap<String,String> m = new HashMap<String,String>();
		//m.put("word", c.getAttributeAt("word", 0));
		
		for (String key: c.getAttributes())
		{
			m.put(key, c.getAttributeAt(key, 0));
		}
		String w =  c.getAttributeAt("word", 0);
		
		List<WordMatch> s = baseLemmatizer.lookupWordform(w);
		if (s==null || s.size()==0)
		{
			//System.out.println(w + "  --> "  + "NoMatch");
			//test.incrementCount(MatchType.None);
		}
		else
		{     
			//System.out.println(""  + w + " ");
			if (simplify)
			{
				s = WordMatch.simplify(s, true);
			}
			ArrayList<WordMatch> asList = new ArrayList<WordMatch>(s);
			Collections.sort(asList, new WordMatchComparator());
			WordMatch bestMatch = asList.get(0);
			System.err.println(w + "-->" + bestMatch.wordform);
			m.put("lemma", bestMatch.wordform.lemma);
			m.put("tag",  bestMatch.wordform.lemmaPoS);
			m.put("mform",  bestMatch.wordform.modernWordform);
			
			//test.incrementCount(bestMatch.type);
			/*
			System.out.println(w  + " --> " + bestMatch);
			for (WordMatch wf: asList)
			{
				System.out.println("\t" + wf);
			}
			*/
		}
		return m;
	}

	@Override
	public Corpus tag(Corpus inputCorpus) 
	{
		OutputEnumeration out = new OutputEnumeration(this,inputCorpus);
		EnumerationWithContext ewc = 
				new EnumerationWithContext(Map.class, out, new DummyMap());
		
		return new SimpleCorpus(ewc);
	}
}
