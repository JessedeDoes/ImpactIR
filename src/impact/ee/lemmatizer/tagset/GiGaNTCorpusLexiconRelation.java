package impact.ee.lemmatizer.tagset;

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
	@Override
	public boolean compatible(String t1, String t2) 
	{
		// TODO Auto-generated method stub
		Tag tag1 = tagSet.parseTag(t1);
		Tag tag2 = tagSet.parseTag(t2);
		String p1 = tag1.getValues("pos");
		String p2 = tag1.getValues("pos");
		if (!p1.equals(tag2.getValues("pos")))
			return false;
		if (p1.equals("NOU-C") || p1.equals("NOU-P"))
		{
			return intersects(tag1.get("number"), tag2.get("number"));
			
			//return V3
		}
		if (p1.equals("VRB")) // tense must agree.... etc...
		{
			return agreement(tag1.get("number"), tag2.get("number"))
					&& agreement(tag1.get("mood"), tag2.get("mood"))
					&& agreement(tag1.get("tense"), tag2.get("tense"))
					&& agreement(tag1.get("person"), tag2.get("person"));
		}
		if (p1.equals("AA")) // tense must agree.... etc...
		{
			return agreement(tag1.get("degree"), tag2.get("degree"));
		}
		return t1.equals(t2);
	}
}
