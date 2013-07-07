package impact.ee.tagger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import impact.ee.lemmatizer.Lemmatizer;
import impact.ee.lemmatizer.WordMatch;
import impact.ee.lemmatizer.WordMatchComparator;
import impact.ee.lemmatizer.tagset.Brown2OED;
import impact.ee.lemmatizer.tagset.TagRelation;

public class LookupLemmatizer implements Tagger 
{
	private Lemmatizer baseLemmatizer;
	boolean simplify = true;
	
	TagRelation tagRelation = new Brown2OED();
	
	public LookupLemmatizer(impact.ee.lemmatizer.Lemmatizer baseLemmatizer)
	{
		this.baseLemmatizer = baseLemmatizer;
	}
	
	/**
	 * This should have an extra option to check for already assigned 
	 * PoS and check consistency of lexical tag and corpus tag
	 */
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
		
			ArrayList<WordMatch> asList = new ArrayList<WordMatch>(s);
			Collections.sort(asList, new WordMatchComparator());
			if (simplify) // simplify after sorting?
			{
				s = WordMatch.simplify(s, true);
			}
			WordMatch bestMatch = asList.get(0);
			boolean foundCompatible = false;
			if (tagRelation != null) // look for best compatible tag...
			{
				for (int i=0; i < asList.size(); i++)
				{
					WordMatch x = asList.get(i);
					if (tagRelation.compatible(m.get("tag"), x.wordform.lemmaPoS))
					{
						bestMatch = x;
						foundCompatible = true;
						break;
					}
				}
			}
			System.err.println(w + "-->" + bestMatch.wordform);
			
			m.put("lemma", bestMatch.wordform.lemma);
			if (m.get("tag") == null)
			{
				m.put("tag",  bestMatch.wordform.lemmaPoS);
			} else
			{
				String corpusTag = m.get("tag");
				String lexiconTag = bestMatch.wordform.lemmaPoS;
				m.put("tag", "corpus:"  + corpusTag + ",lexicon:"  + lexiconTag  + ",matching:" + foundCompatible);
			}
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

	@Override
	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub
		
	}
}
