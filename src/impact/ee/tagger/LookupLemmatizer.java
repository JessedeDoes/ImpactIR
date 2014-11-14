package impact.ee.tagger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import impact.ee.lemmatizer.Lemmatizer;
import impact.ee.lemmatizer.MatchType;
import impact.ee.lemmatizer.WordMatch;
import impact.ee.lemmatizer.WordMatchComparator;
import impact.ee.lemmatizer.tagset.Brown2OED;
import impact.ee.lemmatizer.tagset.TagRelation;
import impact.ee.lexicon.WordForm;

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
		
			ArrayList<WordMatch> wordMatchList = new ArrayList<WordMatch>(s);
			Collections.sort(wordMatchList, new WordMatchComparator());
			String allMatchesAsXML = makeXMLFromList(wordMatchList);
			m.put("allMatches", allMatchesAsXML);
			if (simplify) // simplify after sorting?
			{
				s = WordMatch.simplify(s, true);
			}
			
			WordMatch bestMatch = wordMatchList.get(0);
			boolean foundCompatible = false;
			
			if (tagRelation != null) // look for best compatible tag...
			{
				for (int i=0; i < wordMatchList.size(); i++)
				{
					WordMatch x = wordMatchList.get(i);
					if (tagRelation.corpusTagCompatibleWithLexiconTag(m.get("tag"), x.wordform.lemmaPoS,false))
					{
						bestMatch = x;
						foundCompatible = true;
						break;
					}
				}
			}
			// System.err.println(w + "-->" + bestMatch.wordform);
			
			m.put("lemma", bestMatch.wordform.lemma);
			if (m.get("tag") == null)
			{
				m.put("tag",  bestMatch.wordform.lemmaPoS);
			} else
			{
				String corpusTag = m.get("tag");
				String lexiconTag = bestMatch.wordform.lemmaPoS;
				//m.put("tag", "corpus:"  + corpusTag + ",lexicon:"  + lexiconTag  + ",matching:" + foundCompatible);
				m.put("tag", lexiconTag);
			}
			// laat die mform maar zitten, slaat alleen ergens op als een match met het modern lexicon bereikt is
			
			// m.put("mform",  bestMatch.wordform.modernWordform);
			
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

	private String interp(String type, String content)
	{
		return "<interp type=\"" + type + "\">" + content + "</interp>";
	}
	
	private String makeXMLFromList(ArrayList<WordMatch> wordMatchList) 
	{
		// TODO Auto-generated method stub
		String xml = "<document>";
		for (WordMatch wm: wordMatchList)
		{
			WordForm w = wm.wordform;
			xml += "<interpGrp type=\"lexiconMatch\">";
			xml += interp("matchType", wm.type.toString());
			xml += interp("lemma", w.lemma); // this is risky, and may result in incorrect xml...
			if (w.lemmaID != null && w.lemmaID.length() > 0)
				xml += interp("lemmaId", w.lemmaID);
			xml += interp("partOfSpeech", w.lemmaPoS);
			if (wm.type == MatchType.ModernWithPatterns)
				xml += interp("matchScore", wm.matchScore + "");
			xml += "</interpGrp>";
		}
		xml += "</document>";
		return xml;
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
