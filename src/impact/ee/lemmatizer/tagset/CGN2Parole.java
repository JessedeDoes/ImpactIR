package impact.ee.lemmatizer.tagset;

import java.util.*;

public class CGN2Parole implements TagRelation 
{
	TagSet tagSet1 = new CGNTagSet();
	TagSet tagSet2 = new CGNTagSet();
	
	static final String[][] compatibilities =
	{
		{"N","NOU"},
		{"ADJ","ADJ"},
		{"WW","VRB"},
		{"TW", "NUM"},
		{"VNW", "DET"},
		{"VNW", "PRN"},
		{"LID", "ART"},
		{"BW", "ADV"},
		{"BW", "ADJ"}
	};
	
	
	public CGN2Parole()
	{
		
	}
	@Override
	public boolean compatible(Tag t1, Tag t2) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean corpusTagCompatibleWithLexiconTag(String t1, String t2, boolean allowConversion) 
	{
		// TODO Auto-generated method stub
		String p0 = tagSet1.getPoS(t1);
		String p1 = tagSet2.getPoS(t2);
		for (int i=0; i < compatibilities.length; i++)
		{
			String c0 = compatibilities[i][0];
			String c1 = compatibilities[i][1];
			if (c0.equalsIgnoreCase(p0) && c1.equalsIgnoreCase(p1))
				return true;
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		CGN2Parole x = new CGN2Parole();
		System.out.println(x.corpusTagCompatibleWithLexiconTag("N","NOU",false));
	}
}
