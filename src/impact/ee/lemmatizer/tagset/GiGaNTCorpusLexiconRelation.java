package impact.ee.lemmatizer.tagset;

import impact.ee.lexicon.LexiconUtils;
import impact.ee.lexicon.WordForm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;



public class GiGaNTCorpusLexiconRelation implements TagRelation 
{
	TagSet tagSet = new GiGaNTTagSet();
	
	@Override
	public boolean compatible(Tag t1, Tag t2) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	static <Type> Set<Type> intersection(Set<Type> V1, Set<Type> V2)
	{
		Set<Type> intersection = new HashSet<Type>(V1);
		intersection.retainAll(V2);
		//nl.openconvert.log.ConverterLog.defaultLog.println(V1 + " intersect" + V2 +   "  " + intersection);
		return intersection;
	}
	
	static <Type> boolean intersects(Set<Type> V1, Set<Type> V2)
	{
		return !intersection(V1,V2).isEmpty();
	}
	
	static <Type> boolean agreement(Set<Type> V1, Set<Type> V2)
	{
		return V1.isEmpty() && V2.isEmpty() || !intersection(V1,V2).isEmpty();
	}
	
	/**
	 * Better to use this only during lexicon lookup....
	 * @param corpusTag
	 * @param lexiconTag
	 * @return
	 */
	public boolean possibleConversion(Tag corpusTag, Tag lexiconTag)
	{
		
		String corpusPoS = corpusTag.getValues("pos");
		String lexiconPoS = lexiconTag.getValues("pos");
		
		if (corpusPoS.equals("NOU-C")) // denk ook aan "anderen" etc....
		{
			if (lexiconPoS.equals("AA"))
			{
				return true;
			}
			
			if (lexiconPoS.equals("VRB"))
			{
				return 
						corpusTag.hasFeature("number","sg") && lexiconTag.hasFeature("finiteness",  "inf") 
						|| lexiconTag.hasFeature("finiteness",  "part")
				             && 
				             (corpusTag.hasFeature("number",  "sg") && !lexiconTag.hasFeature("formal", "infl-en")
						      || corpusTag.hasFeature("number",  "pl")  && lexiconTag.hasFeature("formal", "infl-en"));
			}
			
			if (lexiconPoS.equals("PD")) // allow use of pronoun as noun (dubious)
			{
				return true;
			}
		}
		
		if (corpusPoS.equals("AA"))
		{
			if (lexiconPoS.equals("VRB")) // ahem -- liever niet
			{
				return lexiconTag.hasFeature("finiteness","part");
			}
			if (lexiconPoS.equals("PD")) // allow use of determiner as adjective
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean corpusTagCompatibleWithLexiconTag(String corpusTag, String lexiconTag, boolean allowConversion) 
	{
		
		Tag tag1 = tagSet.parseTag(corpusTag);
		Tag tag2 = tagSet.parseTag(lexiconTag);
		
		String corpusPoS = tag1.getValues("pos");
		String lexiconPoS = tag2.getValues("pos");
		
		if (!corpusPoS.equals(lexiconPoS))
		{
			if (allowConversion) return possibleConversion(tag1,tag2); else return false;
		}
		
		if (corpusPoS.equals("NOU-C") || corpusPoS.equals("NOU-P"))
		{
			return intersects(tag1.get("number"), tag2.get("number"));
					// && (!tag1.hasFeature("gender") || agreement(tag1.get("gender"), tag2.get("gender")));
		}
		
		if (corpusPoS.equals("VRB")) // tense must agree.... etc...
		{
			return agreement(tag1.get("number"), tag2.get("number"))
					&& agreement(tag1.get("mood"), tag2.get("mood"))
					&& agreement(tag1.get("tense"), tag2.get("tense"))
					&& agreement(tag1.get("formal"), tag2.get("formal"))
					&& (!tag1.hasFeature("person") || agreement(tag1.get("person"), tag2.get("person")));
		}
		
		if (corpusPoS.equals("AA")) // tense must agree.... etc...
		{
			return agreement(tag1.get("degree"), tag2.get("degree"))
					&& agreement(tag1.get("formal"), tag2.get("formal"));
		}
		return corpusPoS.equals(lexiconPoS);
	}
	
	public static void main(String[] args) throws IOException
	{
		GiGaNTCorpusLexiconRelation r = new GiGaNTCorpusLexiconRelation();
		Reader reader = new InputStreamReader(System.in);
		BufferedReader b = new BufferedReader(reader) ; // UTF?
		String s;
		while ( (s = b.readLine()) != null) // volgorde: type lemma pos lemma_pos /// why no ID's? it is better to keep them
		{
			String[] p = s.split("\\s+");
			nl.openconvert.log.ConverterLog.defaultLog.println(r.corpusTagCompatibleWithLexiconTag(p[0],p[1], true));
		}
	}
}
